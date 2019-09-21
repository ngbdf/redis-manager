package com.newegg.ec.redis.entity;

import java.sql.Timestamp;

/**
 *
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public class Group {

    private Integer groupId;

    private Integer userId;

    private String groupName;

    private String groupInfo;

    private Timestamp updateTime;

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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

    @Override
    public String toString() {
        return "Group{" +
                "groupId=" + groupId +
                ", userId=" + userId +
                ", groupName='" + groupName + '\'' +
                ", groupInfo='" + groupInfo + '\'' +
                ", updateTime=" + updateTime +
                '}';
    }
}
