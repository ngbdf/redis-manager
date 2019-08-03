package com.newegg.ec.redis.entity;

/**
 * For
 * @author Jay.H.Zou
 * @date 7/25/2019
 */
public class RedisNode {

    private String redisNodeId;

    private int clusterId;

    private String host;

    private int port;

    private NodeRole nodeRole;

    public String getRedisNodeId() {
        return redisNodeId;
    }

    public void setRedisNodeId(String redisNodeId) {
        this.redisNodeId = redisNodeId;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public NodeRole getNodeRole() {
        return nodeRole;
    }

    public void setNodeRole(NodeRole nodeRole) {
        this.nodeRole = nodeRole;
    }
}
