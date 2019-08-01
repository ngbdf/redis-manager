package com.newegg.ec.redis.entity;

import java.sql.Timestamp;


/**
 * @author Jay.H.Zou
 * @date 7/23/2019
 */
public class NodeInfoQueryParam {

    private String clusterId;

    private String tableName;

    private NodeInfoType.DataType dataType;

    private NodeInfoType.TimeType timeType;

    private Timestamp startTime;

    private Timestamp endTime;

    public NodeInfoQueryParam(String tableName, NodeInfoType.DataType dataType, NodeInfoType.TimeType timeType,  Timestamp startTime, Timestamp endTime) {
        this.tableName = tableName;
        this.dataType = dataType;
        this.timeType = timeType;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
