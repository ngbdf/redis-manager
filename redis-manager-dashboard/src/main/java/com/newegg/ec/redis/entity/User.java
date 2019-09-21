package com.newegg.ec.redis.entity;

import java.sql.Timestamp;

/**
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public class User {

    private Integer userId;

    private Integer groupId;

    private String userName;

    private String password;

    private String token;

    private UserRole userRole;

    private String headPic;

    private String email;

    private String mobile;

    /**
     * 0: 系统内部用户
     * 1: 外部用户
     */
    private int userType;

    private Timestamp updateTime;

    public enum UserRole {

        /**
         * super admin: manage all groups and members
         */
        SUPER_ADMIN,

        /**
         * admin: manage the group and member
         */
        ADMIN,

        /**
         * only read
         */
        MEMBER;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
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

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public String getHeadPic() {
        return headPic;
    }

    public void setHeadPic(String headPic) {
        this.headPic = headPic;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
