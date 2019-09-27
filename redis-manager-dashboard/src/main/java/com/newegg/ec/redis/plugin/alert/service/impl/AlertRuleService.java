package com.newegg.ec.redis.plugin.alert.service.impl;

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
    public List<AlertRule> getAlertRuleIds(List<Integer> ruleIdList) {
        try {
            return alertRuleDao.selectAlertRuleByIds(ruleIdList);
        } catch (Exception e) {
            logger.error("Get alert rule by ids failed, rule ids = " + ruleIdList, e);
            return null;
        }
    }

    @Override
    public List<AlertRule> getAlertRuleListByGroupId(int groupId) {
        try {
            return alertRuleDao.selectAlertRuleByGroupId(groupId);
        } catch (Exception e) {
            logger.error("Get alert rule by group id failed, group id = " + groupId, e);
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
    public boolean updateAlertRuleLastCheckTime(List<Integer> ruleIdList) {
        try {
            int row = alertRuleDao.updateAlertRuleLastCheckTime(ruleIdList);
            return row > 0;
        } catch (Exception e) {
            logger.error("Update alert rule failed, rule id = " + ruleIdList, e);
            return false;
        }
    }

    @Override
    public boolean deleteAlertRuleByIds(List<Integer> ruleIdList) {
        try {
            alertRuleDao.deleteAlertRuleByIdList(ruleIdList);
            return true;
        } catch (Exception e) {
            logger.error("Delete alert rule failed, rule ids = " + ruleIdList, e);
            return false;
        }
    }

    @Override
    public boolean deleteAlertRuleByGroupId(int groupId) {
        try {
            int row = alertRuleDao.deleteAlertRuleByGroupId(groupId);
            return row > 0;
        } catch (Exception e) {
            logger.error("Delete alert rule failed, group id = " + groupId, e);
            return false;
        }
    }
}
