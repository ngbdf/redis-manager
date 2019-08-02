package com.newegg.ec.redis.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.dao.IClusterDao;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Group;
import com.newegg.ec.redis.service.IClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jay.H.Zou
 * @date 8/2/2019
 */
public class ClusterService implements IClusterService {

    private static final Logger logger = LoggerFactory.getLogger(ClusterService.class);

    @Autowired
    private IClusterDao clusterDao;

    @Override
    public Map<Group, List<Cluster>> getAllClusterList() {
        return null;
    }

    @Override
    public List<Cluster> getClusterListByGroupId(String groupId) {
        List<Cluster> clusterList = new ArrayList<>();
        if (Strings.isNullOrEmpty(groupId)) {
            return clusterList;
        }
        try {
            return clusterDao.selectClusterByGroupId(groupId);
        } catch (Exception e) {
            logger.error("Get cluster list by group id failed, group id = " + groupId, e);
        }
        return clusterList;
    }

    @Override
    public Cluster getClusterById(String clusterId) {
        if (Strings.isNullOrEmpty(clusterId)) {
            return null;
        }
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
    public boolean deleteCluster(Cluster cluster) {
        return false;
    }
}
