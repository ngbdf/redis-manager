package com.newegg.ec.redis.plugin.install;

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
    public boolean install(InstallationOperation installationOperation, InstallationParam installationParam) {
        Cluster cluster = installationParam.getCluster();
        // TODO: 用于标记是否是新建集群
        String clusterId = cluster.getClusterId();
        List<RedisNode> redisNodeList = installationParam.getRedisNodeList();
        String machineGroup = installationParam.getMachineGroup();
        List<Machine> machineByMachineGroup = machineService.getMachineByMachineGroup(machineGroup);
        boolean checkEnvironmentPass = checkEnvironment(installationParam, installationOperation, machineByMachineGroup);
        if (!checkEnvironmentPass) {
            return false;
        }
        boolean pullImageSuccess = installationOperation.pullImage();
        if (!pullImageSuccess) {
            return false;
        }
        boolean buildRedisConfigSuccess = buildRedisConfig(installationOperation);
        if (!buildRedisConfigSuccess) {
            return false;
        }
        Map<RedisNode, List<RedisNode>> redisNodeListMap = buildTopology(installationParam, machineByMachineGroup);
        Set<RedisNode> redisMasterNodes = redisNodeListMap.keySet();
        if (redisMasterNodes.isEmpty()) {
            return false;
        }
        redisNodeList = getAllNode(installationParam, redisNodeListMap);
        boolean installSuccess = installationOperation.install(redisNodeList);
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
     * 检查安装环境：Machine资源、Docker 环境、Kubernetes 环境
     *
     * @param installationOperation
     * @param installationParam
     * @return
     */
    public boolean checkEnvironment(InstallationParam installationParam, InstallationOperation installationOperation, List<Machine> machineByMachineGroup) {
        //检查机器内存CPU资源
        // 不同安装方式的环境监测
        installationOperation.checkEnvironment();
        // 检查所有机器之间是否网络相通, n! 次
        return true;
    }


    public boolean buildRedisConfig(InstallationOperation installationOperation) {
        installationOperation.buildConfig();
        return true;
    }

    /**
     * 构建集群拓扑图
     *
     * @return
     */
    public Map<RedisNode, List<RedisNode>> buildTopology(InstallationParam installationParam, List<Machine> machineByMachineGroup) {
        Map<RedisNode, List<RedisNode>> clusterTopology = new HashMap<>();

        return clusterTopology;
    }

    /**
     * Get all node
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
     * @param redisNodeListMap
     * @return
     */
    public boolean buildCluster(Map<RedisNode, List<RedisNode>>  redisNodeListMap) {
        return false;
    }

    /**
     * Slot distribution for redis cluster
     * @param installationParam
     * @param redisMasterNodes
     * @return
     */
    public boolean initCluster(InstallationParam installationParam, Set<RedisNode> redisMasterNodes) {
        return true;
    }

    public boolean saveToDB(InstallationParam installationParam, Map<RedisNode, List<RedisNode>>  redisNodeListMap) {
        return true;
    }
}
