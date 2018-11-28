package com.newegg.ec.cache.app.dao;

import com.newegg.ec.cache.app.model.Cluster;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by gl49 on 2018/4/20.
 */
@Repository
public interface IClusterDao {
    List<Cluster> getClusterList(String userGroup);

    List<Cluster> getClusterByAddress(String address);

    Cluster getCluster(int id);

    int removeCluster(int id);

    int addCluster(Cluster cluster);

    boolean updateClusterAddress(@Param("id") int id, @Param("address") String address);

    List<String> getClusterGroups();

    int updatePassword(@Param("id") int id, @Param("redisPassword") String redisPassword);
}
