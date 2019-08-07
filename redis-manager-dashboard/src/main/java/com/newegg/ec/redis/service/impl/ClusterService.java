package com.newegg.ec.redis.service.impl;

import com.newegg.ec.redis.client.RedisClient;
import com.newegg.ec.redis.client.RedisClientFactory;
import com.newegg.ec.redis.client.RedisURI;
import com.newegg.ec.redis.dao.IClusterDao;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.util.RedisClusterUtil;
import com.newegg.ec.redis.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.HostAndPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.newegg.ec.redis.util.RedisNodeInfoUtil.*;

/**
 * @author Jay.H.Zou
 * @date 8/2/2019
 */
public class ClusterService implements IClusterService {

    private static final Logger logger = LoggerFactory.getLogger(ClusterService.class);

    @Autowired
    private IClusterDao clusterDao;

    @Override
    public List<Cluster> getAllClusterList() {
        List<Cluster> clusterList = new ArrayList<>();
        try {
            clusterList = clusterDao.selectAllCluster();
        } catch (Exception e) {
            logger.error("Get all cluster failed.", e);
        }
        return clusterList;
    }

    @Override
    public List<Cluster> getClusterListByGroupId(int groupId) {
        List<Cluster> clusterList = new ArrayList<>();
        try {
            return clusterDao.selectClusterByGroupId(groupId);
        } catch (Exception e) {
            logger.error("Get cluster list by group id failed, group id = " + groupId, e);
        }
        return clusterList;
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
    public boolean addCluster(Cluster cluster) {
        String nodes = cluster.getNodes();
        Set<HostAndPort> hostAndPortSet = RedisUtil.nodesToHostAndPortSet(nodes);
        RedisURI redisURI = new RedisURI(hostAndPortSet, cluster.getRedisPassword());
        RedisClient redisClient;
        try {
            redisClient = RedisClientFactory.buildRedisClient(redisURI);
            String clusterInfo = redisClient.getClusterInfo();
            Cluster clusterInfoObj = RedisClusterUtil.parseClusterInfoToObject(clusterInfo);
            fillClusterInfo(cluster, clusterInfoObj);
        } catch (Exception e) {
            logger.error("Fill cluster info failed, " + cluster, e);
            return false;
        }
        try {
            Map<String, String> infoMap = redisClient.getInfo(RedisClient.SERVER);
            fillNodeInfo(cluster, infoMap);
        } catch (Exception e) {
            logger.error("Fill node info failed, " + cluster, e);
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
            clusterDao.updateTotalKey(clusterId, cluster.getTotalKeys());
            clusterDao.updateTotalExpires(clusterId, cluster.getTotalExpires());
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

    private void fillClusterInfo(Cluster cluster, Cluster clusterInfoObj) {
        cluster.setClusterState(cluster.getClusterState());
        cluster.setClusterSlotsAssigned(clusterInfoObj.getClusterSlotsAssigned());
        cluster.setClusterSlotsFail(clusterInfoObj.getClusterSlotsPfail());
        cluster.setClusterSlotsPfail(clusterInfoObj.getClusterSlotsPfail());
        cluster.setClusterSlotsOk(clusterInfoObj.getClusterSlotsOk());
        cluster.setClusterSize(clusterInfoObj.getClusterSize());
        cluster.setClusterKnownNodes(clusterInfoObj.getClusterKnownNodes());
    }

    private void fillNodeInfo(Cluster cluster, Map<String, String> infoMap) {
        cluster.setOs(infoMap.get(OS));
        cluster.setRedisMode(infoMap.get(REDIS_MODE));
        cluster.setRedisVersion(infoMap.get(REDIS_VERSION));
    }
}
