package com.newegg.ec.redis.entity;

import java.sql.Timestamp;
import java.util.List;


/**
 * @author Jay.H.Zou
 * @date 7/23/2019
 */
public class NodeInfoParam {

    private Integer clusterId;

    private Integer timeType;

    private String infoItem;

    private Timestamp startTime;

    private Timestamp endTime;

    private String node;

    private List<String> nodeList;

    public NodeInfoParam() {
    }

    public NodeInfoParam(Integer clusterId, Integer timeType) {
        this(clusterId, timeType, null);
    }

    public NodeInfoParam(Integer clusterId, Integer timeType, String node) {
        this(clusterId, timeType, null, null, node);
    }

    public NodeInfoParam(Integer clusterId, Timestamp startTime, Timestamp endTime) {
        this(clusterId, null, startTime, endTime, null);
    }

    public NodeInfoParam(Integer clusterId, Integer timeType, Timestamp startTime, Timestamp endTime, String node) {
        this.clusterId = clusterId;
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

    public Integer getTimeType() {
        return timeType;
    }

    public void setTimeType(Integer timeType) {
        this.timeType = timeType;
    }

    public String getInfoItem() {
        return infoItem;
    }

    public void setInfoItem(String infoItem) {
        this.infoItem = infoItem;
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

    public List<String> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<String> nodeList) {
        this.nodeList = nodeList;
    }

    @Override
    public String toString() {
        return "NodeInfoParam{" +
                "clusterId=" + clusterId +
                ", timeType=" + timeType +
                ", infoItem='" + infoItem + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", node='" + node + '\'' +
                ", nodeList=" + nodeList +
                '}';
    }
}
