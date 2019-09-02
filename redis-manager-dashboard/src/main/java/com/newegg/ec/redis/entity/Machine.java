package com.newegg.ec.redis.entity;

/**
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public class Machine {

    private int machineId;

    private String machineGroupName;

    private String groupId;

    private String host;

    private String userName;

    private String password;

    private String token;

    private String machineInfo;

    public Machine(){}

    public Machine(String host) {
        this.host = host;
    }

    public int getMachineId() {
        return machineId;
    }

    public void setMachineId(int machineId) {
        this.machineId = machineId;
    }

    public String getMachineGroupName() {
        return machineGroupName;
    }

    public void setMachineGroupName(String machineGroupName) {
        this.machineGroupName = machineGroupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
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

    public String getMachineInfo() {
        return machineInfo;
    }

    public void setMachineInfo(String machineInfo) {
        this.machineInfo = machineInfo;
    }
}
