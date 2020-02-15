package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.SentinelMaster;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 1/22/2020
 */
@Mapper
public interface ISentinelMastersDao {

    @Select("SELECT * FROM sentinel_masters WHERE cluster_id = #{clusterId}")
    List<SentinelMaster> selectSentinelMasterByClusterId(Integer clusterId);

    @Select("SELECT * FROM sentinel_masters WHERE cluster_id = #{clusterId} AND name = #{name}")
    SentinelMaster selectSentinelMasterByMasterName(@Param("clusterId") Integer clusterId, @Param("name") String name);

    @Select("SELECT * FROM sentinel_masters WHERE sentinel_master_id = #{sentinelMasterId}")
    SentinelMaster selectSentinelMasterById(Integer sentinelMasterId);

    @Update("UPDATE sentinel_masters SET host = #{host} , port = #{port}, flags = #{flags}, " +
            "status = #{status}, link_pending_commands = #{linkPendingCommands}, link_refcount = #{linkRefcount}, " +
            "last_ping_sent = #{lastPingSent}, last_ok_ping_reply = #{lastOkPingReply}, last_ping_reply = #{lastPingReply}, " +
            "s_down_time = #{sDownTime}, o_down_time = #{oDownTime}, down_after_milliseconds = #{downAfterMilliseconds}, info_refresh = #{infoRefresh}, " +
            "role_reported = #{roleReported}, role_reported_time = #{roleReportedTime}, config_epoch = #{configEpoch}, num_slaves = #{numSlaves}, sentinels = #{sentinels}, " +
            "quorum = #{quorum} , failover_timeout = #{failoverTimeout}, parallel_syncs = #{parallelSyncs}, auth_pass = #{authPass}, update_time = NOW() " +
            "WHERE cluster_id = #{clusterId} AND name = #{name}")
    int updateSentinelMaster(SentinelMaster sentinelMaster);

    @Insert("INSERT INTO sentinel_masters (cluster_id, group_id, name, host, port, flags, last_master_node, " +
            "status, link_pending_commands, link_refcount, last_ping_sent, last_ok_ping_reply, last_ping_reply, " +
            "s_down_time, o_down_time, down_after_milliseconds, info_refresh, role_reported, role_reported_time, config_epoch, " +
            "num_slaves, sentinels, quorum, failover_timeout, parallel_syncs, auth_pass, update_time) " +
            "VALUES " +
            "(#{clusterId}, #{groupId}, #{name}, #{host}, #{port}, #{flags}, #{lastMasterNode}, " +
            "#{status}, #{linkPendingCommands}, #{linkRefcount}, #{lastPingSent}, #{lastOkPingReply}, #{lastPingReply}, " +
            "#{sDownTime}, #{oDownTime}, #{downAfterMilliseconds}, #{infoRefresh}, #{roleReported}, #{roleReportedTime}, #{configEpoch}, " +
            "#{numSlaves}, #{sentinels}, #{quorum}, #{failoverTimeout}, #{parallelSyncs}, #{authPass}, NOW())")
    int insertSentinelMaster(SentinelMaster sentinelMaster);

    @Delete("DELETE FROM sentinel_masters WHERE cluster_id = #{clusterId} AND name = #{name}")
    int deleteSentinelMasterByName(@Param("clusterId") Integer clusterId, @Param("name") String name);

    @Delete("DELETE FROM sentinel_masters WHERE sentinel_master_id = #{cluster_id}")
    int deleteSentinelMasterByClusterId(Integer clusterId);

    @Select("create TABLE IF NOT EXISTS `sentinel_masters` (\n" +
            "sentinel_master_id integer(4) NOT NULL AUTO_INCREMENT,\n" +
            "cluster_id integer(4) NOT NULL,\n" +
            "group_id integer(4) NOT NULL,\n" +
            "name varchar(255) NOT NULL,\n" +
            "host varchar(255) NOT NULL,\n" +
            "port integer(4) NOT NULL,\n" +
            "flags varchar(255) DEFAULT NULL,\n" +
            "last_master_node varchar(255) DEFAULT NULL,\n" +
            "status varchar(50) DEFAULT NULL,\n" +
            "link_pending_commands bigint(20) DEFAULT NULL,\n" +
            "link_refcount bigint(20) DEFAULT NULL,\n" +
            "last_ping_sent bigint(20) DEFAULT NULL,\n" +
            "last_ok_ping_reply bigint(20) DEFAULT NULL,\n" +
            "last_ping_reply bigint(20) DEFAULT NULL,\n" +
            "s_down_time bigint(20) DEFAULT NULL,\n" +
            "o_down_time bigint(20) DEFAULT NULL,\n" +
            "down_after_milliseconds bigint(20) DEFAULT NULL,\n" +
            "info_refresh bigint(20) DEFAULT NULL,\n" +
            "role_reported varchar(50) DEFAULT NULL,\n" +
            "role_reported_time bigint(20) DEFAULT NULL,\n" +
            "config_epoch bigint(20) DEFAULT NULL,\n" +
            "num_slaves integer(4) DEFAULT NULL,\n" +
            "sentinels integer(4) DEFAULT NULL,\n" +
            "quorum integer(4) DEFAULT NULL,\n" +
            "failover_timeout bigint(4) DEFAULT NULL,\n" +
            "parallel_syncs integer(4) DEFAULT NULL,\n" +
            "auth_pass varchar(255) DEFAULT NULL,\n" +
            "update_time datetime(0) NOT NULL,\n" +
            "PRIMARY KEY (sentinel_master_id),\n" +
            "UNIQUE KEY `master_name` (cluster_id, name) " +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createSentinelMastersTable();
}
