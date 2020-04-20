package com.newegg.ec.redis.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.dao.IClusterDao;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.entity.SentinelMaster;
import com.newegg.ec.redis.plugin.install.service.AbstractNodeOperation;
import com.newegg.ec.redis.plugin.install.service.impl.DockerNodeOperation;
import com.newegg.ec.redis.plugin.install.service.impl.HumpbackNodeOperation;
import com.newegg.ec.redis.plugin.install.service.impl.MachineNodeOperation;
import com.newegg.ec.redis.service.*;
import com.newegg.ec.redis.util.NetworkUtil;
import com.newegg.ec.redis.util.RedisClusterInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.HostAndPort;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.newegg.ec.redis.client.RedisClient.SENTINEL;
import static com.newegg.ec.redis.client.RedisClient.SERVER;
import static com.newegg.ec.redis.plugin.install.entity.InstallationEnvironment.*;
import static com.newegg.ec.redis.util.RedisNodeInfoUtil.*;
import static com.newegg.ec.redis.util.RedisUtil.*;

/**
 * @author Jay.H.Zou
 * @date 8/2/2019
 */
@Service
public class ClusterService implements IClusterService {

    private static final Logger logger = LoggerFactory.getLogger(ClusterService.class);

    @Autowired
    private IClusterDao clusterDao;

    @Autowired
    private IRedisService redisService;

    @Autowired
    private INodeInfoService nodeInfoService;

    @Autowired
    private IRedisNodeService redisNodeService;

    @Autowired
    private DockerNodeOperation dockerNodeOperation;

    @Autowired
    private MachineNodeOperation machineNodeOperation;

    @Autowired
    private HumpbackNodeOperation humpbackNodeOperation;

    @Autowired
    private ISentinelMastersService sentinelMastersService;

    @Override
    public List<Cluster> getAllClusterList() {
        try {
            return clusterDao.selectAllCluster();
        } catch (Exception e) {
            logger.error("Get all cluster failed.", e);
            return null;
        }
    }

    @Override
    public List<Cluster> getClusterListByGroupId(Integer groupId) {
        try {
            return clusterDao.selectClusterByGroupId(groupId);
        } catch (Exception e) {
            logger.error("Get cluster list by group id failed, group id = " + groupId, e);
            return null;
        }
    }

    @Override
    public Cluster getClusterById(Integer clusterId) {
        try {
            return clusterDao.selectClusterById(clusterId);
        } catch (Exception e) {
            logger.error("Get cluster by id failed, cluster id = " + clusterId, e);
            return null;
        }
    }

    @Override
    public Cluster getClusterByIdAndGroup(Integer groupId, Integer clusterId) {
        try {
            return clusterDao.getClusterByIdAndGroup(groupId, clusterId);
        } catch (Exception e) {
            logger.error("Get cluster failed, cluster id = " + clusterId, e);
            return null;
        }
    }

    @Override
    public Cluster getClusterByName(String clusterName) {
        try {
            return clusterDao.selectClusterByName(clusterName);
        } catch (Exception e) {
            logger.error("Get cluster by name failed, cluster name = " + clusterName, e);
            return new Cluster();
        }
    }

    /**
     * Complete cluster info
     *
     * @param cluster
     * @return
     */
    @Override
    public Cluster completeClusterInfo(Cluster cluster) {
        // reset cluster state
        cluster.setClusterState(Cluster.ClusterState.HEALTH);
        boolean fillBaseInfoResult = fillBaseInfo(cluster);
        if (!fillBaseInfoResult) {
            logger.error("Fill base info failed, cluster name = " + cluster.getClusterName());
            cluster.setClusterState(Cluster.ClusterState.BAD);
        }
        String redisMode = cluster.getRedisMode();
        if (Strings.isNullOrEmpty(redisMode) || Objects.equals(Cluster.UNKNOWN_VALUE, redisMode)) {
            logger.error("Can't get redis mode, cluster name = " + cluster.getClusterName() + ", redis mode = " + redisMode);
            return cluster;
        }
        boolean fillInfoResult = false;
        if (!Objects.equals(redisMode, REDIS_MODE_SENTINEL)) {
            fillTotalData(cluster);
        }
        if (Objects.equals(redisMode, REDIS_MODE_CLUSTER)) {
            fillInfoResult = fillInfoForCluster(cluster);
        } else if (Objects.equals(redisMode, REDIS_MODE_STANDALONE)) {
            fillInfoResult = fillInfoForStandalone(cluster);
        } else if (Objects.equals(redisMode, REDIS_MODE_SENTINEL)) {
            fillInfoResult = fillInfoForSentinel(cluster);
        }
        if (!fillInfoResult) {
            logger.error("Complete cluster info failed, cluster name = " + cluster.getClusterName());
            cluster.setClusterState(Cluster.ClusterState.BAD);
        }
        return cluster;
    }


