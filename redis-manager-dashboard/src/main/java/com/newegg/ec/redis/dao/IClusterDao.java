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

    @Select("SELECT * FROM cluster WHERE cluster_name = #{clusterName}")
    Cluster selectClusterByName(String clusterName);


    @Insert("INSERT INFO cluster " +
            "(group_id, cluster_token, cluster_name, nodes, redis_mode, " +
            "os, redis_version, image, initialized, total_keys, total_expires, db_size" +
            "cluster_state, cluster_slots_assigned, cluster_slots_ok, " +
            "cluster_slots_pfail, cluster_slots_fail, cluster_known_nodes, cluster_size, " +
            "redis_password, installation_environment, installation_type, update_time) " +
            "VALUES (#{groupId}, #{clusterToken}, #{clusterName}, #{nodes}, #{redisMode}, " +
            "#{os}, #{redisVersion}, #{image}, #{initialized}, #{totalKeys}, #{totalExpires}, #{dbSize}" +
            "#{clusterState}, #{clusterSlotsAssigned}, #{clusterSlotsOk}, " +
            "#{clusterSlotsPfail}, #{clusterSlotsFail}, #{clusterKnownNodes}, #{clusterSize}, " +
            "#{redisPassword}, #{installationEnvironment}, #{installationType},  NOW())")
    Integer insertCluster(Cluster cluster);

    @Update("UPDATE cluster SET cluster_token = #{clusterToken}, cluster_name = #{clusterName}, nodes = #{nodes}, redis_mode = #{redisMode}, " +
            "os = #{os}, redis_version = #{redisVersion}, image = #{image}, initialized = #{initialized}, total_key = #{totalKey}, total_expires = #{totalExpires}, db_size = #{dbSize}" +
            "cluster_state = #{clusterState}, cluster_slots_assigned = #{clusterSlotsAssigned}, cluster_slots_ok = #{clusterSlotsOk}, " +
            "cluster_slots_pfail =  #{clusterSlotsPfail}, cluster_slots_fail = #{clusterSlotsFail}, cluster_known_nodes = #{clusterKnownNodes}, cluster_size = #{clusterSize}, " +
            "redis_password = #{redisPassword}, installation_environment = #{installationEnvironment}, installation_type = #{installationType}, update_time = NOW()" +
            "WHERE cluster_id = #{clusterId}")
    int updateCluster(Cluster cluster);

    @Update("UPDATE cluster SET total_key = #{totalKey}, total_expires = #{totalExpires}, update_time = NOW() WHERE cluster_id = #{clusterId}")
    int updateKeyspace(@Param("clusterId") Integer clusterId, @Param("totalKeys") Long totalKeys,  @Param("totalExpires") Long totalExpires);

    @Update("UPDATE cluster SET redis_password = #{redisPassword}, update_time = NOW() WHERE cluster_id = #{clusterId}")
    int updateRedisPassword(@Param("clusterId") Integer clusterId, @Param("redisPassword") String redisPassword);

    @Update("UPDATE cluster SET nodes = #{nodes}, update_time = NOW() WHERE cluster_id = #{clusterId}")
    int updateNodes(@Param("clusterId") Integer clusterId, @Param("nodes") String nodes);

    @Delete("DELETE FROM cluster WHERE cluster_id = #{clusterId}")
    int deleteClusterById(Integer clusterId);

}
