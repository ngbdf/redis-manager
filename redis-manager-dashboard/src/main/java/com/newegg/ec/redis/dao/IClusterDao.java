package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.Cluster;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 *
 * @author Jay.H.Zou
 * @date 2019/7/18
 */
@Mapper
public interface IClusterDao {

    @Select("SELECT * FROM cluster")
    List<Cluster> selectAllCluster();

    @Select("SELECT * FROM cluster WHERE group_id = #{groupId}")
    List<Cluster> selectClusterByGroupId(Integer groupId);

    @Select("SELECT * FROM cluster WHERE cluster_id = #{clusterId}")
    Cluster selectClusterById(Integer clusterId);

    @Select("SELECT * FROM cluster WHERE group_id = #{groupId} AND cluster_id = #{clusterId}")
    Cluster getClusterByIdAndGroup(Integer groupId, Integer clusterId);

    @Select("SELECT * FROM cluster WHERE cluster_name = #{clusterName}")
    Cluster selectClusterByName(String clusterName);

    @Insert("INSERT INTO cluster " +
            "(group_id, user_id, cluster_token, " +
            "cluster_name, nodes, redis_mode, " +
            "os, redis_version, image, " +
            "initialized, total_keys, total_expires, " +
            "db_size, cluster_state, cluster_slots_assigned, " +
            "cluster_slots_ok, cluster_slots_pfail, cluster_slots_fail, " +
            "cluster_known_nodes, cluster_size, redis_password, " +
            "installation_environment, installation_type, update_time) " +
            "VALUES " +
            "(#{groupId}, #{userId}, #{clusterToken}, " +
            "#{clusterName}, #{nodes}, #{redisMode}, " +
            "#{os}, #{redisVersion}, #{image}, " +
            "#{initialized}, #{totalKeys}, #{totalExpires}, " +
            "#{dbSize}, #{clusterState}, #{clusterSlotsAssigned}, " +
            "#{clusterSlotsOk}, #{clusterSlotsPfail}, #{clusterSlotsFail}, " +
            "#{clusterKnownNodes}, #{clusterSize}, #{redisPassword}, " +
            "#{installationEnvironment}, #{installationType},  NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "clusterId", keyColumn = "cluster_id")
    Integer insertCluster(Cluster cluster);

    @Update("UPDATE cluster SET " +
            "cluster_token = #{clusterToken}, user_id = #{userId}, cluster_name = #{clusterName}, " +
            "nodes = #{nodes}, redis_mode = #{redisMode}, os = #{os}, " +
            "redis_version = #{redisVersion}, initialized = #{initialized}, " +
            "total_keys = #{totalKeys}, total_expires = #{totalExpires}, db_size = #{dbSize}, " +
            "cluster_state = #{clusterState}, cluster_slots_assigned = #{clusterSlotsAssigned}, cluster_slots_ok = #{clusterSlotsOk}, " +
            "cluster_slots_pfail = #{clusterSlotsPfail}, cluster_slots_fail = #{clusterSlotsFail}, cluster_known_nodes = #{clusterKnownNodes}, " +
            "cluster_size = #{clusterSize}, redis_password = #{redisPassword}, installation_environment = #{installationEnvironment}, " +
            "update_time = NOW() " +
            "WHERE cluster_id = #{clusterId}")
    int updateCluster(Cluster cluster);

    @Update("UPDATE cluster SET total_keys = #{totalKeys}, total_expires = #{totalExpires}, update_time = NOW() WHERE cluster_id = #{clusterId}")
    int updateKeyspace(@Param("clusterId") Integer clusterId, @Param("totalKeys") Long totalKeys,  @Param("totalExpires") Long totalExpires);

    @Update("UPDATE cluster SET redis_password = #{redisPassword}, update_time = NOW() WHERE cluster_id = #{clusterId}")
    int updateRedisPassword(@Param("clusterId") Integer clusterId, @Param("redisPassword") String redisPassword);

    @Update("UPDATE cluster SET nodes = #{nodes}, update_time = NOW() WHERE cluster_id = #{clusterId}")
    int updateNodes(@Param("clusterId") Integer clusterId, @Param("nodes") String nodes);

    @Update("UPDATE cluster SET rule_ids = #{ruleIds}, update_time = NOW() WHERE cluster_id = #{clusterId}")
    int updateClusterRuleIds(@Param("clusterId") Integer clusterId, @Param("ruleIds") String ruleIds);

    @Update("UPDATE cluster SET channel_ids = #{channelIds}, update_time = NOW() WHERE cluster_id = #{clusterId}")
    int updateClusterChannelIds(@Param("clusterId") Integer clusterId, @Param("channelIds") String channelIds);

    @Delete("DELETE FROM cluster WHERE cluster_id = #{clusterId}")
    int deleteClusterById(Integer clusterId);

    @Select("create TABLE IF NOT EXISTS `cluster` ( " +
            "cluster_id integer(4) NOT NULL AUTO_INCREMENT COMMENT '自增ID', " +
            "group_id integer(4) NOT NULL, " +
            "user_id integer(4) NOT NULL, " +
            "cluster_token varchar(255) DEFAULT NULL, " +
            "cluster_name varchar(255) NOT NULL, " +
            "nodes varchar(255) NOT NULL, " +
            "redis_mode varchar(25) NOT NULL, " +
            "os varchar(255) NOT NULL, " +
            "redis_version varchar(25) NOT NULL, " +
            "image varchar(255) DEFAULT NULL, " +
            "initialized tinyint(1) NOT NULL, " +
            "total_keys bigint(20) NOT NULL, " +
            "total_expires bigint(20) NOT NULL, " +
            "db_size integer(8) NOT NULL, " +
            "cluster_state varchar(50) NOT NULL, " +
            "cluster_slots_assigned integer(4) DEFAULT NULL, " +
            "cluster_slots_ok integer(4) DEFAULT NULL, " +
            "cluster_slots_pfail integer(4) DEFAULT NULL, " +
            "cluster_slots_fail integer(4) DEFAULT NULL, " +
            "cluster_known_nodes integer(4) NOT NULL, " +
            "cluster_size integer(4) NOT NULL, " +
            "redis_password varchar(50) DEFAULT NULL, " +
            "rule_ids varchar(255) DEFAULT NULL, " +
            "channel_ids varchar(255) DEFAULT NULL, " +
            "installation_environment integer(2) NOT NULL, " +
            "installation_type tinyint(1) NOT NULL, " +
            "update_time datetime(0) NOT NULL, " +
            "PRIMARY KEY (cluster_id), " +
            "UNIQUE KEY (cluster_name) " +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createClusterTable();

}
