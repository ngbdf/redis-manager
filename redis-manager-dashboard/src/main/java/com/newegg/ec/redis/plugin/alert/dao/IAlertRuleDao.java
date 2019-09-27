package com.newegg.ec.redis.plugin.alert.dao;

import com.newegg.ec.redis.plugin.alert.entity.AlertRule;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public interface IAlertRuleDao {

    @Select("SELECT * FROM alert_rule WHERE group_id = #{groupId}")
    List<AlertRule> selectAlertRuleByGroupId(Integer groupId);

    @Select("<script>" +
            "SELECT * FROM alert_rule WHERE rule_id IN " +
            "<foreach item='ruleId' collection='ruleIdList' open='(' separator=',' close=')'>" +
            "#{ruleId}" +
            "</foreach>)" +
            "</script>")
    List<AlertRule> selectAlertRuleByIds(@Param("ruleIdList") List<Integer> ruleIdList);

    @Insert("INSERT INTO alert_rule (group_id, alert_key, alert_value, compare_type, check_cycle, " +
            "valid, global, rule_info, update_time, last_check_time) " +
            "VALUES (#{groupId}, #{alertKey}, #{alertValue}, #{compareType}, #{checkCycle}, " +
            "#{valid}, #{global}, #{ruleInfo}, #{updateTime}, #{lastCheckTime})")
    int insertAlertRule(AlertRule alertRule);

    @Update("UPDATE alert_rule SET group_id = #{groupId}, alert_key = #{ruleKey}, alert_value = #{alertRule}, " +
            "compare_type = #{compareType}, valid = #{valid}, global = #{global}, rule_info = #{ruleInfo}, " +
            "update_time = #{updateTime}, last_check_time #{lastCheckTime} " +
            "WHERE rule_id = #{ruleId}")
    int updateAlertRule(AlertRule alertRule);

    int updateAlertRuleLastCheckTime(List<Integer> ruleIdList);

    int deleteAlertRuleByIdList(List<Integer> ruleIdList);

    int deleteAlertRuleByClusterId(int clusterId);

    int deleteAlertRuleByGroupId(int groupId);

}
