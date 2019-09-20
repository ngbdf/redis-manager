package com.newegg.ec.redis.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.client.RedisClient;
import com.newegg.ec.redis.client.RedisClientFactory;
import com.newegg.ec.redis.dao.IClusterDao;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.util.RedisClusterInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.newegg.ec.redis.client.RedisClient.SERVER;
import static com.newegg.ec.redis.util.RedisNodeInfoUtil.*;
import static com.newegg.ec.redis.util.RedisUtil.*;

/**
 * @author Jay.H.Zou
 * @date 8/2/2019
 */
public class ClusterService implements IClusterService {

    private static final Logger logger = LoggerFactory.getLogger(ClusterService.class);

    @Autowired
    private IClusterDao clusterDao;

    @Autowired
    private IRedisService redisService;

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
    public List<Cluster> getClusterListByGroupId(int groupId) {
        try {
            return clusterDao.selectClusterByGroupId(groupId);
        } catch (Exception e) {
            logger.error("Get cluster list by group id failed, group id = " + groupId, e);
            return null;
        }
    }

    @Override
    public Cluster getClusterById(int clusterId) {
        try {
            return clusterDao.selectClusterById(clusterId);
        } catch (Exception e) {
            logger.error("Get cluster by id failed, cluster id = " + clusterId, e);
            return null;
        }
    }

    @Override
    public Cluster getClusterByName(String clusterName) {
        if (Strings.isNullOrEmpty(clusterName)) {
            return null;
        }
        try {
            return clusterDao.selectClusterByName(clusterName);
        } catch (Exception e) {
            logger.error("Get cluster by name failed, cluster name = " + clusterName, e);
            return new Cluster();
        }
    }

    /**
     * 不仅需要
     *
     * @param cluster
     * @return
     */
    @Override
    public boolean addCluster(Cluster cluster) {
        String redisMode;
        try {
            fillBaseInfo(cluster);
            redisMode = cluster.getRedisMode();
            if (redisMode == null) {
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
        } catch (Exception e) {
            logger.error("Fill base info failed, " + cluster, e);
            return false;
        }
        try {
            int row = clusterDao.insertCluster(cluster);
            return row > 0;
        } catch (Exception e) {
            logger.error("Save cluster failed, " + cluster, e);
            return false;
        }
    }

    @Override
    public boolean updateCluster(Cluster cluster) {
        return false;
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

    /**
     * drop node_info_{clusterId}
     * delete redis_node in this cluster
     *
     * @param cluster
     * @return
     */
    @Override
    public boolean deleteCluster(Cluster cluster) {
        return false;
    }

    /**
     * @param cluster
     * @return
     */
    public boolean fillClusterInfo(Cluster cluster) {
        try {
            Map<String, String> clusterInfo = redisService.getClusterInfo(cluster);
            if (clusterInfo == null) {
                logger.warn("Cluster info is empty.");
                return true;
            }
            Cluster clusterInfoObj = RedisClusterInfoUtil.parseClusterInfoToObject(clusterInfo);
            cluster.setClusterStatus(clusterInfoObj.getClusterStatus());
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

    public boolean fillStandaloneInfo(Cluster cluster) {
        List<RedisNode> redisNodeList = redisService.getRedisNodeList(cluster);
        cluster.setClusterSize(1);
        cluster.setClusterKnownNodes(redisNodeList.size());
        return true;
    }

    public void fillBaseInfo(Cluster cluster) {

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

    public void fillKeyspaceInfo(Cluster cluster) {
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

}
