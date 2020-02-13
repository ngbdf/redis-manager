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

    @Select("SELECT * from 'sentinel_masters' where cluster_id = #{clusterId}")
    List<SentinelMaster> selectSentinelMasterByClusterId(Integer clusterId);

    @Select("SELECT * from 'sentinel_masters' where cluster_id = #{clusterId} and master_name =#{masterName}")
    SentinelMaster selectSentinelMasterByMasterName(Integer clusterId, String masterName);

    @Select("SELECT * from 'sentinel_masters' where sentinel_master_id = #{sentinelMasterId}")
    SentinelMaster selectSentinelMasterById(Integer sentinelMasterId);

    @Update("UPDATE sentinel_masters set group_id = #{groupId}, user_id = #{userId}, " +
            "flags = #{flags}, s_down_time = #{sDownTime}, o_down_time = #{oDownTime},  down_after_milliseconds = #{downAfterMilliseconds}," +
            "num_slaves = #{numSlaves} , master_host = #{masterHost} , master_port = #{masterPort}, state = #{state} " +
            "master_name = #{masterName} , quorum = #{quorum} , failover_timeout = #{failoverTimeout},parallel_syncs = #{parallelSync} ," +
            "update_time = NOW() where cluster_id = #{clusterId}")
    int updateSentinelMaster(SentinelMaster sentinelMaster);

    @Insert("INSERT into 'sentinel_masters' (cluster_id, group_id, master_name, master_host, " +
            "master_port, last_master_node, flags, s_down_time, o_down_time, down_after_milliseconds" +
            "num_slaves, quorum, failover_timeout, parallel_syncs, state, update_time) " +
            "VALUES " +
            "(#{sentinelMaster.clusterId}, #{sentinelMaster.groupId}, #{sentinelMaster.masterName}, #{sentinelMaster.masterHost}," +
            "#{sentinelMaster.masterPort}, #{sentinelMaster.lastMasterNode}, #{sentinelMaster.flags}, #{sentinelMaster.sDownTime}, #{sentinelMaster.oDownTime}," +
            "#{sentinelMaster.downAfterMilliseconds}, #{sentinelMaster.numSlaves}, #{sentinelMaster.quorum}, #{sentinelMaster.failoverTimeout}," +
            "#{sentinelMaster.numSlaves}, #{sentinelMaster.numSlaves}," +
            "#{sentinelMaster.parallelSync}, #{sentinelMaster.state}, NOW()) ")
    int insertSentinelMaster(SentinelMaster sentinelMaster);

    @Delete("")
    int deleteSentinelMaster(SentinelMaster sentinelMaster);

    @Delete("DELETE FROM sentinel_masters WHERE sentinel_master_id = #{sentinelMasterId}")
    int deleteSentinelMasterById(Integer sentinelMasterId);

    @Delete("DELETE FROM sentinel_masters WHERE sentinel_master_id = #{cluster_id}")
    int deleteSentinelMasterByClusterId(Integer clusterId);

    @Select("create TABLE IF NOT EXISTS `sentinel_masters` (\n" +
            "sentinel_master_id integer(4) NOT NULL AUTO_INCREMENT,\n" +
            "cluster_id integer(4) NOT NULL,\n" +
            "group_id integer(4) NOT NULL,\n" +
            "user_id integer(4) NOT NULL,\n" +
            "master_name varchar(255) NOT NULL,\n" +
            "master_host varchar(255) NOT NULL,\n" +
            "master_port integer(4) NOT NULL,\n" +
            "flags varchar(255) NOT NULL,\n" +
            "s_down_time bigint(20) NOT NULL,\n" +
            "o_down_time bigint(20) NOT NULL,\n" +
            "down_after_milliseconds bigint(20) NOT NULL,\n" +
            "num_slaves integer(4) NOT NULL,\n" +
            "quorum integer(4) NOT NULL,\n" +
            "failover_timeout bigint(4) NOT NULL,\n" +
            "parallel_syncs integer(4) NOT NULL,\n" +
            "in_sentinel tinyint(1) NOT NULL,\n" +
            "update_time datetime(0) NOT NULL,\n" +
            "state varchar(50) NOT NULL,\n" +
            "PRIMARY KEY (sentinel_master_id)\n" +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createSentinelMastersTable();
}
