package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.NodeInfo;
import com.newegg.ec.redis.entity.NodeInfoParam;
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
            "WHERE update_time &gt;= #{startTime} " +
            "AND update_time &lt;= #{endTime} " +
            "AND data_type = #{dataType} " +
            "AND time_type = #{timeType} " +
            "<if test='node != null'> AND node = #{node} </if>" +
            "</script>")
    List<NodeInfo> selectNodeInfoList(NodeInfoParam nodeInfoParam);

    @Select("<script>" +
            "SELECT * FROM node_info_${clusterId} " +
            "WHERE data_type = #{dataType} " +
            "AND time_type = #{timeType} " +
            "<if test='node != null'> AND node = #{node} </if> " +
            "AND last_time = 1" +
            "</script>")
    List<NodeInfo> selectLastTimeNodeInfo(NodeInfoParam nodeInfoParam);

    @Insert("<script>" +
            "INSERT INTO node_info_${clusterId} (`node`, `role`, `data_type`, `time_type`, `last_time`, " +
            "`response_time`, `connected_clients`, `client_longest_output_list`, `client_biggest_input_buf`, `blocked_clients`, " +
            "`used_memory`, `used_memory_rss`, `used_memory_overhead`, `used_memory_dataset`, `used_memory_dataset_perc`, `mem_fragmentation_ratio`, " +
            "`total_connections_received`, `connections_received`, `rejected_connections`, `total_commands_processed`, `commands_processed`, " +
            "`instantaneous_ops_per_sec`, `total_net_input_bytes`, `net_input_bytes`, `total_net_output_bytes`, `net_output_bytes`, " +
            "`sync_full`, `sync_partial_ok`, `sync_partial_err`, " +
            "`keyspace_hits`, `keyspace_misses`, `keyspace_hits_ratio`, " +
            "`used_cpu_sys`, `cpu_sys`, `used_cpu_user`, `cpu_user`, `keys`, `expires`, `update_time`) " +
            "VALUES " +
            "<foreach item='nodeInfo' collection='nodeInfoList' index='index' separator=','>" +
            "(#{nodeInfo.node}, #{nodeInfo.role}, #{nodeInfo.dataType}, #{nodeInfo.timeType}, #{nodeInfo.lastTime}, " +
            "#{nodeInfo.responseTime}, #{nodeInfo.connectedClients}, #{nodeInfo.clientLongestOutputList}, #{nodeInfo.clientBiggestInputBuf}, #{nodeInfo.blockedClients}, " +
            "#{nodeInfo.usedMemory}, #{nodeInfo.usedMemoryRss}, #{nodeInfo.usedMemoryOverhead}, #{nodeInfo.usedMemoryDataset}, #{nodeInfo.usedMemoryDatasetPerc}, #{nodeInfo.memFragmentationRatio}, " +
            "#{nodeInfo.totalConnectionsReceived}, #{nodeInfo.connectionsReceived}, #{nodeInfo.rejectedConnections}, #{nodeInfo.totalCommandsProcessed}, #{nodeInfo.commandsProcessed}, " +
            "#{nodeInfo.instantaneousOpsPerSec}, #{nodeInfo.totalNetInputBytes}, #{nodeInfo.netInputBytes}, #{nodeInfo.totalNetOutputBytes}, #{nodeInfo.netOutputBytes}, " +
            "#{nodeInfo.syncFull}, #{nodeInfo.syncPartialOk}, #{nodeInfo.syncPartialErr}, " +
            "#{nodeInfo.keyspaceHits}, #{nodeInfo.keyspaceMisses}, #{nodeInfo.keyspaceHitsRatio}, " +
            "#{nodeInfo.usedCpuSys}, #{nodeInfo.cpuSys}, #{nodeInfo.usedCpuUser}, #{nodeInfo.cpuUser}, #{nodeInfo.keys}, #{nodeInfo.expires}, NOW())" +
            "</foreach>" +
            "</script>")
    int insertNodeInfo(@Param("clusterId") Integer clusterId, @Param("nodeInfoList") List<NodeInfo> nodeInfoList);

    @Delete("DELETE FROM node_info_${clusterId} WHERE update_time &lt; #{oldestTime}")
    int deleteNodeInfoByTime(@Param("clusterId") Integer clusterId, @Param("oldestTime") Timestamp oldestTime);

    @Update("UPDATE node_info_${clusterId} SET last_time = 0 WHERE last_time = 1 AND time_type = #{timeType}")
    int updateLastTimeStatus(@Param("clusterId") Integer clusterId, @Param("timeType") Integer timeType);

    @Delete("DELETE FROM node_info_${clusterId}")
    int deleteAllNodeInfo(@Param("clusterId") Integer clusterId);

    @Delete("DROP TABLE node_info_${clusterId}")
    int dropTable(@Param("clusterId") Integer clusterId);

    @Select("DROP TABLE node_info_${clusterId}")
    void deleteNodeInfoTable(@Param("clusterId") Integer clusterId);

    @Select("SELECT COUNT(*) FROM information_schema.TABLES WHERE table_schema = #{database} AND table_name = #{tableName}")
    int existNodeInfoTable(@Param("database") String database, @Param("tableName") String tableName);

    @Select("CREATE TABLE IF NOT EXISTS node_info_${clusterId} ( " +
            "`info_id` integer(4) NOT NULL AUTO_INCREMENT, " +
            "`node` varchar(50) NOT NULL, " +
            "`role` varchar(50) NOT NULL, " +
            "`data_type` integer(2) NOT NULL, " +
            "`time_type` integer(2) NOT NULL, " +
            "`last_time` tinyint(1) NOT NULL, " +
            "`response_time` integer(4) NOT NULL, " +
            "`connected_clients` bigint(20) NOT NULL, " +
            "`client_longest_output_list` bigint(20) NOT NULL, " +
            "`client_biggest_input_buf` bigint(20) NOT NULL, " +
            "`blocked_clients` bigint(20) NOT NULL, " +
            "`used_memory` bigint(20) NOT NULL, " +
            "`used_memory_rss` bigint(20) NOT NULL, " +
            "`used_memory_overhead` bigint(20) NOT NULL, " +
            "`used_memory_dataset` bigint(20) NOT NULL, " +
            "`used_memory_dataset_perc` double(6, 2) NOT NULL, " +
            "`mem_fragmentation_ratio` double(6, 2) NOT NULL, " +
            "`total_connections_received` bigint(20) NOT NULL, " +
            "`connections_received` bigint(20) NOT NULL, " +
            "`rejected_connections` bigint(20) NOT NULL, " +
            "`total_commands_processed` bigint(20) NOT NULL, " +
            "`commands_processed` bigint(20) NOT NULL, " +
            "`instantaneous_ops_per_sec` bigint(20) NOT NULL, " +
            "`total_net_input_bytes` bigint(20) NOT NULL, " +
            "`net_input_bytes` bigint(20) NOT NULL, " +
            "`total_net_output_bytes` bigint(40) NOT NULL, " +
            "`net_output_bytes` bigint(20) NOT NULL, " +
            "`sync_full` integer(4) NOT NULL, " +
            "`sync_partial_ok` integer(4) NOT NULL, " +
            "`sync_partial_err` integer(4) NOT NULL, " +
            "`keyspace_misses` bigint(20) NOT NULL, " +
            "`keyspace_hits` bigint(20) NOT NULL, " +
            "`keyspace_hits_ratio` double(6, 2) NOT NULL, " +
            "`used_cpu_sys` double(20, 2) NOT NULL, " +
            "`cpu_sys` double(20, 2) NOT NULL, " +
            "`used_cpu_user` double(20, 2) NOT NULL, " +
            "`cpu_user` double(20, 2) NOT NULL, " +
            "`keys` integer(8) NOT NULL, " +
            "`expires` integer(8) NOT NULL, " +
            "`update_time` datetime(0) NOT NULL, " +
            "PRIMARY KEY (info_id), " +
            "INDEX `multiple_query` (`update_time`, `data_type`, `time_type`, `node`) " +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createNodeInfoTable(@Param("clusterId") Integer clusterId);
}
