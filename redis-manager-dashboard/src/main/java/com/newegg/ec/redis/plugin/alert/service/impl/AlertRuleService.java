package com.newegg.ec.redis.plugin.alert.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.plugin.alert.dao.IAlertRuleDao;
import com.newegg.ec.redis.plugin.alert.entity.AlertRule;
import com.newegg.ec.redis.plugin.alert.service.IAlertRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/8/31
 */
public class AlertRuleService implements IAlertRuleService {

    private static final Logger logger = LoggerFactory.getLogger(AlertRuleService.class);

    @Autowired
    private IAlertRuleDao alertRuleDao;

    @Override
    public List<AlertRule> getAlertRuleIds(List<String> ruleIdList) {
        try {
            return alertRuleDao.selectAlertRuleIds(ruleIdList);
        } catch (Exception e) {
            logger.error("Get alert rule by ids failed, rule ids = " + ruleIdList, e);
            return null;
        }
    }

    @Override
    public List<AlertRule> getAlertRuleListByGroupId(String groupId) {
        if (Strings.isNullOrEmpty(groupId)) {
            return null;
        }
        try {
            return alertRuleDao.selectAlertRuleListByGroupId(groupId);
        } catch (Exception e) {
            logger.error("Get alert rule by group id failed, group id = " + groupId, e);
            return null;
        }
    }

    @Override
    public List<AlertRule> getAlertRuleListByClusterId(String groupId, String clusterId) {
        if (Strings.isNullOrEmpty(groupId) || Strings.isNullOrEmpty(clusterId)) {
            return null;
        }
        try {
            return alertRuleDao.selectAlertRuleListByClusterId(groupId, clusterId);
        } catch (Exception e) {
            logger.error("Get alert rule by group id and cluster id failed, group id = " + groupId + ", cluster id = " + clusterId, e);
            return null;
        }
    }

    @Override
    public boolean addAlertRule(AlertRule alertRule) {
        try {
            int row = alertRuleDao.insertAlertRule(alertRule);
            return row > 0;
        } catch (Exception e) {
            logger.error("Add alert rule failed, " + alertRule, e);
            return false;
        }
    }

    @Override
    public boolean updateAlertRule(AlertRule alertRule) {
        try {
            int row = alertRuleDao.updateAlertRule(alertRule);
            return row > 0;
        } catch (Exception e) {
            logger.error("Update alert rule failed, " + alertRule, e);
            return false;
        }
    }

    @Override
    public boolean deleteAlertRuleByIds(List<String> ruleIdList) {
        try {
            alertRuleDao.deleteAlertRuleByIdList(ruleIdList);
            return true;
        } catch (Exception e) {
            logger.error("Delete alert rule failed, rule ids = " + ruleIdList, e);
            return false;
        }
    }

    @Override
    public boolean deleteAlertRuleByGroupId(String groupId) {
        try {
            int row = alertRuleDao.deleteAlertRuleByGroupId(groupId);
            return row > 0;
        } catch (Exception e) {
            logger.error("Delete alert rule failed, group id = " + groupId, e);
            return false;
        }
    }
}
