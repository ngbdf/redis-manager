package com.newegg.ec.redis.plugin.alert.entity;

import java.sql.Timestamp;

/**
 * @author Jay.H.Zou
 * @date 2019/7/29
 */
public class AlertRule {

    private String ruleId;

    private String groupId;

    private String clusterId;

    private String channelIds;

    /**
     * 告警配置
     */
    private String alertKey;
    /**
     * 告警阈值
     */
    private double alertValue;

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
    private boolean isGlobal;

    private String ruleInfo;

    private Timestamp updateTime;

    private Timestamp lastCheckTime;
}
