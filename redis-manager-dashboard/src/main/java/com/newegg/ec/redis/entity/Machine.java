package com.newegg.ec.redis.entity;

import java.sql.Timestamp;

/**
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public class Machine {

    private Integer machineId;

    private String machineGroupName;

    private Integer groupId;

    private String host;

    private String userName;

    private String password;

    private String token;

    private int machineType;

    private String machineInfo;

    private Timestamp updateTime;

    public Machine() {
    }

    public Machine(String host) {
        this.host = host;
    }


    public Integer getMachineId() {
        return machineId;
    }

    public void setMachineId(Integer machineId) {
        this.machineId = machineId;
    }

    public String getMachineGroupName() {
        return machineGroupName;
    }

    public void setMachineGroupName(String machineGroupName) {
        this.machineGroupName = machineGroupName;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getMachineType() {
        return machineType;
    }

    public void setMachineType(int machineType) {
        this.machineType = machineType;
    }

    public String getMachineInfo() {
        return machineInfo;
    }

    public void setMachineInfo(String machineInfo) {
        this.machineInfo = machineInfo;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
