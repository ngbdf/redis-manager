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

    @Select("")
    List<SentinelMaster> selectSentinelMasterByClusterId(Integer clusterId);

    @Select("")
    SentinelMaster selectSentinelMasterByMasterName(Integer clusterId, String masterName);

    @Select("")
    SentinelMaster selectSentinelMasterById(Integer sentinelMasterId);

    @Update("")
    int updateSentinelMaster(SentinelMaster sentinelMaster);

    @Insert("")
    int insertSentinelMaster(SentinelMaster sentinelMaster);

    @Delete("")
    int deleteSentinelMaster(SentinelMaster sentinelMaster);

    @Delete("")
    int deleteSentinelMasterById(Integer sentinelMasterId);

    @Delete("")
    int deleteSentinelMasterByClusterId(Integer clusterId);

    @Select("create TABLE IF NOT EXISTS `sentinel_masters` (\n" +
            "sentinel_master_id integer(4) NOT NULL AUTO_INCREMENT,\n" +
            "cluster_id integer(4) NOT NULL,\n" +
            "group_id integer(4) NOT NULL,\n" +
            "user_id integer(4) NOT NULL,\n" +
            "master_name varchar(255) NOT NULL,\n" +
            "master_host varchar(255) NOT NULL,\n" +
            "master_port integer(4) NOT NULL,\n" +
            "last_master_node varchar(255) NOT NULL,\n" +
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
            "PRIMARY KEY (sentinel_master_id)\n" +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createSentinelMastersTable();
}
