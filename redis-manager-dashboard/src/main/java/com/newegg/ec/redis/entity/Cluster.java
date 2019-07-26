package com.newegg.ec.redis.entity;

/**
 * 描述集群基本情况
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public class Cluster {

    private String clusterId;

    private String groupId;

    private String userId;

    private String token;

    private String clusterName;

    private String nodes;

    private String redisMode;

    private String os;

    private String version;

    /**
     * 集群 key 总数
     */
    private long totalKeys;

    /**
     * 集群 expire 总数
     */
    private long totalExpires;

    private String clusterPass;

    private ClusterType clusterType;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getNodes() {
        return nodes;
    }

    public void setNodes(String nodes) {
        this.nodes = nodes;
    }

    public String getRedisMode() {
        return redisMode;
    }

    public void setRedisMode(String redisMode) {
        this.redisMode = redisMode;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getTotalKeys() {
        return totalKeys;
    }

    public void setTotalKeys(long totalKeys) {
        this.totalKeys = totalKeys;
    }

    public long getTotalExpires() {
        return totalExpires;
    }

    public void setTotalExpires(long totalExpires) {
        this.totalExpires = totalExpires;
    }

    public String getClusterPass() {
        return clusterPass;
    }

    public void setClusterPass(String clusterPass) {
        this.clusterPass = clusterPass;
    }

    public ClusterType getClusterType() {
        return clusterType;
    }

    public void setClusterType(ClusterType clusterType) {
        this.clusterType = clusterType;
    }
}
