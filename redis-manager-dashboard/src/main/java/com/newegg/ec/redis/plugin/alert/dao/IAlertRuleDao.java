package com.newegg.ec.redis.plugin.alert.dao;

import com.newegg.ec.redis.plugin.alert.entity.AlertRule;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public interface IAlertRuleDao {

    List<AlertRule> selectAlertRuleIds(List<Integer> ruleIdList);
    
    List<AlertRule> selectAlertRuleListByGroupId(int groupId);

    List<AlertRule> selectAlertRuleListByClusterId(int groupId, int clusterId);

    int insertAlertRule(AlertRule alertRule);

    int updateAlertRule(AlertRule alertRule);

    int updateAlertRuleLastCheckTime(List<Integer> ruleIdList);

    int deleteAlertRuleByIdList(List<Integer> ruleIdList);

    int deleteAlertRuleByClusterId(int clusterId);

    int deleteAlertRuleByGroupId(int groupId);

}
