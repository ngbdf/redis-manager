package com.newegg.ec.redis.plugin.alert.entity;

/**
 * @author Jay.H.Zou
 * @date 2019/7/29
 */
public class AlertRecord extends AlertRule {

    private int recordId;

    private String groupName;

    private String clusterName;

    private String redisNode;

    private String alertRule;

    private String actualData;

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getRedisNode() {
        return redisNode;
    }

    public void setRedisNode(String redisNode) {
        this.redisNode = redisNode;
    }

    public String getAlertRule() {
        return alertRule;
    }

    public void setAlertRule(String alertRule) {
        this.alertRule = alertRule;
    }

    public String getActualData() {
        return actualData;
    }

    public void setActualData(String actualData) {
        this.actualData = actualData;
    }
}
