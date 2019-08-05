package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Group;

import java.util.List;
import java.util.Map;

/**
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public interface IClusterService {

    List<Cluster> getAllClusterList();

    List<Cluster> getClusterListByGroupId(int groupId);

    Cluster getClusterById(int clusterId);

    boolean saveCluster(Cluster cluster);

    boolean updateCluster(Cluster cluster);

    boolean updateClusterKeys(Cluster cluster);

    boolean deleteCluster(Cluster cluster);

}
