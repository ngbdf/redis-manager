package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.Cluster;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public interface IClusterService {

    List<Cluster> getAllClusterList();

    List<Cluster> getClusterListByGroupId(int groupId);

    Cluster getClusterById(int clusterId);

    Cluster getClusterByName(String clusterName);

    boolean addCluster(Cluster cluster);

    boolean updateCluster(Cluster cluster);

    boolean updateClusterKeys(Cluster cluster);

    boolean deleteCluster(Integer clusterId);

    boolean fillClusterInfo(Cluster cluster);

    boolean fillStandaloneInfo(Cluster cluster);

    void fillBaseInfo(Cluster cluster);

    void fillKeyspaceInfo(Cluster cluster);

}
