package com.newegg.ec.redis.entity;

/**
 * Mark node info type
 *
 * @author Jay.H.Zou
 * @date 7/23/2019
 */
public enum  NodeInfoType {
    /** host node info */
    NODE,

    /** monitor default: calculate node info */
    AVG_MINUTE,

    AVG_HOUR;


}
