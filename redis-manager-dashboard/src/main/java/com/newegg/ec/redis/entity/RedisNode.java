package com.newegg.ec.redis.entity;

import redis.clients.jedis.HostAndPort;

import java.sql.Timestamp;

/**
 * For
 *
 * @author Jay.H.Zou
 * @date 7/25/2019
 */
public class RedisNode {

    /**
     * mysql table id
     */
    private Integer redisNodeId;

    private Integer groupId;

    private Integer clusterId;

    private String nodeId;

    /***
     * 如果节点是slave，并且已知master节点，则这里列出master节点ID,否则的话这里列出"-"
     */
    private String masterId;

    private String host;

    private int port;

    private NodeRole nodeRole;

    /**
     * 节点状态: 当节点为 myself. master, slave 时, 此值为空
     * myself: 当前连接的节点
     * master: 节点是master.
     * slave: 节点是slave.
     * fail?: 节点处于PFAIL 状态。 当前节点无法联系，但逻辑上是可达的 (非 FAIL 状态).
     * fail: 节点处于FAIL 状态. 大部分节点都无法与其取得联系将会将改节点由 PFAIL 状态升级至FAIL状态。
     * handshake: 还未取得信任的节点，当前正在与其进行握手.
     * noaddr: 没有地址的节点（No address known for this node）.
     * noflags: 连个标记都没有（No flags at all）.
     */
    private String flags;

    /**
     * link-state: node-to-node 集群总线使用的链接的状态，我们使用这个链接与集群中其他节点进行通信.值可以是 connected 和 disconnected.
     */
    private String linkState;

    private String slotRange;

    private int slotNumber;

    private String containerId;

    private String containerName;

    private boolean inCluster;

    private boolean runStatus;

    private boolean inDatabase;

    private Timestamp insertTime;

    private Timestamp updateTime;

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

    public RedisNode(String host, int port) {
        this(host, port, NodeRole.UNKNOWN);
    }

    public static RedisNode masterRedisNode(String host, int port) {
        return new RedisNode(host, port, NodeRole.MASTER);
    }

    public static RedisNode masterRedisNode(HostAndPort hostAndPort) {
        return new RedisNode(hostAndPort.getHost(), hostAndPort.getPort(), NodeRole.MASTER);
    }

    public static RedisNode masterRedisNode(String nodeId, String host, int port) {
        return new RedisNode(nodeId, host, port, NodeRole.MASTER);
    }

    public static RedisNode slaveRedisNode(String host, int port) {
        return new RedisNode(host, port, NodeRole.SLAVE);
    }

    public static RedisNode slaveRedisNode(String nodeId, String host, int port) {
        return new RedisNode(nodeId, host, port, NodeRole.SLAVE);
    }

    public Integer getRedisNodeId() {
        return redisNodeId;
    }

    public void setRedisNodeId(Integer redisNodeId) {
        this.redisNodeId = redisNodeId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
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

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public String getLinkState() {
        return linkState;
    }

    public void setLinkState(String linkState) {
        this.linkState = linkState;
    }

    public String getSlotRange() {
        return slotRange;
    }

    public void setSlotRange(String slotRange) {
        this.slotRange = slotRange;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public boolean getInCluster() {
        return inCluster;
    }

    public void setInCluster(boolean inCluster) {
        this.inCluster = inCluster;
    }

    public boolean getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(boolean runStatus) {
        this.runStatus = runStatus;
    }

    public boolean getInDatabase() {
        return inDatabase;
    }

    public void setInDatabase(boolean inDatabase) {
        this.inDatabase = inDatabase;
    }

    public Timestamp getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Timestamp insertTime) {
        this.insertTime = insertTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
