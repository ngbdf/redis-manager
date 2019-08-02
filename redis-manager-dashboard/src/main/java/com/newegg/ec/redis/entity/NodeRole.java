package com.newegg.ec.redis.entity;

/**
 * @author Jay.H.Zou
 * @date 2019/8/2
 */
public enum NodeRole {

    /**
     * redis master
     */
    MASTER,

    /**
     * slave
     */
    SLAVE,

    /**
     * replicate: for redis 5
     */
    REPLICATE;

}
