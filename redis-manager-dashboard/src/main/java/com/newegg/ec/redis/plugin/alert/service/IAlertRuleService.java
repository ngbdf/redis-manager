package com.newegg.ec.redis.plugin.alert.service;

import com.newegg.ec.redis.plugin.alert.entity.AlertRule;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 8/31/2019
 */
public interface IAlertRuleService {

    List<AlertRule> selectAlertRuleIds(List<String> ruleIdList);
    
    List<AlertRule> selectAlertRuleListByGroupId(String groupId);

    List<AlertRule> selectAlertRuleListByClusterId(String groupId, String clusterId);

    boolean addAlertRule(AlertRule alertRule);

    boolean updateAlertRule(AlertRule alertRule);

    boolean deleteAlertRuleById(String ruleId);

    boolean deleteAlertRuleByClusterId(String clusterId);

    boolean deleteAlertRuleByGroupId(String groupId);

}
