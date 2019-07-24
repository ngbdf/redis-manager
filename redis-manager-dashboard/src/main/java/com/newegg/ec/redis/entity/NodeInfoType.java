package com.newegg.ec.redis.entity;

/**
 * Mark node info type
 *
 * @author Jay.H.Zou
 * @date 7/23/2019
 */
public class NodeInfoType {

    private NodeInfoType() {
    }

    public enum DataType {
        /**
         * host node info
         */
        NODE,

        /**
         * monitor default: calculate node info
         */
        AVG,

        MAX,

        MIN;
    }

    public enum TimeType {
        /**
         * monitor default:
         */
        MINUTE,

        HOUR;
    }

}
