package com.newegg.ec.redis.plugin.alert.service;

import com.newegg.ec.redis.plugin.alert.entity.AlertRule;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 8/31/2019
 */
public interface IAlertRuleService {

    List<AlertRule> getAlertRuleIds(List<Integer> ruleIdList);
    
    List<AlertRule> getAlertRuleListByGroupId(int groupId);

    List<AlertRule> getAlertRuleListByClusterId(int groupId, int clusterId);

    boolean addAlertRule(AlertRule alertRule);

    boolean updateAlertRule(AlertRule alertRule);

    boolean updateAlertRuleLastCheckTime(List<Integer> ruleIdList);

    boolean deleteAlertRuleByIds(List<Integer> ruleIdList);

    boolean deleteAlertRuleByGroupId(int groupId);

}
