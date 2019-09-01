package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.NodeInfo;
import com.newegg.ec.redis.entity.NodeInfoParam;
import com.newegg.ec.redis.entity.NodeInfoType;
import org.apache.ibatis.annotations.*;

import java.sql.Timestamp;
import java.util.List;

/**
 * Manage node info
 *
 * @author Jay.H.Zou
 * @date 2019/7/18
 */
public interface INodeInfoDao {

    @Select("<script>" +
            "SELECT * FROM node_info_${clusterId} " +
            "WHERE update_time >= #{startTime}" +
            "AND update_time <= #{endTime}" +
            "AND data_type = #{dataType}" +
            "AND time_type = #{timeType}" +
            "<if test='node != null'> AND node = #{node} </if>" +
            "</script>")
    List<NodeInfo> selectNodeInfoList(NodeInfoParam nodeInfoParam);

    @Select("<script>" +
            "SELECT * FROM node_info_${clusterId} " +
            "WHERE data_type = #{dataType}" +
            "AND time_type = #{timeType}" +
            "<if test='node != null'> AND node = #{node} </if> " +
            "AND last_time = 1" +
            "</script>")
    List<NodeInfo> selectLastTimeNodeInfo(NodeInfoParam nodeInfoParam);

    @Insert("<script>" +
            "INSERT INFO node_info_${clusterId} (info_id, node, data_type, time_type, " +
            "response_time, connected_clients, client_longest_output_list, client_biggest_input_buf, blocked_clients, " +
            "used_memory, used_memory_rss, used_memory_overhead, used_memory_dataset, used_memory_dataset_perc, mem_fragmentation_ratio, " +
            "total_connections_received, connections_received, total_commands_processed, commands_processed, " +
            "total_net_input_bytes, net_input_bytes, total_net_output_bytes, net_output_bytes, " +
            "keyspace_hits, keyspace_misses, keyspace_hits_ratio, " +
            "used_cpu_sys, keys, expires, update_time) " +
            "VALUES " +
            "<foreach item='nodeInfo' collection='nodeInfoList' index='index' separator=','>" +
            "(#{infoId}, #{node}, #{dataType}, #{timeType}, " +
            "#{responseTime}, #{connectedClients}, #{clientLongestOutputList}, #{clientBiggestInputBuf}, #{blockedClients}, " +
            "#{usedMemory}, #{usedMemoryRss}, #{usedMemoryOverhead}, #{usedMemoryDataset}, #{usedMemoryDatasetPerc}, " +
            "#{totalConnectionsReceived}, #{connectionsReceived}, #{totalCommandsProcessed}, #{commandsProcessed}, " +
            "#{totalNetInputBytes}, #{netInputBytes}, #{totalNetOutputBytes}, #{commandsProcessed}, " +
            "#{keyspaceHits}, #{keyspaceMisses}, #{keyspaceHitsRatio}, " +
            "#{usedCpuSys}, #{keys}, #{expires}, NOW())" +
            "</foreach>" +
            "</script>")
    int insertNodeInfo(@Param("clusterId") int clusterId, @Param("nodeInfoList") List<NodeInfo> nodeInfoList);

    @Delete("DELETE FROM node_info_${clusterId} WHERE update_time < #{oldestTime}")
    int deleteNodeInfoByTime(@Param("clusterId") int clusterId, @Param("oldestTime") Timestamp oldestTime);

    @Update("UPDATE node_info_${clusterId} SET last_time = 0 WHERE last_time = 1 AND time_type = #{timeType}")
    int updateLastTimeStatus(@Param("clusterId") int clusterId, @Param("timeType") NodeInfoType.TimeType timeType);

    @Delete("DELETE FROM node_info_${clusterId}")
    int deleteAllNodeInfo(int clusterId);

    @Delete("DROP TABLE node_info_${clusterId}")
    int dropTable(int clusterId);
}
