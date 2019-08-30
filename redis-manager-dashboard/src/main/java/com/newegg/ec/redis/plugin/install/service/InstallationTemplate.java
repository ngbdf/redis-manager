package com.newegg.ec.redis.plugin.install.service;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.newegg.ec.redis.client.RedisClientFactory;
import com.newegg.ec.redis.client.RedisClusterClient;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.service.IMachineService;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.util.NetworkUtil;
import com.newegg.ec.redis.util.SlotBalanceUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.newegg.ec.redis.entity.NodeRole.MASTER;
import static com.newegg.ec.redis.entity.NodeRole.SLAVE;
import static com.newegg.ec.redis.util.SignUtil.COLON;
import static com.newegg.ec.redis.util.SignUtil.COMMAS;
import static com.newegg.ec.redis.util.TimeRangeUtil.FIVE_SECONDS;
import static com.newegg.ec.redis.util.TimeRangeUtil.TEN_SECONDS;
import static javax.management.timer.Timer.ONE_MINUTE;
import static org.apache.logging.log4j.ThreadContext.isEmpty;

/**
 * 集群安装模板
 * <p>
 * TODO:
 * 1.最大
 *
 * @author Jay.H.Zou
 * @date 2019/7/26
 */
public class InstallationTemplate {

    @Autowired
    private IMachineService machineService;

    @Autowired
    private IRedisService redisService;

    private static final String CIRCLE_POLICY = "circle";

    private static final String RANDOM_POLICY = "random";

    private static final int MAX_PORT = 65535;

    /**
     * 模板通用，调用方传入不同的策略
     *
     * @param installationOperation
     * @param installationParam
     * @return
     */
    public boolean install(AbstractOperationManage installationOperation, InstallationParam installationParam) {
        boolean verify = verify(installationParam);
        if (verify) {
            return false;
        }
        // 数据准备
        boolean prepareSuccess = prepareForInstallation(installationOperation, installationParam);
        if (!prepareSuccess) {
            return false;
        }
        // 拉取安装包
        boolean pullImageSuccess = installationOperation.pullImage(installationParam);
        if (!pullImageSuccess) {
            return false;
        }
        // 分发配置文件
        boolean buildConfigSuccess = installationOperation.buildConfig(installationParam);
        if (!buildConfigSuccess) {
            return false;
        }
        // 节点安装
        boolean installSuccess = installationOperation.install(installationParam);
        if (!installSuccess) {
            return false;
        }
        buildCluster(installationParam);

        return saveToDB(installationParam);
    }

    /**
     * 校验参数
     *
     * @param installationParam
     * @return
     */
    public boolean verify(InstallationParam installationParam) {
        return true;
    }

    private boolean prepareForInstallation(AbstractOperationManage installationOperation, InstallationParam installationParam) {
        // 获取机器列表
        buildMachineList(installationParam);
        // 构建集群拓扑图
        boolean buildTopologySuccess = buildTopology(installationParam);
        if (!buildTopologySuccess) {
            return false;
        }
        // 构建机器和Redis节点结构
        buildMachineRedisNodeMap(installationParam);
        // 检查机器列表和节点列表
        boolean checkResult = check(installationParam);
        if (checkResult) {
            return false;
        }
        // 检查安装环境
        boolean checkEnvironmentPass = installationOperation.checkInstallationEnv(installationParam);
        if (!checkEnvironmentPass) {
            return false;
        }
        return true;
    }

    /**
     * 获取机器列表
     * 1. 选择机器
     * 2. 通过文本框获取(js 解析后传入后台)
     *
     * @param installationParam
     */
    private void buildMachineList(InstallationParam installationParam) {
        boolean autoBuild = installationParam.isAutoBuild();
        if (autoBuild) {
            List<String> machineIdList = installationParam.getMachineIdList();
            List<Machine> machineList = machineService.getMachineListByIds(machineIdList);
            installationParam.setMachineList(machineList);
        }
        // 如果是文本框填入，则机器列表在前端处理后传入后台
    }

