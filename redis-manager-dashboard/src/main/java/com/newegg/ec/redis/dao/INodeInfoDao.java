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
@Mapper
public interface INodeInfoDao {

    @Select("<script>" +
            "SELECT * FROM node_info_${clusterId} " +
            "WHERE update_time &gt;= #{startTime}" +
            "AND update_time &lt;= #{endTime}" +
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
            "INSERT INFO node_info_${clusterId} (node, data_type, time_type, last_time, " +
            "response_time, connected_clients, client_longest_output_list, client_biggest_input_buf, blocked_clients, " +
            "used_memory, used_memory_rss, used_memory_overhead, used_memory_dataset, used_memory_dataset_perc, mem_fragmentation_ratio, " +
            "total_connections_received, connections_received, rejected_connections, total_commands_processed, commands_processed, " +
            "instantaneous_ops_per_sec, total_net_input_bytes, net_input_bytes, total_net_output_bytes, net_output_bytes, " +
            "keyspace_hits, keyspace_misses, keyspace_hits_ratio, " +
            "used_cpu_sys, used_cpu_user, keys, expires, update_time) " +
            "VALUES " +
            "<foreach item='nodeInfo' collection='nodeInfoList' index='index' separator=','>" +
            "(#{node}, #{dataType}, #{timeType}, #{lastTime}, " +
            "#{responseTime}, #{connectedClients}, #{clientLongestOutputList}, #{clientBiggestInputBuf}, #{blockedClients}, " +
            "#{usedMemory}, #{usedMemoryRss}, #{usedMemoryOverhead}, #{usedMemoryDataset}, #{usedMemoryDatasetPerc}, #{memFragmentationRatio}, " +
            "#{totalConnectionsReceived}, #{connectionsReceived}, #{rejectedConnections}, #{totalCommandsProcessed}, #{commandsProcessed}, " +
            "#{instantaneousOpsPerSec}, #{totalNetInputBytes}, #{netInputBytes}, #{totalNetOutputBytes}, #{netOutputBytes}, " +
            "#{keyspaceHits}, #{keyspaceMisses}, #{keyspaceHitsRatio}, " +
            "#{usedCpuSys}, #{usedCpuUser}, #{keys}, #{expires}, NOW())" +
            "</foreach>" +
            "</script>")
    int insertNodeInfo(@Param("clusterId") Integer clusterId, @Param("nodeInfoList") List<NodeInfo> nodeInfoList);

    @Delete("DELETE FROM node_info_${clusterId} WHERE update_time &lt; #{oldestTime}")
    int deleteNodeInfoByTime(@Param("clusterId") Integer clusterId, @Param("oldestTime") Timestamp oldestTime);

    @Update("UPDATE node_info_${clusterId} SET last_time = 0 WHERE last_time = 1 AND time_type = #{timeType}")
    int updateLastTimeStatus(@Param("clusterId") Integer clusterId, @Param("timeType") NodeInfoType.TimeType timeType);

    @Delete("DELETE FROM node_info_${clusterId}")
    int deleteAllNodeInfo(Integer clusterId);

    @Delete("DROP TABLE node_info_${clusterId}")
    int dropTable(Integer clusterId);
}
