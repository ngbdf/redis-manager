package com.newegg.ec.redis.plugin.alert.service;

import com.newegg.ec.redis.plugin.alert.entity.AlertRule;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 8/31/2019
 */
public interface IAlertRuleService {

    AlertRule getAlertRuleById(Integer ruleId);

    List<AlertRule> getAlertRuleByIds(List<Integer> ruleIdList);
    
    List<AlertRule> getAlertRuleListByGroupId(Integer groupId);

    boolean addAlertRule(AlertRule alertRule);

    boolean updateAlertRule(AlertRule alertRule);

    boolean updateAlertRuleLastCheckTime(List<Integer> ruleIdList);

    boolean deleteAlertRuleById(Integer ruleId);

    boolean deleteAlertRuleByGroupId(Integer groupId);

}
