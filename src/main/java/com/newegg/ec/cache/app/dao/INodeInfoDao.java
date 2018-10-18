package com.newegg.ec.cache.app.dao;

import com.newegg.ec.cache.app.model.NodeInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by gl49 on 2018/4/21.
 */
@Repository
public interface INodeInfoDao {
    boolean addNodeInfo(@Param("tableName") String tableName, @Param("q") NodeInfo nodeInfo);

    List<NodeInfo> getGroupNodeInfo(@Param("tableName") String tableName, @Param("startTime") int startTime, @Param("endTime") int endTime, @Param("host") String host, @Param("type") String type, @Param("date") String date);

    List<Map> getMaxField(@Param("tableName") String tableName, @Param("startTime") int startTime, @Param("endTime") int endTime, @Param("key") String key, @Param("limit") int limit);

    List<Map> getMinField(@Param("tableName") String tableName, @Param("startTime") int startTime, @Param("endTime") int endTime, @Param("key") String key, @Param("limit") int limit);

    String getAvgField(@Param("tableName") String tableName, @Param("startTime") int startTime, @Param("endTime") int endTime, @Param("host") String host, @Param("key") String key);

    String getAllField(@Param("tableName") String tableName, @Param("startTime") int startTime, @Param("endTime") int endTime, @Param("key") String key);

    NodeInfo getLastNodeInfo(@Param("tableName") String tableName, @Param("startTime") int startTime, @Param("endTime") int endTime, @Param("host") String host);
}
