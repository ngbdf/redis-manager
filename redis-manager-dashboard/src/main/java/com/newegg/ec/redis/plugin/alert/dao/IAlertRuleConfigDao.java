package com.newegg.ec.redis.plugin.alert.dao;

import com.newegg.ec.redis.plugin.alert.entity.AlertRuleConfig;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/29
 */
public interface IAlertRuleConfigDao {

    List<AlertRuleConfig> selectAlertRuleConfigByGroupId(String groupId);

    List<AlertRuleConfig> selectAlertRuleConfigByClusterId(String clusterId);

    AlertRuleConfig selectAlertRuleConfigById(String configId);

    int insertAlertRuleConfig(AlertRuleConfig alertRuleConfig);

    int updateAlertRuleConfig(AlertRuleConfig alertRuleConfig);

    int deleteAlertRuleConfigById(String configId);

    int deleteAlertRuleConfigByGroupId(String groupId);

    int deleteAlertRuleConfigByClusterId(String clusterId);

}