    /**
     * 构建集群拓扑图
     * 1. 用户自定义
     * 2. 系统生成
     *
     * @return
     */
    private boolean buildTopology(InstallationParam installationParam) {
        boolean autoBuild = installationParam.isAutoBuild();
        Multimap<RedisNode, RedisNode> topology = ArrayListMultimap.create();
        if (autoBuild) {
            List<Machine> machineList = installationParam.getMachineList();
            int masterNumber = installationParam.getMasterNumber();
            int replicationNumber = installationParam.getReplicationNumber();
            int nodeNumber = masterNumber + replicationNumber;
            int port = installationParam.getStartPort();
            // TODO: 修改
            Multimap<Machine, Integer> machinePortsMap = buildMachinePortsMap(machineList, port, nodeNumber);
            String policy = installationParam.getPolicy();
            /*Map<RedisNode, List<RedisNode>> tempTopology;
            if (Objects.equals(policy, CIRCLE_POLICY)) {
                tempTopology = randomPolicy(machinePortsMap, masterNumber, replicationNumber);
            } else {
                tempTopology = randomPolicy(machinePortsMap, masterNumber, replicationNumber);
            }
            tempTopology.forEach((masterNode, slaveNodes) -> {
                slaveNodes.forEach(slaveNode -> {
                    topology.put(masterNode, slaveNode);
                });
            });*/
        } else {
            List<RedisNode> redisNodeList = installationParam.getRedisNodeList();
            RedisNode masterNode = null;
            for (RedisNode redisNode : redisNodeList) {
                if (Objects.equals(redisNode.getNodeRole(), MASTER)) {
                    masterNode = redisNode;
                }
                if (Objects.equals(redisNode.getNodeRole(), SLAVE)) {
                    Collection<RedisNode> slaves = topology.get(masterNode);
                    if (slaves == null || slaves.isEmpty()) {
                        redisNode.setNodeRole(MASTER);
                        topology.put(redisNode, null);
                    } else {
                        topology.put(masterNode, redisNode);
                    }
                }
            }
        }
        installationParam.setTopology(topology);
        return true;
    }

    private Map<RedisNode, List<RedisNode>> randomPolicy(List<MachineAndPorts> machineAndPortsList, int masterNumber, int replicationNumber) {
        Map<RedisNode, List<RedisNode>> topology = buildMasterNodes(machineAndPortsList, masterNumber);
        List<RedisNode> allSlaveNodes = new ArrayList<>();
        machineAndPortsList.forEach(machineAndPorts -> {
            Machine machine = machineAndPorts.getMachine();
            Set<Integer> ports = machineAndPorts.getPorts();
            for (Integer port : ports) {
                allSlaveNodes.add(new RedisNode(machine.getHost(), port, SLAVE));
            }
        });
        topology.forEach((masterNode, slaveNodeList) -> {
            if (slaveNodeList == null) {
                return;
            }
            while (slaveNodeList.size() < replicationNumber && !allSlaveNodes.isEmpty()) {
                Random random = new Random();
                int size = allSlaveNodes.size();
                int index = random.nextInt(size);
                RedisNode slaveNode = allSlaveNodes.get(index);
                slaveNodeList.add(slaveNode);
                allSlaveNodes.remove(index);
            }
        });
        return topology;
    }

    // TODO: 未完成
    /*private Map<RedisNode, List<RedisNode>> circlePolicy(List<MachineAndPorts> machineAndPortsList, int masterNumber, int replicationNumber) {
        Map<RedisNode, List<RedisNode>> topology = buildMasterNodes(machineAndPortsList, masterNumber);
        // 排序
        topology.forEach((masterNode, slaveNodeList) -> {
            if (getPortNumber(machineAndPortsList) == 0) {
                return;
            }
            while (getPortNumber(machineAndPortsList) > 0 && slaveNodeList.size() < replicationNumber) {
                int size = machineAndPortsList.size();
                for (int i = size - 1; i > 0; i--) {
                    MachineAndPorts machineAndPorts = machineAndPortsList.get(i);
                    Machine machine = machineAndPorts.getMachine();
                    Set<Integer> ports = machineAndPorts.getPorts();
                    if (slaveNodeList.size() >= replicationNumber || ports.isEmpty()) {
                        continue;
                    }
                    Iterator<Integer> portIterator = ports.iterator();
                    Integer port = portIterator.next();
                    slaveNodeList.add(new RedisNode(machine.getHost(), port, SLAVE));
                    portIterator.remove();
                }
            }
        });
        return topology;
    }*/


    private Map<RedisNode, List<RedisNode>> buildMasterNodes(List<MachineAndPorts> machineAndPortsList, int masterNumber) {
        // 排序
        Map<RedisNode, List<RedisNode>> topology = new LinkedHashMap<>();
        while (topology.keySet().size() < masterNumber) {
            if (getPortNumber(machineAndPortsList) == 0) {
                break;
            }
            for (MachineAndPorts machineAndPorts : machineAndPortsList) {
                Set<Integer> ports = machineAndPorts.getPorts();
                if (topology.keySet().size() >= masterNumber || ports.isEmpty()) {
                    break;
                }
                Iterator<Integer> portIterator = ports.iterator();
                RedisNode masterNode = new RedisNode(machineAndPorts.getMachine().getHost(), portIterator.next(), MASTER);
                topology.put(masterNode, new ArrayList<>());
                portIterator.remove();
            }
        }
        return topology;
    }

