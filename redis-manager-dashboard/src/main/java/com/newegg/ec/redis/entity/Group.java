package com.newegg.ec.redis.entity;

import java.sql.Timestamp;

/**
 *
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public class Group {

    private int groupId;

    private String groupName;

    private int clusterNumber;

    private int goodClusterNumber;

    private int userNumber;

    private String groupInfo;

    private Timestamp updateTime;

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getClusterNumber() {
        return clusterNumber;
    }

    public void setClusterNumber(int clusterNumber) {
        this.clusterNumber = clusterNumber;
    }

    public int getGoodClusterNumber() {
        return goodClusterNumber;
    }

    public void setGoodClusterNumber(int goodClusterNumber) {
        this.goodClusterNumber = goodClusterNumber;
    }

    public int getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(int userNumber) {
        this.userNumber = userNumber;
    }

    public String getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(String groupInfo) {
        this.groupInfo = groupInfo;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
