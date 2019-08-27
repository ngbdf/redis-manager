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

import static com.newegg.ec.redis.util.SignUtil.COLON;
import static com.newegg.ec.redis.util.SignUtil.COMMAS;
import static javax.management.timer.Timer.ONE_MINUTE;
import static javax.management.timer.Timer.ONE_SECOND;

/**
 * 集群安装模板
 *
 * @author Jay.H.Zou
 * @date 2019/7/26
 */
public class InstallationTemplate {

    @Autowired
    private IMachineService machineService;

    @Autowired
    private IRedisService redisService;

    /**
     * 校验参数
     *
     * @param installationParam
     * @return
     */
    public boolean verify(InstallationParam installationParam) {

        return true;
    }

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
        Cluster cluster = installationParam.getCluster();
        // 用于 websocket
        String clusterName = cluster.getClusterName();
        // 获取机器列表
        buildMachineList(installationParam);
        // 构建集群拓扑图
        buildTopology(installationParam);
        // 检查机器列表和节点列表
        boolean checkResult = prepareCheck(installationParam);
        if (checkResult) {
            return false;
        }
        // 检查安装环境
        boolean checkEnvironmentPass = installationOperation.checkInstallationEnv(installationParam);
        if (!checkEnvironmentPass) {
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
        boolean buildClusterSuccess = buildCluster(installationParam);
        if (!buildClusterSuccess) {
            return false;
        }
        initSlot(cluster);
        return saveToDB(installationParam);
    }

    /**
     * 获取机器列表
     * 1. 选择机器
     * 2. 通过文本框获取
     *
     * @param installationParam
     */
    private void buildMachineList(InstallationParam installationParam) {
        List<String> machineIdList = installationParam.getMachineIdList();
        List<Machine> machineList = machineService.getMachineListByIds(machineIdList);
        installationParam.setMachineList(machineList);
    }

    /**
     * 构建集群拓扑图
     * 1. 用户自定义
     * 2. 系统生成
     *
     * @return
     */
    private void buildTopology(InstallationParam installationParam) {
        boolean autoBuild = installationParam.isAutoBuild();
        if (autoBuild) {

        }
        List<RedisNode> redisNodeList = installationParam.getRedisNodeList();
    }

    private boolean prepareCheck(InstallationParam installationParam) {
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
    private boolean buildCluster(InstallationParam installationParam) {
        Cluster cluster = installationParam.getCluster();
        Multimap<RedisNode, RedisNode> topology = installationParam.getTopology();
        RedisNode seed = getSeedNode(cluster, topology);
        List<RedisNode> redisNodeList = installationParam.getRedisNodeList();
        // TODO: websocket
        String result = redisService.clusterMeet(cluster, seed, redisNodeList);
        List<RedisNode> redisNodeListWithInfo = waitClusterMeet(installationParam, seed, redisNodeList);
        replicate(cluster, topology, redisNodeListWithInfo);
        // 获取连接节点
        generateConnectionNodes(cluster);
        // 设置密码
        String updateRedisPasswordResult = redisService.updateRedisPassword(cluster);
        boolean autoInit = installationParam.isAutoInit();
        if (autoInit) {
            initSlot(cluster);
        }
        return false;
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
        long tenSeconds = 10 * ONE_SECOND;
        // 超过一分钟则认为完成
        while (timeout < ONE_MINUTE) {
            try {
                redisNodeListWithInfo = redisClusterClient.clusterNodes();
                if (redisNodeListWithInfo.size() < redisNodeList.size()) {
                    Thread.sleep(tenSeconds);
                    timeout += tenSeconds;
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
            Thread.sleep(5 * ONE_SECOND);
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
        return true;
    }
}