    private int getPortNumber(List<MachineAndPorts> machineAndPortsList) {
        int portNumber = 0;
        for (MachineAndPorts machineAndPorts : machineAndPortsList) {
            portNumber += machineAndPorts.getPorts().size();
        }
        return portNumber;
    }

    /**
     * Get useful ports
     *
     * @param machineList
     * @param nodeNumber
     * @param port
     * @return
     */
    private Multimap<Machine, Integer> buildMachinePortsMap(List<Machine> machineList, int port, int nodeNumber) {
        Multimap<Machine, Integer> machinePortsMap = ArrayListMultimap.create();
        for (Machine machine : machineList) {
            Collection<Integer> ports = machinePortsMap.get(machine);
            while (nodeNumber < ports.size()) {
                if (port >= MAX_PORT) {
                    break;
                }
                try {
                    if (!NetworkUtil.telnet(machine.getHost(), port)) {
                        machinePortsMap.put(machine, port);
                    }
                } catch (Exception e) {
                    // TODO: websocket
                }
                port++;
            }
        }
        return machinePortsMap;
    }

    public static void main(String[] args) {
        List<MachineAndPorts> machinePortsMap = new ArrayList<>();
        for (int i = 11; i < 88; i += 11) {
            Machine machine = new Machine(i + "");
            Set<Integer> ports = new TreeSet<>();
            for (int j = 0; j < 6; j++) {
                ports.add(j);
            }
            machinePortsMap.add(new MachineAndPorts(machine, ports));
        }
        /*Set<Integer> ports = new TreeSet<>();

        for (int i = 0; i < 120; i++) {
            ports.add(i);
        }
        machinePortsMap.add(new MachineAndPorts(new Machine("127.0.0.1"), ports));*/
        InstallationTemplate installationTemplate = new InstallationTemplate();
        Map<RedisNode, List<RedisNode>> redisNodeListMap = installationTemplate.randomPolicy(machinePortsMap, 10, 2);
        redisNodeListMap.forEach((master, slaves) -> {
            System.err.println(master.getHost() + ":" + master.getPort() + " master");
            slaves.forEach(slave -> {
                System.err.println(slave.getHost() + ":" + slave.getPort());
            });
        });
    }

    public void buildMachineRedisNodeMap(InstallationParam installationParam) {
        Multimap<Machine, RedisNode> machineAndRedisNode = ArrayListMultimap.create();
        List<Machine> machineList = installationParam.getMachineList();
        List<RedisNode> redisNodeList = installationParam.getRedisNodeList();
        for (Machine machine : machineList) {
            for (RedisNode redisNode : redisNodeList) {
                if (Objects.equals(machine.getHost(), redisNode.getHost())) {
                    machineAndRedisNode.put(machine, redisNode);
                }
            }
        }
        installationParam.setMachineAndRedisNode(machineAndRedisNode);
    }

    private boolean check(InstallationParam installationParam) {
        List<RedisNode> redisNodeList = installationParam.getRedisNodeList();
        List<Machine> machineList = installationParam.getMachineList();
        return !(redisNodeList.isEmpty() || machineList.isEmpty());
    }

    /**
     * cluster master meet
     * slave build
     *
     * @param installationParam
     * @return
     */
    private void buildCluster(InstallationParam installationParam) {
        Cluster cluster = installationParam.getCluster();
        Multimap<RedisNode, RedisNode> topology = installationParam.getTopology();
        RedisNode seed = getSeedNode(cluster, topology);
        List<RedisNode> redisNodeList = installationParam.getRedisNodeList();
        // TODO: websocket
        String result = redisService.clusterMeet(cluster, seed, redisNodeList);
        List<RedisNode> redisNodeListWithInfo = waitClusterMeet(installationParam, seed, redisNodeList);
        replicate(cluster, topology, redisNodeListWithInfo);

        // 设置密码
        String updateRedisPasswordResult = redisService.updateRedisPassword(cluster);
        // TODO: websocket
        boolean autoInit = installationParam.isAutoInit();
        if (autoInit) {
            initSlot(cluster);
        }
    }

    /**
     * Wait for cluster meeting
     *
     * @param installationParam
     * @param seed
     * @param redisNodeList
     * @return
     */
    private List<RedisNode> waitClusterMeet(InstallationParam installationParam, RedisNode seed, List<RedisNode> redisNodeList) {
        // 获取集群节点
        RedisClusterClient redisClusterClient = RedisClientFactory.buildRedisClusterClient(seed);
        List<RedisNode> redisNodeListWithInfo = new ArrayList<>();
        long timeout = 0;
        // 超过一分钟则认为完成
        while (timeout < ONE_MINUTE) {
            try {
                redisNodeListWithInfo = redisClusterClient.clusterNodes();
                if (redisNodeListWithInfo.size() < redisNodeList.size()) {
                    Thread.sleep(TEN_SECONDS);
                    timeout += TEN_SECONDS;
                }
            } catch (Exception e) {
                // TODO: websocket
            }
        }
        if (redisNodeListWithInfo.size() < redisNodeList.size()) {
            installationParam.setAutoInit(false);
            // TODO: websocket 告知用户少了 node
        }
        return redisNodeListWithInfo;
    }

