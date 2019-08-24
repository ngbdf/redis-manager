package com.newegg.ec.redis.plugin.install.service;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.service.IMachineService;
import com.newegg.ec.redis.service.IRedisService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

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
     * 模板通用，调用方传入不同的策略
     *
     * @param installationOperation
     * @param installationParam
     * @return
     */
    public boolean install(AbstractInstallationOperation installationOperation, InstallationParam installationParam) {
        Cluster cluster = installationParam.getCluster();
        // 用于 websocket
        String clusterName = cluster.getClusterName();
        getMachineList(installationParam);
        boolean checkEnvironmentPass = checkEnvironment(installationOperation, installationParam);
        if (!checkEnvironmentPass) {
            return false;
        }
        boolean pullImageSuccess = installationOperation.pullImage(installationParam);
        if (!pullImageSuccess) {
            return false;
        }
        boolean buildRedisConfigSuccess = buildRedisConfig(installationOperation, installationParam);
        if (!buildRedisConfigSuccess) {
            return false;
        }
        Map<RedisNode, List<RedisNode>> redisNodeListMap = buildTopology(installationParam);
        Set<RedisNode> redisMasterNodes = redisNodeListMap.keySet();
        if (redisMasterNodes.isEmpty()) {
            return false;
        }
        boolean installSuccess = installationOperation.install(installationParam);
        if (!installSuccess) {
            return false;
        }
        boolean buildClusterSuccess = buildCluster(redisNodeListMap);
        if (!buildClusterSuccess) {
            return false;
        }
        boolean initClusterSuccess = initCluster(installationParam, redisMasterNodes);
        if (!initClusterSuccess) {
            // TODO: logger, websocket
        }
        return saveToDB(installationParam, redisNodeListMap);
    }

    /**
     * 获取机器列表
     * 1. 选择机器
     * 2. 通过文本框获取
     * @param installationParam
     */
    private void getMachineList(InstallationParam installationParam) {
        List<String> machineIdList = installationParam.getMachineIdList();
        List<Machine> machineList = machineService.getMachineListByIds(machineIdList);
        installationParam.setMachineList(machineList);
    }

    /**
     * 检查安装环境：Machine资源、Docker 环境、Kubernetes 环境
     *
     * @param installationOperation
     * @param installationParam
     * @return
     */
    public boolean checkEnvironment(AbstractInstallationOperation installationOperation, InstallationParam installationParam) {
        //检查机器内存CPU资源
        // 不同安装方式的环境监测
        return installationOperation.checkInstallationEnv(installationParam);
    }


    public boolean buildRedisConfig(AbstractInstallationOperation installationOperation, InstallationParam installationParam) {
        installationOperation.buildConfig(installationParam);
        return true;
    }

    /**
     * 构建集群拓扑图
     *
     * @return
     */
    public Map<RedisNode, List<RedisNode>> buildTopology(InstallationParam installationParam) {
        Map<RedisNode, List<RedisNode>> clusterTopology = new HashMap<>();

        return clusterTopology;
    }

    /**
     * Get all node
     *
     * @param installationParam
     * @param redisNodeListMap
     * @return
     */
    public List<RedisNode> getAllNode(InstallationParam installationParam, Map<RedisNode, List<RedisNode>> redisNodeListMap) {
        boolean autoBuild = installationParam.isAutoBuild();
        if (!autoBuild) {
            return installationParam.getRedisNodeList();
        }
        List<RedisNode> redisNodeList = new ArrayList<>();
        Set<Map.Entry<RedisNode, List<RedisNode>>> entries = redisNodeListMap.entrySet();
        if (entries.isEmpty()) {
            return redisNodeList;
        }
        Iterator<Map.Entry<RedisNode, List<RedisNode>>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<RedisNode, List<RedisNode>> next = iterator.next();
            redisNodeList.add(next.getKey());
            redisNodeList.addAll(next.getValue());
        }
        return redisNodeList;
    }

    /**
     * cluster master meet
     * slave build
     *
     * @param redisNodeListMap
     * @return
     */
    public boolean buildCluster(Map<RedisNode, List<RedisNode>> redisNodeListMap) {
        return false;
    }

    /**
     * standalone node meet
     * slave build
     *
     * @param redisNodeListMap
     * @return
     */
    public boolean buildStandalone(Map<RedisNode, List<RedisNode>> redisNodeListMap) {
        return false;
    }

    /**
     * Slot distribution for redis cluster
     *
     * @param installationParam
     * @param redisMasterNodes
     * @return
     */
    public boolean initCluster(InstallationParam installationParam, Set<RedisNode> redisMasterNodes) {
        return true;
    }

    public boolean saveToDB(InstallationParam installationParam, Map<RedisNode, List<RedisNode>> redisNodeListMap) {
        return true;
    }
}
