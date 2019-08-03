package com.newegg.ec.redis.entity;

import java.sql.Timestamp;


/**
 * @author Jay.H.Zou
 * @date 7/23/2019
 */
public class NodeInfoParam {

    private int clusterId;

    private NodeInfoType.DataType dataType;

    private NodeInfoType.TimeType timeType;

    private Timestamp startTime;

    private Timestamp endTime;

    private String node;

    public NodeInfoParam() {
    }

    public NodeInfoParam(int clusterId, NodeInfoType.DataType dataType, NodeInfoType.TimeType timeType, String node) {
        this(clusterId, dataType, timeType, null, null, node);
    }

    public NodeInfoParam(int clusterId, NodeInfoType.DataType dataType, NodeInfoType.TimeType timeType, Timestamp startTime, Timestamp endTime, String node) {
        this.clusterId = clusterId;
        this.dataType = dataType;
        this.timeType = timeType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.node = node;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public NodeInfoType.DataType getDataType() {
        return dataType;
    }

    public void setDataType(NodeInfoType.DataType dataType) {
        this.dataType = dataType;
    }

    public NodeInfoType.TimeType getTimeType() {
        return timeType;
    }

    public void setTimeType(NodeInfoType.TimeType timeType) {
        this.timeType = timeType;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("NodeInfoParam{");
        sb.append("clusterId=").append(clusterId);
        sb.append(", dataType=").append(dataType);
        sb.append(", timeType=").append(timeType);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", node='").append(node).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
