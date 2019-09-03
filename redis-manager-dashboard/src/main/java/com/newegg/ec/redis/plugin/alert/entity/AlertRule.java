package com.newegg.ec.redis.plugin.alert.entity;

import java.sql.Timestamp;

/**
 * 规则可指定
 *
 * @author Jay.H.Zou
 * @date 2019/7/29
 */
public class AlertRule {

    private int ruleId;

    private int groupId;

    private int clusterId;

    /**
     * 告警配置
     */
    private String alertKey;
    /**
     * 告警阈值
     */
    private double alertValue;

    /**
     * 比较类型
     * 0: 相等
     * 1: 大于
     * -1: 小于
     * 2: 不等于
     */
    private int compareType;

    /**
     * 检测周期, minute
     */
    private int checkCycle;

    /**
     * 规则是否可用
     */
    private boolean valid;

    private boolean atAll;

    /**
     * 是否是全组
     */
    private boolean global;

    private String ruleInfo;

    private Timestamp updateTime;

    private Timestamp lastCheckTime;

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public String getAlertKey() {
        return alertKey;
    }

    public void setAlertKey(String alertKey) {
        this.alertKey = alertKey;
    }

    public double getAlertValue() {
        return alertValue;
    }

    public void setAlertValue(double alertValue) {
        this.alertValue = alertValue;
    }

    public int getCompareType() {
        return compareType;
    }

    public void setCompareType(int compareType) {
        this.compareType = compareType;
    }

    public int getCheckCycle() {
        return checkCycle;
    }

    public void setCheckCycle(int checkCycle) {
        this.checkCycle = checkCycle;
    }

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isAtAll() {
        return atAll;
    }

    public void setAtAll(boolean atAll) {
        this.atAll = atAll;
    }

    public boolean getGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public String getRuleInfo() {
        return ruleInfo;
    }

    public void setRuleInfo(String ruleInfo) {
        this.ruleInfo = ruleInfo;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public Timestamp getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(Timestamp lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }
}
