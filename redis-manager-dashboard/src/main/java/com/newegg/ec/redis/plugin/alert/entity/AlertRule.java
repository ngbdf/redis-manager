package com.newegg.ec.redis.plugin.alert.entity;

import java.sql.Timestamp;


/**
 * 规则可指定
 *
 * @author Jay.H.Zou
 * @date 2019/7/29
 */
public class AlertRule {

    private Integer ruleId;

    private Integer groupId;

    /**
     * 告警配置
     */
    private String ruleKey;
    /**
     * 告警阈值
     */
    private double ruleValue;

    /**
     * 比较类型
     * 0: 相等
     * 1: 大于
     * -1: 小于
     */
    private Integer compareType;

    /**
     * 检测周期, minute
     */
    private Integer checkCycle;

    /**
     * 规则是否可用
     */
    private boolean valid;

    /**
     * 是否是全组
     */
    private boolean global;

    private String ruleInfo;

    private Timestamp updateTime;

    private Timestamp lastCheckTime;

    public Integer getRuleId() {
        return ruleId;
    }

    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getRuleKey() {
        return ruleKey;
    }

    public void setRuleKey(String ruleKey) {
        this.ruleKey = ruleKey;
    }

    public double getRuleValue() {
        return ruleValue;
    }

    public void setRuleValue(double ruleValue) {
        this.ruleValue = ruleValue;
    }

    public Integer getCompareType() {
        return compareType;
    }

    public void setCompareType(Integer compareType) {
        this.compareType = compareType;
    }

    public Integer getCheckCycle() {
        return checkCycle;
    }

    public void setCheckCycle(Integer checkCycle) {
        this.checkCycle = checkCycle;
    }

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
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
