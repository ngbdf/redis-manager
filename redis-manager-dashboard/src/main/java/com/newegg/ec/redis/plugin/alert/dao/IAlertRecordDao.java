package com.newegg.ec.redis.plugin.alert.dao;

import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;
import org.apache.ibatis.annotations.*;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/29
 */
@Mapper
public interface IAlertRecordDao {

    @Select("SELECT * FROM alert_record WHERE cluster_id = #{clusterId}")
    List<AlertRecord> selectAlertRecordByClusterId(Integer clusterId);

    @Insert("<script>" +
            "INSERT INTO alert_record (group_id, group_name, cluster_id, cluster_name, rule_id, redis_node, " +
            "alert_rule, actual_data, check_cycle, rule_info, update_time) " +
            "VALUES " +
            "<foreach item='alertRecord' collection='alertRecordList' separator=','>" +
            "(#{alertRecord.groupId}, #{alertRecord.groupName}, #{alertRecord.clusterId}, #{alertRecord.clusterName}, #{alertRecord.ruleId}, #{alertRecord.redisNode}, " +
            "#{alertRecord.alertRule}, #{alertRecord.actualData}, #{alertRecord.checkCycle}, #{alertRecord.ruleInfo}, NOW())" +
            "</foreach>" +
            "</script>")
    int insertAlertRecordBatch(@Param("alertRecordList") List<AlertRecord> alertRecordList);

    @Select("SELECT COUNT(record_id) FROM alert_record WHERE group_id = #{groupId}")
    int countAlertRecordNumber(Integer groupId);

    @Delete("DELETE FROM alert_record WHERE record_id = #{ruleId}")
    int deleteAlertRecordById(Integer recordId);

    @Delete("<script>" +
            "DELETE FROM alert_record WHERE record_id IN " +
            "<foreach item='ruleId' collection='recordIdList' open='(' separator=',' close=')'>" +
            "#{ruleId}" +
            "</foreach>" +
            "</script>")
    int deleteAlertRecordByIds(@Param("recordIdList") List<Integer> recordIdList);

    @Delete("DELETE FROM alert_record WHERE update_time &lt; #{oldestTime}")
    int deleteAlertRecordByTime(Timestamp oldestTime);

    @Select("create TABLE IF NOT EXISTS `alert_record` (" +
            "record_id integer(4) NOT NULL AUTO_INCREMENT, " +
            "group_id integer(4) NOT NULL, " +
            "group_name varchar(255) NOT NULL, " +
            "cluster_id integer(4) NOT NULL, " +
            "cluster_name varchar(255) NOT NULL, " +
            "rule_id integer(4) NOT NULL, " +
            "redis_node varchar(50) NOT NULL, " +
            "alert_rule varchar(50) NOT NULL, " +
            "actual_data varchar(50) NOT NULL, " +
            "check_cycle integer(4) NOT NULL, " +
            "rule_info varchar(255) DEFAULT NULL, " +
            "update_time datetime(0) NOT NULL, " +
            "PRIMARY KEY (record_id) " +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createAlertRecordTable();
}
