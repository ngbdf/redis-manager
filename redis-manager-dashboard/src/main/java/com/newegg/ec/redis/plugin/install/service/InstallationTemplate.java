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

/**
 * 集群安装模板
 * <p>
 * 机器列表：
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
        // TODO: 前台校验 masterNumber + replicationNumber < startPort - endPort + 1
        int masterNumber = installationParam.getMasterNumber();
        int replicationNumber = installationParam.getReplicationNumber();
        Multimap<RedisNode, RedisNode> topology = ArrayListMultimap.create();
        if (autoBuild) {
            int nodeNumber = masterNumber + replicationNumber;
            // TODO: check 端口可用
            Multimap<Machine, Integer> machinePortMap = buildMachinePortMap(installationParam);
            Map<Machine, Collection<Integer>> machinePortCollection = machinePortMap.asMap();
            for (Map.Entry<Machine, Collection<Integer>> machineCollectionEntry : machinePortCollection.entrySet()) {
                if (machineCollectionEntry.getValue().size() < nodeNumber) {
                    // TODO: websocket 端口不够用了
                    return false;
                }
            }
            String policy = installationParam.getPolicy();
            if (Objects.equals(policy, CIRCLE_POLICY)) {
                topology = circlePolicy(installationParam);
            } else {
                topology = randomPolicy(installationParam);
            }
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

    private Multimap<RedisNode, RedisNode> randomPolicy(InstallationParam installationParam) {
        return null;
    }

    private Multimap<RedisNode, RedisNode> circlePolicy(InstallationParam installationParam) {
        return null;
    }

    /**
     * Check port
     *
     * @param installationParam
     * @return
     */
    private Multimap<Machine, Integer> buildMachinePortMap(InstallationParam installationParam) {
        int startPort = installationParam.getStartPort();
        int endPort = installationParam.getEndPort();
        List<Machine> machineList = installationParam.getMachineList();
        Multimap<Machine, Integer> machinePortMap = ArrayListMultimap.create();
        for (Machine machine : machineList) {
            for (int port = startPort; port <= endPort; port++) {
                try {
                    if (!NetworkUtil.telnet(machine.getHost(), port)) {
                        machinePortMap.put(machine, port);
                    } else {
                        // TODO: websocket 告知用户哪台机器的哪个端口被占用
                    }
                } catch (Exception e) {
                    // TODO: websocket
                }
            }
        }
        return machinePortMap;
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
}
