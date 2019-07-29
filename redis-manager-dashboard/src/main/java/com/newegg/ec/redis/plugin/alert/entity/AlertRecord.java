package com.newegg.ec.redis.plugin.alert.entity;


import java.sql.Timestamp;

/**
 * @author Jay.H.Zou
 * @date 2019/7/29
 */
public class AlertRecord extends AlertRule {

    private String recordId;

    private String groupName;

    private String clusterName;

    private String redisNode;

    private String alertRule;

    private String currentData;

    private Timestamp updateTime;

}