    /**
     * Add cluster to database
     *
     * @param cluster
     * @return
     */
    @Transactional
    @Override
    public boolean addCluster(Cluster cluster) {
        final Cluster completedCluster = completeClusterInfo(cluster);
        String redisMode = completedCluster.getRedisMode();
        if (Strings.isNullOrEmpty(redisMode) || Objects.equals(Cluster.UNKNOWN_VALUE, redisMode)) {
            logger.error("Can't get redis mode, cluster name = " + cluster.getClusterName() + ", redis mode = " + redisMode);
            return false;
        }
        int row = clusterDao.insertCluster(completedCluster);
        if (row == 0) {
            return false;
        }
        nodeInfoService.createNodeInfoTable(completedCluster.getClusterId());
        if (Objects.equals(redisMode, REDIS_MODE_SENTINEL)) {
            List<SentinelMaster> sentinelMasters = redisService.getSentinelMasters(completedCluster);
            sentinelMasters.forEach(sentinelMaster -> {
                sentinelMaster.setClusterId(completedCluster.getClusterId());
                sentinelMaster.setGroupId(completedCluster.getGroupId());
                sentinelMastersService.addSentinelMaster(sentinelMaster);
            });
        }
        return true;
    }

    @Override
    public boolean updateClusterMeta(Cluster cluster) {
        try {
            return clusterDao.updateClusterMeta(cluster) > 0;
        } catch (Exception e) {
            logger.error("Update cluster failed, cluster name = " + cluster.getClusterName(), e);
            return false;
        }
    }

    @Override
    public boolean updateCluster(Cluster cluster) {
        try {
            return clusterDao.updateCluster(cluster) > 1;
        } catch (Exception e) {
            logger.error("Update cluster state failed, cluster name = " + cluster.getClusterName(), e);
            return false;
        }
    }

    @Override
    public boolean updateNodes(Cluster cluster) {
        Integer clusterId = cluster.getClusterId();
        try {
            clusterDao.updateNodes(clusterId, cluster.getNodes());
            return true;
        } catch (Exception e) {
            logger.error("Update cluster nodes failed, cluster name = " + cluster.getClusterName(), e);
            return false;
        }
    }

    @Override
    public boolean updateClusterRuleIds(Cluster cluster) {
        try {
            clusterDao.updateClusterRuleIds(cluster.getClusterId(), cluster.getRuleIds());
            return true;
        } catch (Exception e) {
            logger.error("Update cluster rule ids failed, cluster name = " + cluster.getClusterName(), e);
            return false;
        }
    }

    @Override
    public boolean updateClusterChannelIds(Cluster cluster) {
        try {
            clusterDao.updateClusterChannelIds(cluster.getClusterId(), cluster.getChannelIds());
            return true;
        } catch (Exception e) {
            logger.error("Update cluster channel ids failed, cluster name = " + cluster.getClusterName(), e);
            return false;
        }
    }

    /**
     * drop node_info_{clusterId}
     * delete redis_node in this cluster
     *
     * @param clusterId
     * @return
     */
    @Transactional
    @Override
    public boolean deleteCluster(Integer clusterId) {
        if (clusterId == null) {
            return false;
        }
        Cluster cluster = getClusterById(clusterId);
        String redisMode = cluster.getRedisMode();
        if (REDIS_MODE_SENTINEL.equalsIgnoreCase(redisMode)) {
            sentinelMastersService.deleteSentinelMasterByClusterId(clusterId);
        }
        clusterDao.deleteClusterById(clusterId);
        nodeInfoService.deleteNodeInfoTable(clusterId);
        redisNodeService.deleteRedisNodeListByClusterId(clusterId);
        return true;
    }

    /**
     * @param cluster
     * @return
     */
    private boolean fillInfoForCluster(Cluster cluster) {
        try {
            Map<String, String> clusterInfo = redisService.getClusterInfo(cluster);
            if (clusterInfo == null) {
                logger.error("Cluster info is empty, cluster name = " + cluster.getClusterName());
                return false;
            }
            Cluster clusterInfoObj = RedisClusterInfoUtil.parseClusterInfoToObject(clusterInfo);
            cluster.setClusterState(clusterInfoObj.getClusterState());
            cluster.setClusterSlotsAssigned(clusterInfoObj.getClusterSlotsAssigned());
            cluster.setClusterSlotsFail(clusterInfoObj.getClusterSlotsPfail());
            cluster.setClusterSlotsPfail(clusterInfoObj.getClusterSlotsPfail());
            cluster.setClusterSlotsOk(clusterInfoObj.getClusterSlotsOk());
            cluster.setClusterSize(clusterInfoObj.getClusterSize());
            cluster.setClusterKnownNodes(clusterInfoObj.getClusterKnownNodes());
            return true;
        } catch (Exception e) {
            logger.error("Parse cluster info to object failed, cluster name = " + cluster.getClusterName(), e);
            return false;
        }
    }

