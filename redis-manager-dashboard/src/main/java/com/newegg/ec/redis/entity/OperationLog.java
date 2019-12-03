package com.newegg.ec.redis.entity;

import java.sql.Timestamp;

/**
 * @author fw13
 * @date 2019/11/20 18:08
 */
public class OperationLog {

    /**
     * 自增主键
     */
    private Integer logId;

    /**
     * 用户组id
     */
    private Integer groupId;

    /**
     * 用户组名
     */
    private String groupName;

    /**
     * 用户实际操作的groupId，该参数需要从请求参数中解析
     * 不会在页面显示
     * operationGroupId 即用户在页面上方选择的group
     */
    private Integer operationGroupId;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户的操作记录   例如：import cluster
     */
    private String operationInfo;

    /**
     * 用户的请求参数，不做展示，只会存db，出问题时可以看具体操作
     */
    private String requestParams;

    /**
     * 操作用户的IP
     */
    private String userIp;

    /**
     * 操作时间
     */
    private Timestamp logTime;

    public OperationLog() {
    }

    public OperationLog(Integer groupId, Integer userId,String userIp, Integer operationGroupId, String operationInfo,String requestParams, Timestamp logTime) {
        this.groupId = groupId;
        this.userId = userId;
        this.userIp = userIp;
        this.operationGroupId = operationGroupId;
        this.operationInfo = operationInfo;
        this.requestParams = requestParams;
        this.logTime = logTime;
    }

    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOperationInfo() {
        return operationInfo;
    }

    public void setOperationInfo(String operationInfo) {
        this.operationInfo = operationInfo;
    }

    public Timestamp getLogTime() {
        return logTime;
    }

    public void setLogTime(Timestamp logTime) {
        this.logTime = logTime;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public Integer getOperationGroupId() {
        return operationGroupId;
    }

    public void setOperationGroupId(Integer operationGroupId) {
        this.operationGroupId = operationGroupId;
    }
}
