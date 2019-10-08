package com.newegg.ec.redis.plugin.install;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.newegg.ec.redis.client.RedisClient;
import com.newegg.ec.redis.client.RedisClientFactory;
import com.newegg.ec.redis.client.RedisClusterClient;
import com.newegg.ec.redis.controller.websocket.InstallationWebSocketHandler;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.plugin.install.service.AbstractNodeOperation;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.IMachineService;
import com.newegg.ec.redis.service.INodeInfoService;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.util.NetworkUtil;
import com.newegg.ec.redis.util.SlotBalanceUtil;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.newegg.ec.redis.entity.NodeRole.MASTER;
import static com.newegg.ec.redis.entity.NodeRole.SLAVE;
import static com.newegg.ec.redis.util.RedisConfigUtil.MASTER_AUTH;
import static com.newegg.ec.redis.util.RedisConfigUtil.REQUIRE_PASS;
import static com.newegg.ec.redis.util.SignUtil.COLON;
import static com.newegg.ec.redis.util.SignUtil.COMMAS;
import static com.newegg.ec.redis.util.TimeUtil.FIVE_SECONDS;
import static com.newegg.ec.redis.util.TimeUtil.TEN_SECONDS;
import static javax.management.timer.Timer.ONE_MINUTE;

/**
 * 集群安装模板
 * <p>
 * TODO:
 * 1.standalone slave of
 * 2.集群扩容功能
 *
 * @author Jay.H.Zou
 * @date 2019/7/26
 */
@Component
public class InstallationTemplate {

    private static final Logger logger = LoggerFactory.getLogger(InstallationTemplate.class);

    @Autowired
    private IMachineService machineService;

    @Autowired
    private IRedisService redisService;

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private INodeInfoService nodeInfoService;

    private static final int MAX_PORT = 65535;

    /**
     * 模板通用，调用方传入不同的策略
     *
     * @param installationOperation
     * @param installationParam
     * @return
     */
    public boolean installFlow(AbstractNodeOperation installationOperation, InstallationParam installationParam) {
        String clusterName = installationParam.getCluster().getClusterName();
        boolean verify = verify(installationParam);
        if (!verify) {
            return false;
        }
        // 数据准备
        InstallationWebSocketHandler.appendLog(clusterName, "Start preparing installation...");
        boolean prepareSuccess = prepareForInstallation(installationOperation, installationParam);
        if (!prepareSuccess) {
            return false;
        }
        InstallationWebSocketHandler.appendLog(clusterName, "Start pulling image...");
        boolean pullImageSuccess = pullImage(installationOperation, installationParam);
        // 拉取安装包
        if (!pullImageSuccess) {
            return false;
        }
        InstallationWebSocketHandler.appendLog(clusterName, "Start pulling redis.conf...");
        // 分发配置文件
        boolean pullConfigSuccess = pullConfig(installationOperation, installationParam);
        if (!pullConfigSuccess) {
            return false;
        }
        InstallationWebSocketHandler.appendLog(clusterName, "Start installing redis node...");
        // 节点安装
        boolean installSuccess = install(installationOperation, installationParam);
        if (!installSuccess) {
            return false;
        }
        InstallationWebSocketHandler.appendLog(clusterName, "Start initializing...");
        boolean initSuccess = init(installationParam);
        InstallationWebSocketHandler.appendLog(clusterName, "Start saving to database...");
        return saveToDB(installationParam);
    }

    /**
     * 校验参数
     *
     * @param installationParam
     * @return
     */
    private boolean verify(InstallationParam installationParam) {
        return true;
    }

    public boolean prepareForInstallation(AbstractNodeOperation installationOperation, InstallationParam installationParam) {
        // 获取机器列表
        buildMachineList(installationParam);
        // 构建集群拓扑图
        boolean buildTopologySuccess = buildTopology(installationParam);
        if (!buildTopologySuccess) {
            return false;
        }
        // 构建机器和Redis节点结构
        buildMachineRedisNodeMap(installationParam);
        // 检查安装环境
        boolean checkEnvironmentSuccess = installationOperation.checkInstallationEnv(installationParam);
        if (!checkEnvironmentSuccess) {
            return false;
        }
        boolean checkPortsSuccess = installationOperation.checkPorts(installationParam);
        if (!checkPortsSuccess) {
            return false;
        }
        return true;
    }

    public boolean pullImage(AbstractNodeOperation installationOperation, InstallationParam installationParam) {
        return installationOperation.pullImage(installationParam);
    }

