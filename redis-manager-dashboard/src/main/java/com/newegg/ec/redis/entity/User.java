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

    private Integer userRole;

    private String avatar;

    private String email;

    private String mobile;

    private Timestamp updateTime;

    public static class UserRole {

        /** super admin: manage all groups and members */
        public static final int SUPER_ADMIN = 0;

        /** admin: manage the group and member */
        public static final int ADMIN = 1;

        /** only read */
        public static final int MEMBER = 2;
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

    public Integer getUserRole() {
        return userRole;
    }

    public void setUserRole(Integer userRole) {
        this.userRole = userRole;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
