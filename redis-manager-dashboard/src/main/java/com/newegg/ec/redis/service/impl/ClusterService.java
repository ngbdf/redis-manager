package com.newegg.ec.redis.service.impl;

import com.newegg.ec.redis.dao.IClusterDao;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.service.IClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

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
    public boolean saveCluster(Cluster cluster) {
        return false;
    }

    @Override
    public boolean updateCluster(Cluster cluster) {
        return false;
    }

    @Override
    public boolean updateClusterKeys(Cluster cluster) {
        int clusterId = cluster.getClusterId();
        clusterDao.updateTotalKey(clusterId, cluster.getTotalKeys());

        clusterDao.updateTotalExpires(clusterId, cluster.getTotalExpires());
        return false;
    }

    @Override
    public boolean deleteCluster(Cluster cluster) {
        return false;
    }
}
