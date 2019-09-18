package com.newegg.ec.redis.entity;

import java.sql.Timestamp;

/**
 *
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public class Group {

    private Integer groupId;

    private String groupName;

    private String groupInfo;

    private Timestamp updateTime;

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
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
}
