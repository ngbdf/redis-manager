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
    /**
     * 告警配置
     */
    private String alertKey;
    /**
     * 告警阈值
     */
    private double alertValue;

    private String ruleInfo;

    private Timestamp updateTime;

    private Timestamp lastCheckTime;
}
