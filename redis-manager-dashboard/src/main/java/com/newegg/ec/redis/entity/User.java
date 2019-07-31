package com.newegg.ec.redis.entity;

import java.sql.Timestamp;

/**
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public class User {

    private String userId;

    private String groupId;

    private String userName;

    private String password;

    private String token;

    private UserRole userRole;

    private String headPic;

    private String email;

    private String phoneNumber;

    /**
     * 0: 系统内部用户
     * 1: 外部用户
     */
    private int userType;

    private Timestamp updateTime;
}
