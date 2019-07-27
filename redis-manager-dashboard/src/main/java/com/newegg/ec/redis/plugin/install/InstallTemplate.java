package com.newegg.ec.redis.plugin.install;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallParam;
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
public class InstallTemplate {

    @Autowired
    private IMachineService machineService;

    @Autowired
    private IRedisService redisService;

    /**
     * 模板通用，调用方传入不同的策略
     *
     * @param installOperation
     * @param installParam
     * @return
     */
    public boolean install(InstallOperation installOperation, InstallParam installParam) {
        Cluster cluster = installParam.getCluster();
        // TODO: 用于标记是否是新建集群
        String clusterId = cluster.getClusterId();
        List<RedisNode> redisNodeList = installParam.getRedisNodeList();
        String machineGroup = installParam.getMachineGroup();
        List<Machine> machineByMachineGroup = machineService.getMachineByMachineGroup(machineGroup);
        boolean checkEnvironmentPass = checkEnvironment(installParam, installOperation, machineByMachineGroup);
        if (!checkEnvironmentPass) {
            return false;
        }
        boolean pullImageSuccess = installOperation.pullImage();
        if (!pullImageSuccess) {
            return false;
        }
        boolean changeRedisConfSuccess = changeRedisConf();
        if (!changeRedisConfSuccess) {
            return false;
        }
        Map<RedisNode, List<RedisNode>> redisNodeListMap = buildTopology(installParam, machineByMachineGroup);
        Set<RedisNode> redisMasterNodes = redisNodeListMap.keySet();
        if (redisMasterNodes.isEmpty()) {
            return false;
        }
        redisNodeList = getAllNode(installParam, redisNodeListMap);
        boolean installSuccess = installOperation.install(redisNodeList);
        if (!installSuccess) {
            return false;
        }
        boolean buildClusterSuccess = buildCluster(redisNodeListMap);
        if (!buildClusterSuccess) {
            return false;
        }
        boolean initClusterSuccess = initCluster(installParam, redisMasterNodes);
        if (!initClusterSuccess) {
            // TODO: logger, websocket
        }
        return saveToDB(installParam, redisNodeListMap);
    }

    /**
     * 检查安装环境：Machine资源、Docker 环境、Kubernetes 环境
     *
     * @param installOperation
     * @param installParam
     * @return
     */
    public boolean checkEnvironment(InstallParam installParam, InstallOperation installOperation, List<Machine> machineByMachineGroup) {
        //检查机器内存CPU资源
        // 不同安装方式的环境监测
        installOperation.checkEnvironment();
        // 检查所有机器之间是否网络相通, n! 次

        return true;
    }

    /**
     * 更改安装前需要更改的参数，path,port等
     *
     * @return
     */
    public boolean changeRedisConf() {
        return true;
    }

    /**
     * 构建集群拓扑图
     *
     * @return
     */
    public Map<RedisNode, List<RedisNode>> buildTopology(InstallParam installParam, List<Machine> machineByMachineGroup) {
        Map<RedisNode, List<RedisNode>> clusterTopology = new HashMap<>();

        return clusterTopology;
    }

    /**
     * Get all node
     * @param installParam
     * @param redisNodeListMap
     * @return
     */
    public List<RedisNode> getAllNode(InstallParam installParam, Map<RedisNode, List<RedisNode>> redisNodeListMap) {
        boolean autoBuild = installParam.isAutoBuild();
        if (!autoBuild) {
            return installParam.getRedisNodeList();
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
     * @param installParam
     * @param redisMasterNodes
     * @return
     */
    public boolean initCluster(InstallParam installParam, Set<RedisNode> redisMasterNodes) {
        return true;
    }

    public boolean saveToDB(InstallParam installParam, Map<RedisNode, List<RedisNode>>  redisNodeListMap) {
        return true;
    }
}
