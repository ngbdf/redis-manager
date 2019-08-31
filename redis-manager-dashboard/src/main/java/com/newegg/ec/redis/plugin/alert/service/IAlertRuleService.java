package com.newegg.ec.redis.plugin.alert.service;

import com.newegg.ec.redis.plugin.alert.entity.AlertRule;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 8/31/2019
 */
public interface IAlertRuleService {

    List<AlertRule> getAlertRuleIds(List<String> ruleIdList);
    
    List<AlertRule> getAlertRuleListByGroupId(String groupId);

    List<AlertRule> getAlertRuleListByClusterId(String groupId, String clusterId);

    boolean addAlertRule(AlertRule alertRule);

    boolean updateAlertRule(AlertRule alertRule);

    boolean deleteAlertRuleByIds(List<String> ruleIdList);

    boolean deleteAlertRuleByGroupId(String groupId);

}
