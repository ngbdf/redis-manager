package com.newegg.ec.redis.entity;

import java.sql.Timestamp;
import java.util.List;


/**
 * @author Jay.H.Zou
 * @date 7/23/2019
 */
public class NodeInfoParam {

    private Integer clusterId;

    private Integer dataType;

    private Integer timeType;

    private List<String> columnList;

    private Timestamp startTime;

    private Timestamp endTime;

    private String node;

    public NodeInfoParam() {
    }

    public NodeInfoParam(Integer clusterId, Integer timeType) {
        this(clusterId, null, timeType, null);
    }

    public NodeInfoParam(Integer clusterId, Integer dataType, Integer timeType, String node) {
        this(clusterId, dataType, timeType, null, null, node);
    }

    public NodeInfoParam(Integer clusterId, Timestamp startTime, Timestamp endTime) {
        this(clusterId, null, null, startTime, endTime, null);
    }

    public NodeInfoParam(Integer clusterId, Integer dataType, Integer timeType, Timestamp startTime, Timestamp endTime, String node) {
        this.clusterId = clusterId;
        this.dataType = dataType;
        this.timeType = timeType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.node = node;
    }

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public Integer getTimeType() {
        return timeType;
    }

    public void setTimeType(Integer timeType) {
        this.timeType = timeType;
    }

    public List<String> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<String> columnList) {
        this.columnList = columnList;
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
        return "NodeInfoParam{" +
                "clusterId=" + clusterId +
                ", dataType=" + dataType +
                ", timeType=" + timeType +
                ", columnList=" + columnList +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", node='" + node + '\'' +
                '}';
    }
}
