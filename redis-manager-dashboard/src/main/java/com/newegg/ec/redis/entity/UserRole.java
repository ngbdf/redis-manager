package com.newegg.ec.redis.entity;

/**
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public enum  UserRole {

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
