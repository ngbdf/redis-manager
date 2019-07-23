package com.newegg.ec.redis.entity;

import java.sql.Timestamp;
import java.time.Duration;

/**
 * @author Jay.H.Zou
 * @date 7/23/2019
 */
public class NodeInfoQueryParam {

    private String tableName;

    private long duration;

    private NodeInfoType nodeInfoType;

    private Timestamp startTime;

    private Timestamp endTime;

    public NodeInfoQueryParam(String tableName, Duration duration, NodeInfoType nodeInfoType) {
        this.tableName = tableName;
        this.duration = duration.toMillis();
        this.nodeInfoType = nodeInfoType;
    }

    public NodeInfoQueryParam(String tableName, NodeInfoType nodeInfoType, Timestamp startTime, Timestamp endTime) {
        this.tableName = tableName;
        this.nodeInfoType = nodeInfoType;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
