package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.NodeInfo;
import com.newegg.ec.redis.entity.NodeInfoQueryParam;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
            "SELECT * FROM ${tableName} " +
            "WHERE update_time >= #{startTime}" +
            "AND update_time <= #{endTime}" +
            "AND data_type = #{dataType}" +
            "AND time_type = #{timeType}" +
            "<if test='node != null'> AND node = #{node} </if>" +
            "</script>")
    List<NodeInfo> selectAllNodeInfoList(NodeInfoQueryParam nodeInfoQueryParam);

    @Select("<script>" +
            "SELECT * FROM ${tableName} " +
            "WHERE update_time >= #{startTime}" +
            "AND update_time <= #{endTime}" +
            "AND data_type = #{dataType}" +
            "AND time_type = #{timeType}" +
            "<if test='node != null'> AND node = #{node} </if>" +
            "</script>")
    List<NodeInfo> selectMonitorNodeInfoList(NodeInfoQueryParam nodeInfoQueryParam);

    @Insert("<script>" +
            "INSERT INFO ${tableName} (info_id, node, data_type, time_type, " +
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
            "#{usedCpuSys}, #{keys}, #{expires}, #{updateTime})" +
            "</foreach>" +
            "</script>")
    int insertNodeInfo(String tableName, List<NodeInfo> nodeInfoList);

    @Delete("DELETE FROM ${tableName} WHERE update_time < #{oldestTime}")
    int deleteNodeInfoByTime(@Param("tableName") String tableName, @Param("oldestTime")Timestamp oldestTime);

    @Delete("DELETE FROM ${tableName}")
    int deleteAllNodeInfo(String tableName);

    @Delete("DROP TABLE ${tableName}")
    int dropTable(String tableName);
}
