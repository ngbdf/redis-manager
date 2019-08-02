package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.Cluster;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 *
 * @author Jay.H.Zou
 * @date 2019/7/18
 */
public interface IClusterDao {

    @Select("SELECT * FROM cluster;")
    List<Cluster> selectAllCluster();

    @Select("SELECT * FROM cluster WHERE group_id = #{groupId}")
    List<Cluster> selectClusterByGroupId(String groupId);

    @Select("<script>" +
            "SELECT * FROM cluster WHERE group_id IN " +
            "<foreach item='groupId' collection='groupIdList'>" +
            "#{groupId}" +
            "</foreach>" +
            "</script>")
    List<Cluster> selectClusterByGroupIdList(@Param("groupIdList") List<String> groupIdList);

    @Select("SELECT * FROM cluster WHERE cluster_id = #{clusterId}")
    Cluster selectClusterById(String clusterId);

    @Select("SELECT * FROM cluster WHERE cluster_name = #{clusterName}")
    Cluster selectClusterByName(String clusterName);

//    List<Cluster> selectClusterByUserId(String userId);

    @Insert("INSERT INFO cluster " +
            "(cluster_id, group_id, admins, " +
            "cluster_token, nodes, redis_mode, " +
            "os, redis_version, total_keys, total_expires, " +
            "cluster_state, cluster_slots_assigned, cluster_slots_ok, " +
            "cluster_slots_pfail, cluster_slots_fail, " +
            "cluster_known_nodes, cluster_size, redis_password, " +
            "installation_env, installation_type, update_time) " +
            "VALUES (#{clusterId}, #{groupId}, #{admins}, " +
            "#{clusterToken}, #{nodes}, #{redisMode}, " +
            "#{os}, #{redisVersion}, #{totalKeys}, #{totalExpires}, " +
            "#{clusterState}, #{clusterSlotsAssigned}, #{clusterSlotsOk}, " +
            "#{clusterSlotsPfail}, #{clusterSlotsFail}, " +
            "#{clusterKnownNodes}, #{clusterSize}, #{redisPassword}, " +
            "#{installationEnv}, #{installationType}, #{updateTime})")
    int insertCluster(Cluster cluster);

    @Update("UPDATE cluster SET cluster_id = #{clusterId}, group_id = #{groupId}, user_ids = #{userIds}," +
            "cluster_token = #{clusterToken}, nodes = #{nodes}, redis_mode = #{redisMode}, " +
            "os = #{os}, redis_version = #{redisVersion}, total_key = #{totalKey}, total_expires = #{totalExpires}, " +
            "cluster_state = #{clusterState}, cluster_slots_assigned = #{clusterSlotsAssigned}, cluster_slots_ok = #{clusterSlotsOk}, " +
            "cluster_slots_pfail =  #{clusterSlotsPfail}, cluster_slots_fail = #{clusterSlotsFail}, " +
            "cluster_known_nodes = #{clusterKnownNodes}, cluster_size = #{clusterSize}, redis_password = #{redisPassword}" +
            "installation_env = #{installationEnv}, installation_type = #{installationType}, update_time = #{updateTime}" +
            "WHERE cluster_id = #{clusterId}")
    int updateCluster(Cluster cluster);

    @Update("UPDATE cluster SET total_key = #{totalKey}, update_time = NOW() WHERE cluster_id = #{clusterId}")
    int updateTotalKey(@Param("clusterId") String clusterId, @Param("totalKeys") long totalKeys);

    @Update("UPDATE cluster SET total_expires = #{totalExpires}, update_time = NOW() WHERE cluster_id = #{clusterId}")
    int updateTotalExpires(@Param("clusterId") String clusterId, @Param("totalExpires") long totalExpires);

    @Update("UPDATE cluster SET user_ids = #{userIds}, update_time = NOW() WHERE cluster_id = #{clusterId}")
    int updateUserIds(@Param("clusterId") String clusterId, @Param("userIds") String userIds);

    @Update("UPDATE cluster SET redis_password = #{redisPassword}, update_time = NOW() WHERE cluster_id = #{clusterId}")
    int updateRedisPassword(@Param("clusterId") String clusterId, @Param("redisPassword") String redisPassword);

    @Update("UPDATE cluster SET nodes = #{nodes}, update_time = NOW() WHERE cluster_id = #{clusterId}")
    int updateNodes(@Param("clusterId") String clusterId, @Param("nodes") String nodes);

    @Delete("DELETE FROM cluster WHERE cluster_id = #{clusterId}")
    int deleteClusterById(String clusterId);

}
