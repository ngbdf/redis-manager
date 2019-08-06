package com.newegg.ec.redis.entity;

/**
 * For
 *
 * @author Jay.H.Zou
 * @date 7/25/2019
 */
public class RedisNode {

    private int redisNodeId;

    private int clusterId;

    private String nodeId;

    private String host;

    private int port;

    private NodeRole nodeRole;

    private String status;

    public RedisNode() {
    }

    public RedisNode(String host, int port, NodeRole nodeRole) {
        this(null, host, port, nodeRole);
    }

    public RedisNode(String nodeId, String host, int port, NodeRole nodeRole) {
        this.nodeId = nodeId;
        this.host = host;
        this.port = port;
        this.nodeRole = nodeRole;
    }

    public int getRedisNodeId() {
        return redisNodeId;
    }

    public void setRedisNodeId(int redisNodeId) {
        this.redisNodeId = redisNodeId;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