    private RedisNode getSeedNode(Cluster cluster, Multimap<RedisNode, RedisNode> topology) {
        Iterator<RedisNode> iterator = topology.keySet().iterator();
        RedisNode seed = iterator.next();
        if (!NetworkUtil.telnet(seed.getHost(), seed.getPort())) {
            while (iterator.hasNext()) {
                if (NetworkUtil.telnet(seed.getHost(), seed.getPort())) {
                    seed = iterator.next();
                    break;
                }
            }
        }
        cluster.setNodes(seed.getHost() + COLON + seed.getPort());
        return seed;
    }

    private void replicate(Cluster cluster, Multimap<RedisNode, RedisNode> topology, List<RedisNode> redisNodeListWithInfo) {
        Multimap<RedisNode, RedisNode> realTopology = ArrayListMultimap.create();
        // 将 master 替换掉
        for (Map.Entry<RedisNode, RedisNode> entry : topology.entries()) {
            RedisNode masterNode = entry.getKey();
            String host = masterNode.getHost();
            int port = masterNode.getPort();
            for (RedisNode redisNodeWithInfo : redisNodeListWithInfo) {
                if (Objects.equals(host, redisNodeWithInfo.getHost()) && port == redisNodeWithInfo.getPort()) {
                    realTopology.put(masterNode, entry.getValue());
                }
            }
        }
        realTopology.forEach((masterNode, slaveNode) -> {
            String replicateResult = redisService.clusterReplicate(cluster, masterNode, slaveNode);
            if (!Strings.isNullOrEmpty(replicateResult)) {
                // TODO: websocket
            }
        });
        try {
            Thread.sleep(FIVE_SECONDS);
        } catch (InterruptedException e) {
        }
    }

    private void generateConnectionNodes(Cluster cluster) {
        List<RedisNode> allRedisNodeList = redisService.getRedisNodeList(cluster);
        // 随机选前3个节点
        StringBuffer nodes = new StringBuffer();
        int size = allRedisNodeList.size();
        RedisNode redisNode1 = allRedisNodeList.get(0);
        nodes.append(redisNode1.getHost()).append(COLON).append(redisNode1.getPort());
        if (size > 3) {
            RedisNode redisNode2 = allRedisNodeList.get(1);
            RedisNode redisNode3 = allRedisNodeList.get(2);
            nodes.append(COMMAS).append(redisNode2.getHost()).append(COLON).append(redisNode2.getPort())
                    .append(COMMAS).append(redisNode3.getHost()).append(COLON).append(redisNode3.getPort());
        }
        cluster.setNodes(nodes.toString());
    }

    /**
     * Slot distribution for redis cluster
     *
     * @param cluster
     * @return
     */
    private void initSlot(Cluster cluster) {
        List<RedisNode> masterNodeList = redisService.getRedisMasterNodeList(cluster);
        List<SlotBalanceUtil.Shade> balanceSlots = SlotBalanceUtil.balanceSlot(masterNodeList.size());
        Map<RedisNode, SlotBalanceUtil.Shade> masterNodeShadeMap = new LinkedHashMap<>();
        for (int i = 0; i < masterNodeList.size(); i++) {
            RedisNode redisNode = masterNodeList.get(i);
            SlotBalanceUtil.Shade shade = balanceSlots.get(i);
            masterNodeShadeMap.put(redisNode, shade);
        }
        String result = redisService.clusterAddSlotsBatch(cluster, masterNodeShadeMap);
    }

    /**
     * standalone node meet
     * slave build
     *
     * @param installationParam
     * @return
     */
    private boolean buildStandalone(InstallationParam installationParam) {
        return false;
    }

    private boolean saveToDB(InstallationParam installationParam) {
        // 获取连接节点
        generateConnectionNodes(installationParam.getCluster());
        return true;
    }

    private static class MachineAndPorts {
        private Machine machine;

        private Set<Integer> ports;

        public MachineAndPorts(Machine machine, Set<Integer> ports) {
            this.machine = machine;
            this.ports = ports;
        }

        public Machine getMachine() {
            return machine;
        }

        public void setMachine(Machine machine) {
            this.machine = machine;
        }

        public Set<Integer> getPorts() {
            return ports;
        }

        public void setPorts(Set<Integer> ports) {
            this.ports = ports;
        }
    }

}
