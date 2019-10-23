package com.newegg.ec.redis.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.client.RedisClient;
import com.newegg.ec.redis.client.RedisClientFactory;
import com.newegg.ec.redis.dao.IClusterDao;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.service.AbstractNodeOperation;
import com.newegg.ec.redis.plugin.install.service.impl.DockerNodeOperation;
import com.newegg.ec.redis.plugin.install.service.impl.MachineNodeOperation;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.INodeInfoService;
import com.newegg.ec.redis.service.IRedisNodeService;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.util.RedisClusterInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.newegg.ec.redis.client.RedisClient.SERVER;
import static com.newegg.ec.redis.plugin.install.entity.InstallationEnvironment.DOCKER;
import static com.newegg.ec.redis.plugin.install.entity.InstallationEnvironment.MACHINE;
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
     * @param cluster
     * @return
     */
    @Transactional
    @Override
    public boolean addCluster(Cluster cluster) {
        boolean fillResult = fillCluster(cluster);
        if (!fillResult) {
            return false;
        }
        int row = clusterDao.insertCluster(cluster);
        if (row == 0) {
            throw new RuntimeException("Save cluster failed.");
        }
        nodeInfoService.createNodeInfoTable(cluster.getClusterId());
        return true;
    }

    @Override
    public boolean updateCluster(Cluster cluster) {
        try {
            fillCluster(cluster);
            int row = clusterDao.updateCluster(cluster);
            return row > 0;
        } catch (Exception e) {
            logger.error("Update cluster failed.", e);
            return false;
        }
    }

    @Override
    public boolean updateClusterKeys(Cluster cluster) {
        int clusterId = cluster.getClusterId();
        try {
            clusterDao.updateKeyspace(clusterId, cluster.getTotalKeys(), cluster.getTotalExpires());
            return true;
        } catch (Exception e) {
            logger.error("Update cluster keys failed, " + cluster, e);
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
            logger.error("Update cluster nodes failed, " + cluster, e);
            return false;
        }
    }

    @Override
    public boolean updateClusterRuleIds(Cluster cluster) {
        try {
            clusterDao.updateClusterRuleIds(cluster.getClusterId(), cluster.getRuleIds());
            return true;
        } catch (Exception e) {
            logger.error("Update cluster rule ids failed.", e);
            return false;
        }
    }

    @Override
    public boolean updateClusterChannelIds(Cluster cluster) {
        try {
            clusterDao.updateClusterChannelIds(cluster.getClusterId(), cluster.getChannelIds());
            return true;
        } catch (Exception e) {
            logger.error("Update cluster channel ids failed.", e);
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
        clusterDao.deleteClusterById(clusterId);
        nodeInfoService.deleteNodeInfoTable(clusterId);
        redisNodeService.deleteRedisNodeListByClusterId(clusterId);
        return true;
    }

    private boolean fillCluster(Cluster cluster) {
        fillBaseInfo(cluster);
        String redisMode = cluster.getRedisMode();
        if (Strings.isNullOrEmpty(redisMode)) {
            logger.error("Fill base info failed, " + cluster);
            return false;
        }
        fillKeyspaceInfo(cluster);
        if (Objects.equals(redisMode, CLUSTER)) {
            if (!fillClusterInfo(cluster)) {
                logger.error("Fill cluster info failed, " + cluster);
                return false;
            }
        } else if (Objects.equals(redisMode, STANDALONE)) {
            fillStandaloneInfo(cluster);
        }
        return true;
    }

    /**
     * @param cluster
     * @return
     */
    private boolean fillClusterInfo(Cluster cluster) {
        try {
            Map<String, String> clusterInfo = redisService.getClusterInfo(cluster);
            if (clusterInfo == null) {
                logger.warn("Cluster info is empty.");
                return true;
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
            logger.error("Fill cluster info failed, " + cluster, e);
            return false;
        }
    }

    private boolean fillStandaloneInfo(Cluster cluster) {
        List<RedisNode> redisNodeList = redisService.getRedisNodeList(cluster);
        cluster.setClusterSize(1);
        cluster.setClusterKnownNodes(redisNodeList.size());
        cluster.setClusterState(Cluster.ClusterState.HEALTH);
        cluster.setClusterSlotsAssigned(1);
        cluster.setClusterSlotsFail(1);
        cluster.setClusterSlotsPfail(1);
        cluster.setClusterSlotsOk(1);

        return true;
    }

    private void fillBaseInfo(Cluster cluster) {

        try {
            String nodes = cluster.getNodes();
            RedisClient redisClient = RedisClientFactory.buildRedisClient(nodesToHostAndPort(nodes), cluster.getRedisPassword());
            Map<String, String> serverInfo = redisClient.getInfo(SERVER);
            // Server
            cluster.setOs(serverInfo.get(OS));
            cluster.setRedisMode(serverInfo.get(REDIS_MODE));
            cluster.setRedisVersion(serverInfo.get(REDIS_VERSION));
        } catch (Exception e) {
            logger.error("Fill redis base info failed, " + cluster, e);
        }
    }

    private void fillKeyspaceInfo(Cluster cluster) {
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
    }

    @Override
    public AbstractNodeOperation getNodeOperation(Integer installationEnvironment) {
        switch (installationEnvironment) {
            case DOCKER:
                return dockerNodeOperation;
            case MACHINE:
                return machineNodeOperation;
            default:
                return null;
        }
    }

}
