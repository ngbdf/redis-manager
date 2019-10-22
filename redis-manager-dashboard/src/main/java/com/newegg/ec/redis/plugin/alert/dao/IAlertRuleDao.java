package com.newegg.ec.redis.plugin.alert.dao;

import com.newegg.ec.redis.plugin.alert.entity.AlertRule;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
@Mapper
public interface IAlertRuleDao {

    @Select("SELECT * FROM alert_rule WHERE group_id = #{groupId}")
    List<AlertRule> selectAlertRuleByGroupId(Integer groupId);

    @Select("<script>" +
            "SELECT * FROM alert_rule WHERE global = 1 " +
            "AND group_id = #{groupId} " +
            "<if test='ruleIdList != null and ruleIdList.size() > 0'>" +
            "OR rule_id IN " +
            "<foreach item='ruleId' collection='ruleIdList' open='(' separator=',' close=')'>" +
            "#{ruleId}" +
            "</foreach>" +
            "</if>" +
            "</script>")
    List<AlertRule> selectAlertRuleByIds(@Param("groupId") Integer groupId, @Param("ruleIdList") List<Integer> ruleIdList);

    @Select("<script>" +
            "SELECT * FROM alert_rule WHERE group_id = #{groupId} " +
            "AND global = 0 " +
            "<if test='ruleIdList != null and ruleIdList.size > 0'>" +
            "AND rule_id NOT IN " +
            "<foreach item='ruleId' collection='ruleIdList' open='(' separator=',' close=')'>" +
            "#{ruleId}" +
            "</foreach>" +
            "</if>" +
            "</script>")
    List<AlertRule> selectAlertRuleNotUsed(@Param("groupId") Integer groupId, @Param("ruleIdList") List<Integer> ruleIdList);

    @Select("SELECT * FROM alert_rule WHERE rule_id = #{ruleId}")
    AlertRule selectAlertRuleById(Integer ruleId);

    @Insert("INSERT INTO alert_rule (group_id, rule_key, rule_value, compare_type, check_cycle, " +
            "valid, global, rule_info, update_time, last_check_time) " +
            "VALUES (#{groupId}, #{ruleKey}, #{ruleValue}, #{compareType}, #{checkCycle}, " +
            "#{valid}, #{global}, #{ruleInfo}, NOW(), NOW())")
    int insertAlertRule(AlertRule alertRule);

    @Update("UPDATE alert_rule SET group_id = #{groupId}, rule_key = #{ruleKey}, rule_value = #{ruleValue}, " +
            "compare_type = #{compareType}, check_cycle = #{checkCycle}, valid = #{valid}, global = #{global}, rule_info = #{ruleInfo}, " +
            "update_time = NOW() " +
            "WHERE rule_id = #{ruleId}")
    int updateAlertRule(AlertRule alertRule);

    @Update("<script>" +
            "UPDATE alert_rule SET last_check_time = NOW() " +
            "WHERE rule_id IN " +
            "<foreach item='ruleId' collection='ruleIdList' open='(' separator=',' close=')'>" +
            "#{ruleId}" +
            "</foreach>" +
            "</script>")
    int updateAlertRuleLastCheckTime(@Param("ruleIdList") List<Integer> ruleIdList);

    @Delete("DELETE FROM alert_rule WHERE rule_id = #{ruleId}")
    int deleteAlertRuleById(Integer ruleId);

    @Delete("DELETE FROM alert_rule WHERE group_id = #{groupId}")
    int deleteAlertRuleByGroupId(Integer groupId);

    @Select("create TABLE IF NOT EXISTS `alert_rule` (" +
            "rule_id integer(4) NOT NULL AUTO_INCREMENT, " +
            "group_id integer(4) NOT NULL, " +
            "rule_key varchar(50) NOT NULL, " +
            "rule_value varchar(50) NOT NULL, " +
            "compare_type integer(2) NOT NULL, " +
            "check_cycle integer(4) NOT NULL, " +
            "valid tinyint(1) NOT NULL, " +
            "global tinyint(1) NOT NULL, " +
            "rule_info varchar(255) DEFAULT NULL, " +
            "update_time datetime(0) NOT NULL, " +
            "last_check_time datetime(0) NOT NULL, " +
            "PRIMARY KEY (rule_id) " +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createAlertChannelTable();

}
