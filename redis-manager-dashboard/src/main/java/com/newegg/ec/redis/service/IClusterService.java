package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.Cluster;

import java.util.List;

/**
 *
 *
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public interface IClusterService {

    List<Cluster> getAllClusterList();

    List<Cluster> getClusterListByGroupId(String groupId);

    Cluster getClusterById(String clusterId);

    boolean addCluster(Cluster cluster);

    boolean updateCluster(Cluster cluster);

    boolean deleteCluster(Cluster cluster);

}