    private boolean fillInfoForStandalone(Cluster cluster) {
        List<RedisNode> redisNodeList = redisService.getRealRedisNodeList(cluster);
        if (redisNodeList.isEmpty()) {
            return false;
        }
        cluster.setClusterSize(1);
        cluster.setClusterKnownNodes(redisNodeList.size());
        return true;
    }

    /**
     * sentinel_ok
     * sentinel_masters
     * master_ok
     *
     * @param cluster
     */
    private boolean fillInfoForSentinel(Cluster cluster) {
        try {
            List<RedisNode> redisNodeList = redisService.getRealRedisNodeList(cluster);
            cluster.setClusterKnownNodes(redisNodeList.size());
            cluster.setSentinelOk(redisNodeList.size());
            Set<HostAndPort> hostAndPorts = nodesToHostAndPortSet(cluster.getNodes());
            Map<String, String> sentinelInfo = null;
            for (HostAndPort hostAndPort : hostAndPorts) {
                sentinelInfo = redisService.getNodeInfo(hostAndPort, null, SENTINEL);
                if (sentinelInfo != null) {
                    break;
                }
            }
            // 无法获取 sentinel info
            if (sentinelInfo == null) {
                return false;
            }
            Integer sentinelMasters = Integer.parseInt(sentinelInfo.get(SENTINEL_MASTERS));
            cluster.setSentinelMasters(sentinelMasters);
            AtomicInteger masterOk = new AtomicInteger();
            sentinelInfo.forEach((key, val) -> {
                // master0:name=master,status=ok,address=10.16.50.219:16379,slaves=3,sentinels=6
                if (key.startsWith(MASTER_PREFIX) && val.contains("ok")) {
                    masterOk.getAndIncrement();
                }
            });
            cluster.setMasterOk(masterOk.get());
            cluster.setSentinelOk(getSentinelOkNumber(hostAndPorts));
            return true;
        } catch (Exception e) {
            logger.error("Fill redis base info failed, cluster name = " + cluster.getClusterName(), e);
            return false;
        }

    }

    /**
     * 判断 sentinel node 是否 down 掉
     *
     * @param hostAndPorts
     * @return
     */
    private int getSentinelOkNumber(Set<HostAndPort> hostAndPorts) {
        int sentinelOk = 0;
        for (HostAndPort hostAndPort : hostAndPorts) {
            boolean telnet = NetworkUtil.telnet(hostAndPort.getHost(), hostAndPort.getPort());
            if (telnet) {
                sentinelOk++;
            } else {
                logger.error("Sentinel node is down, please check, " + hostAndPort);
            }
        }
        return sentinelOk;
    }

    /**
     * 注意：默认所有节点的版本一致
     * 寻找一个种子节点获取集群的基本信息
     *
     * @param cluster
     */
    private boolean fillBaseInfo(Cluster cluster) {
        String nodes = cluster.getNodes();
        Set<HostAndPort> hostAndPorts = nodesToHostAndPortSet(nodes);
        Map<String, String> serverInfo = null;
        for (HostAndPort hostAndPort : hostAndPorts) {
            serverInfo = redisService.getNodeInfo(hostAndPort, cluster.getRedisPassword(), SERVER);
            if (serverInfo != null) {
                break;
            }
        }
        if (serverInfo == null) {
            logger.error("Fill redis base info failed, cluster name = " + cluster.getClusterName());
            return false;
        }
        cluster.setOs(serverInfo.get(OS));
        cluster.setRedisMode(serverInfo.get(REDIS_MODE));
        cluster.setRedisVersion(serverInfo.get(REDIS_VERSION));
        return true;
    }

    /**
     * total keys
     * total expires
     * total used memory
     *
     * @param cluster
     */
    private void fillTotalData(Cluster cluster) {
        Map<String, Map<String, Long>> keyspaceInfoMap = redisService.getKeyspaceInfo(cluster);
        cluster.setDbSize(keyspaceInfoMap.size());
        long totalKeys = 0;
        long totalExpires = 0;
        for (Map<String, Long> value : keyspaceInfoMap.values()) {
            totalKeys += value.get(KEYS);
            totalExpires += value.get(EXPIRES);
        }
        cluster.setTotalKeys(totalKeys);
        cluster.setTotalExpires(totalExpires);
        Long totalMemory = redisService.getTotalMemoryInfo(cluster);
        cluster.setTotalUsedMemory(totalMemory);
    }

    @Override
    public AbstractNodeOperation getNodeOperation(Integer installationEnvironment) {
        switch (installationEnvironment) {
            case DOCKER:
                return dockerNodeOperation;
            case MACHINE:
                return machineNodeOperation;
            case HUMPBACK:
                return humpbackNodeOperation;
            default:
                return null;
        }
    }

}
