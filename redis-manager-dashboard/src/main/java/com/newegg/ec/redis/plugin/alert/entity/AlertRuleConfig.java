package com.newegg.ec.redis.plugin.alert.entity;

import java.sql.Timestamp;

/**
 * @author Jay.H.Zou
 * @date 2019/7/29
 */
public class AlertRuleConfig {

    private String alertConfigId;

    private String gorupId;

    /**
     * 比较类型
     * 0: 相等
     * 1: 大于
     * -1: 小于
     * 2: 不等于
     */
    private int compareType;

    /**
     * 检测周期
     */
    private int checkCycle;

    /**
     * 规则是否可用
     */
    private boolean status;

    /**
     * 是否是全组
     */
    private boolean type;

    private String configInfo;

    private Timestamp updateTime;

}
