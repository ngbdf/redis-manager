package com.newegg.ec.redis.plugin.alert.service.impl;

import com.newegg.ec.redis.plugin.alert.dao.IAlertRuleDao;
import com.newegg.ec.redis.plugin.alert.entity.AlertRule;
import com.newegg.ec.redis.plugin.alert.service.IAlertRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/8/31
 */
@Service
public class AlertRuleService implements IAlertRuleService {

    private static final Logger logger = LoggerFactory.getLogger(AlertRuleService.class);

    @Autowired
    private IAlertRuleDao alertRuleDao;

    @Override
    public AlertRule getAlertRuleById(Integer ruleId) {
        try {
            return alertRuleDao.selectAlertRuleById(ruleId);
        } catch (Exception e) {
            logger.error("Get alert rule by ids failed, rule id = " + ruleId, e);
            return null;
        }
    }

    @Override
    public List<AlertRule> getAlertRuleByIds(Integer groupId, List<Integer> ruleIdList) {
        try {
            return alertRuleDao.selectAlertRuleByIds(groupId,ruleIdList);
        } catch (Exception e) {
            logger.error("Get alert rule by ids failed.", e);
            return null;
        }
    }

    @Override
    public List<AlertRule> getAlertRuleByGroupId(Integer groupId) {
        try {
            return alertRuleDao.selectAlertRuleByGroupId(groupId);
        } catch (Exception e) {
            logger.error("Get alert rule by group id failed, group id = " + groupId, e);
            return null;
        }
    }

    @Override
    public List<AlertRule> getAlertRuleNotUsed(Integer groupId, List<Integer> ruleIdList) {
        try {
            return alertRuleDao.selectAlertRuleNotUsed(groupId, ruleIdList);
        } catch (Exception e) {
            logger.error("Get alert rule not used failed.", e);
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
            logger.error("Update alert rule failed.", e);
            return false;
        }
    }

    @Override
    public boolean deleteAlertRuleById(Integer ruleId) {
        try {
            alertRuleDao.deleteAlertRuleById(ruleId);
            return true;
        } catch (Exception e) {
            logger.error("Delete alert rule failed.", e);
            return false;
        }
    }

    @Override
    public boolean deleteAlertRuleByGroupId(Integer groupId) {
        try {
            int row = alertRuleDao.deleteAlertRuleByGroupId(groupId);
            return row > 0;
        } catch (Exception e) {
            logger.error("Delete alert rule failed, group id = " + groupId, e);
            return false;
        }
    }

}
