package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.NodeInfo;
import com.newegg.ec.redis.entity.NodeInfoQueryParam;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Manage node info
 *
 *     `info_id` integer(4) NOT NULL AUTO_INCREMENT,
 *     `node` varchar(50) NOT NULL,
 *     `response_time` integer(4) NOT NULL,
 *     `data_type` varchar(50) NOT NULL,
 *     `time_type` varchar(50) NOT NULL,
 *     `connected_clients` integer(4) NOT NULL,
 *     `client_longest_output_list` integer(4) NOT NULL,
 *     `client_biggest_input_buf` integer(4) NOT NULL,
 *     `blocked_clients` integer(4) NOT NULL,
 *     `used_memory` integer(4) NOT NULL,
 *     `used_memory_rss` integer(4) NOT NULL,
 *     `used_memory_overhead` integer(4) NOT NULL,
 *     `used_memory_dataset_perc` integer(4) NOT NULL,
 *     `mem_fragmentation_ratio` double(6, 2) NOT NULL,
 *     `total_connections_received` integer(4) NOT NULL,
 *     `total_commands_processed` integer(4) NOT NULL,
 *     `minute_commands_processed` integer(4) NOT NULL,
 *     `total_net_input_bytes` integer(4) NOT NULL,
 *     `total_net_output_bytes` integer(4) NOT NULL,
 *     `keyspace_hits` integer(4) NOT NULL,
 *     `keyspace_misses` integer(4) NOT NULL,
 *     `keyspace_hits_ratio` double(6, 2) NOT NULL,
 *     `used_cpu_sys` double(6, 2) NOT NULL,
 *     `keys` integer(4) NOT NULL,
 *     `expires` integer(4) NOT NULL,
 *     `update_time` datetime(0) NOT NULL,
 *
 * @author Jay.H.Zou
 * @date 2019/7/18
 */
public interface INodeInfoDao {

    @Select("SELECT * FROM ${tableName} " +
            "WHERE update_time >= #{startTime}" +
            "AND update_time <= #{endTime}" +
            "AND data_type = #{dataType}" +
            "AND time_type = #{timeType}")
    List<NodeInfo> selectAllNodeInfoList(NodeInfoQueryParam nodeInfoQueryParam);

    @Select("SELECT * FROM ${tableName} " +
            "AND update_time <= #{endTime}" +
            "AND data_type = #{dataType}" +
            "AND time_type = #{timeType}")
    List<NodeInfo> selectMonitorNodeInfoList(NodeInfoQueryParam nodeInfoQueryParam);

    @Insert("<script>" +
            "INSERT INFO ${tableName} (info_id, node, data_type, time_type, " +
            "response_time, connected_clients, client_longest_output_list, client_biggest_input_buf," +
            "blocked_clients, used_memory, used_memory_rss, used_memory_overhead, " +
            "used_memory_dataset_perc, mem_fragmentation_ratio, total_connections_received, total_commands_processed, " +
            "minute_commands_processed, total_net_input_bytes, total_net_output_bytes, keyspace_hits, " +
            "keyspace_misses, keyspace_hits_ratio, used_cpu_sys, keys, expires, update_time)" +
            "VALUES (#{infoId}, #{node}, #{dataType}, #{timeType} ...)" +
            "</script>")
    int insertNodeInfo(String tableName, List<NodeInfo> nodeInfoList);

    int deleteNodeInfoByTime(NodeInfoQueryParam nodeInfoQueryParam);

    int deleteAllNodeInfo(NodeInfoQueryParam nodeInfoQueryParam);

}
