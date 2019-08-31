package com.newegg.ec.redis.plugin.alert.dao;

import com.newegg.ec.redis.plugin.alert.entity.AlertRule;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public interface IAlertRuleDao {

    List<AlertRule> selectAlertRuleIds(List<String> ruleIdList);
    
    List<AlertRule> selectAlertRuleListByGroupId(String groupId);

    List<AlertRule> selectAlertRuleListByClusterId(String groupId, String clusterId);

    int insertAlertRule(AlertRule alertRule);

    int updateAlertRule(AlertRule alertRule);

    int deleteAlertRuleById(String ruleId);

    int deleteAlertRuleByClusterId(String clusterId);

    int deleteAlertRuleByGroupId(String groupId);

}