    public boolean pullConfig(AbstractNodeOperation installationOperation, InstallationParam installationParam) {
        return installationOperation.pullConfig(installationParam);
    }

    public boolean install(AbstractNodeOperation installationOperation, InstallationParam installationParam) {
        return installationOperation.install(installationParam);
    }

    /**
     * 获取机器列表
     * 1. 选择机器
     * 2. 通过文本框获取(js 解析后传入后台)
     *
     * @param installationParam
     */
    public void buildMachineList(InstallationParam installationParam) {
        boolean autoBuild = installationParam.isAutoBuild();
        if (autoBuild) {
            List<Integer> machineIdList = installationParam.getMachineIdList();
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
    // Rest 调用
    public boolean buildTopology(InstallationParam installationParam) {
        boolean autoBuild = installationParam.isAutoBuild();
        String clusterName = installationParam.getCluster().getClusterName();
        Multimap<RedisNode, RedisNode> topology = ArrayListMultimap.create();
        if (autoBuild) {
            List<Machine> machineList = installationParam.getMachineList();
            int masterNumber = installationParam.getMasterNumber();
            int replicaNumber = installationParam.getReplicaNumber();
            int nodeNumber = masterNumber * (replicaNumber + 1);
            int port = installationParam.getStartPort();
            List<MachineAndPorts> machineAndPortsList = buildMachinePortsMap(clusterName, machineList, port, nodeNumber);
            topology = randomPolicy(machineAndPortsList, masterNumber, replicaNumber);
        } else {
            List<RedisNode> allRedisNodes = installationParam.getRedisNodeList();
            RedisNode masterNode = null;
            for (RedisNode redisNode : allRedisNodes) {
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
        List<RedisNode> allRedisNodes = new LinkedList<>();
        for (RedisNode masterNode : topology.keySet()) {
            allRedisNodes.add(masterNode);
            for (RedisNode redisNode : topology.get(masterNode)) {
                allRedisNodes.add(redisNode);
            }
        }
        installationParam.setRedisNodeList(allRedisNodes);
        return true;
    }

    /**
     * rest 调用
     *
     * @param machineAndPortsList machine, ports
     * @param masterNumber
     * @param replicaNumber
     * @return
     */
    public Multimap<RedisNode, RedisNode> randomPolicy(List<MachineAndPorts> machineAndPortsList, int masterNumber, int replicaNumber) {
        Map<RedisNode, List<RedisNode>> tempTopology = buildMasterNodes(machineAndPortsList, masterNumber);
        List<RedisNode> allSlaveNodes = new ArrayList<>();
        machineAndPortsList.forEach(machineAndPorts -> {
            Machine machine = machineAndPorts.getMachine();
            List<Integer> ports = machineAndPorts.getPorts();
            for (Integer port : ports) {
                allSlaveNodes.add(new RedisNode(machine.getHost(), port, SLAVE));
            }
        });
        tempTopology.forEach((masterNode, slaveNodeList) -> {
            if (slaveNodeList == null) {
                return;
            }
            while (slaveNodeList.size() < replicaNumber && !allSlaveNodes.isEmpty()) {
                Random random = new Random();
                int size = allSlaveNodes.size();
                int index = random.nextInt(size);
                RedisNode slaveNode = allSlaveNodes.get(index);
                slaveNodeList.add(slaveNode);
                allSlaveNodes.remove(index);
            }
        });
        return map2Multimap(tempTopology);
    }

    private Map<RedisNode, List<RedisNode>> buildMasterNodes(List<MachineAndPorts> machineAndPortsList, int masterNumber) {
        // 排序
        Map<RedisNode, List<RedisNode>> topology = new LinkedHashMap<>();
        while (topology.keySet().size() < masterNumber) {
            if (getPortNumber(machineAndPortsList) == 0) {
                break;
            }
            for (MachineAndPorts machineAndPorts : machineAndPortsList) {
                List<Integer> ports = machineAndPorts.getPorts();
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

    private Multimap<RedisNode, RedisNode> map2Multimap(Map<RedisNode, List<RedisNode>> redisNodeListMap) {
        Multimap<RedisNode, RedisNode> topology = ArrayListMultimap.create();
        redisNodeListMap.forEach((masterNode, slaveNodes) -> {
            slaveNodes.forEach(slaveNode -> {
                topology.put(masterNode, slaveNode);
            });
        });
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
     * @param clusterName
     * @param machineList
     * @param nodeNumber
     * @param port
     * @return
     */
    private List<MachineAndPorts> buildMachinePortsMap(String clusterName, List<Machine> machineList, int port, int nodeNumber) {
        List<MachineAndPorts> machineAndPortsList = new ArrayList<>();
        for (Machine machine : machineList) {
            List<Integer> ports = new ArrayList<>();
            String host = machine.getHost();
            while (nodeNumber > ports.size()) {
                if (port >= MAX_PORT) {
                    break;
                }
                try {
                    if (!NetworkUtil.telnet(host, port)) {
                        ports.add(port);
                    }
                } catch (Exception e) {
                    String message = "Check port failed, host: " + host + ", port: " + port;
                    InstallationWebSocketHandler.appendLog(clusterName, message);
                    InstallationWebSocketHandler.appendLog(clusterName, e.getMessage());
                    logger.error(message, e);
                }
                port++;
            }
            machineAndPortsList.add(new MachineAndPorts(machine, ports));
        }
        return machineAndPortsList;
    }

    /**
     * @param installationParam
     */
    public void buildMachineRedisNodeMap(InstallationParam installationParam) {
        Multimap<Machine, RedisNode> machineAndRedisNode = ArrayListMultimap.create();
        List<Machine> machineList = installationParam.getMachineList();
        List<RedisNode> allRedisNodes = installationParam.getRedisNodeList();
        for (Machine machine : machineList) {
            for (RedisNode redisNode : allRedisNodes) {
                if (Objects.equals(machine.getHost(), redisNode.getHost())) {
                    machineAndRedisNode.put(machine, redisNode);
                }
            }
        }
        installationParam.setMachineAndRedisNode(machineAndRedisNode);
    }

    /**
     * cluster master meet
     * slave build
     *
     * @param installationParam
     * @return
     */
    public boolean init(InstallationParam installationParam) {
        Cluster cluster = installationParam.getCluster();
        String clusterName = cluster.getClusterName();
        String redisPassword = cluster.getRedisPassword();
        cluster.setRedisPassword(null);

        Multimap<RedisNode, RedisNode> topology = installationParam.getTopology();
        RedisNode seed = getSeedNode(cluster, topology);
        List<RedisNode> allRedisNodes = installationParam.getRedisNodeList();
        String result = redisService.clusterMeet(cluster, seed, allRedisNodes);

        InstallationWebSocketHandler.appendLog(clusterName, "Cluster meet... ");
        InstallationWebSocketHandler.appendLog(clusterName,  result);

        List<RedisNode> redisNodeListWithInfo = waitClusterMeet(installationParam, seed, allRedisNodes);
        replicate(cluster, topology, redisNodeListWithInfo);

        // Set password
        if (!Strings.isNullOrEmpty(redisPassword)) {
            String updateRedisPasswordResult = updateRedisPassword(redisPassword, cluster);
            InstallationWebSocketHandler.appendLog(clusterName, updateRedisPasswordResult);
            logger.error(updateRedisPasswordResult);
        }
        boolean autoInit = installationParam.isAutoInit();
        if (autoInit) {
            String initResult = initSlot(cluster);
            if (Strings.isNullOrEmpty(initResult)) {
                cluster.setInitialized(true);
            }
        }
        return true;
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
        String clusterName = installationParam.getCluster().getClusterName();
        // 获取集群节点
        RedisClusterClient redisClusterClient = RedisClientFactory.buildRedisClusterClient(seed);
        List<RedisNode> redisNodeListWithInfo = new ArrayList<>();
        int size = redisNodeList.size();
        long timeout = 0;
        // TODO: 根据集群规模设置等待时长
        while (timeout < 5000 * size && timeout < 2 * ONE_MINUTE) {
            try {
                redisNodeListWithInfo = redisClusterClient.clusterNodes();
                int realSize = redisNodeListWithInfo.size();
                int needSize = redisNodeList.size();
                if (realSize == needSize) {
                    break;
                } else {
                    Thread.sleep(TEN_SECONDS);
                    timeout += TEN_SECONDS;
                }
            } catch (Exception e) {
                String message = "Wait for cluster meet error.";
                InstallationWebSocketHandler.appendLog(clusterName, message);
                logger.error(message, e);
            }
        }
        if (redisNodeListWithInfo.size() != redisNodeList.size()) {
            installationParam.setAutoInit(false);
            String message = "Topology is incorrect, real nodes = " + redisNodeListWithInfo + ", expectation nodes = " + redisNodeList;
            InstallationWebSocketHandler.appendLog(clusterName, message);
            logger.warn(message);
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

    /**
     * @param cluster
     * @param topology
     * @param redisNodeListWithInfo 包含 redis node 基本信息，如 node id
     */
    private void replicate(Cluster cluster, Multimap<RedisNode, RedisNode> topology, List<RedisNode> redisNodeListWithInfo) {
        Multimap<RedisNode, RedisNode> realTopology = ArrayListMultimap.create();
        // 将 master 替换掉
        for (Map.Entry<RedisNode, RedisNode> entry : topology.entries()) {
            RedisNode masterNode = entry.getKey();
            for (RedisNode redisNodeWithInfo : redisNodeListWithInfo) {
                if (Objects.equals(masterNode.getHost(), redisNodeWithInfo.getHost())
                        && masterNode.getPort() == redisNodeWithInfo.getPort()) {
                    realTopology.put(redisNodeWithInfo, entry.getValue());
                }
            }
        }
        realTopology.forEach((masterNodeWithInfo, slaveNode) -> {
            String replicateResult = redisService.clusterReplicate(cluster, masterNodeWithInfo, slaveNode);
            if (!Strings.isNullOrEmpty(replicateResult)) {
                InstallationWebSocketHandler.appendLog(cluster.getClusterName(), replicateResult);
                logger.error(replicateResult);
            }
        });
        try {
            Thread.sleep(FIVE_SECONDS);
        } catch (InterruptedException e) {
        }
    }

    private String updateRedisPassword(String redisPassword, Cluster cluster) {
        StringBuffer result = new StringBuffer();
        List<RedisNode> redisNodeList = redisService.getRedisNodeList(cluster);
        redisNodeList.forEach(redisNode -> {
            try {
                RedisClient redisClient = RedisClientFactory.buildRedisClient(redisNode);
                redisClient.setConfig(new Pair<>(MASTER_AUTH, redisPassword));
                redisClient.setConfig(new Pair<>(REQUIRE_PASS, redisPassword));
                redisClient.close();
                redisClient = RedisClientFactory.buildRedisClient(redisNode, redisPassword);
                redisClient.rewriteConfig();
                redisClient.close();

            } catch (Exception e) {
                String message = "Update redis password failed, host=" + redisNode.getHost() + ", port=" + redisNode.getPort() + ".";
                logger.error(message, e);
                result.append(message + e.getMessage());
            }
        });
        if (Strings.isNullOrEmpty(result.toString())) {
            cluster.setRedisPassword(redisPassword);
        }
        return result.toString();
    }

    private String generateConnectionNodes(Cluster cluster) {
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
        return nodes.toString();
    }

    /**
     * Slot distribution for redis cluster
     *
     * @param cluster
     * @return
     */
    private String initSlot(Cluster cluster) {
        List<RedisNode> masterNodeList = redisService.getRedisMasterNodeList(cluster);
        List<SlotBalanceUtil.Shade> balanceSlots = SlotBalanceUtil.balanceSlot(masterNodeList.size());
        Map<RedisNode, SlotBalanceUtil.Shade> masterNodeShadeMap = new LinkedHashMap<>();
        for (int i = 0; i < masterNodeList.size(); i++) {
            RedisNode redisNode = masterNodeList.get(i);
            SlotBalanceUtil.Shade shade = balanceSlots.get(i);
            masterNodeShadeMap.put(redisNode, shade);
        }
        return redisService.clusterAddSlotsBatch(cluster, masterNodeShadeMap);
    }

    /**
     * Standalone: node slave of
     *
     * @param installationParam
     * @return
     */
    private boolean buildStandalone(InstallationParam installationParam) {
        return false;
    }

    private boolean saveToDB(InstallationParam installationParam) {
        // 获取连接节点
        Cluster cluster = installationParam.getCluster();
        String nodes = generateConnectionNodes(cluster);
        cluster.setNodes(nodes);
        clusterService.addCluster(cluster);
        return true;
    }

    private static class MachineAndPorts {
        private Machine machine;

        private List<Integer> ports;

        public MachineAndPorts(Machine machine, List<Integer> ports) {
            this.machine = machine;
            this.ports = ports;
        }

        public Machine getMachine() {
            return machine;
        }

        public void setMachine(Machine machine) {
            this.machine = machine;
        }

        public List<Integer> getPorts() {
            return ports;
        }

        public void setPorts(List<Integer> ports) {
            this.ports = ports;
        }
    }

}
