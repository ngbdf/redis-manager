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

    private int order;

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
    String flags;

    /**
     * link-state: node-to-node 集群总线使用的链接的状态，我们使用这个链接与集群中其他节点进行通信.值可以是 connected 和 disconnected.
     */
    String linkState;

    String slotRange;

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

    public static RedisNode masterRedisNode(String host, int port) {
        return new RedisNode(host, port, NodeRole.MASTER);
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

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RedisNode{");
        sb.append("redisNodeId=").append(redisNodeId);
        sb.append(", clusterId=").append(clusterId);
        sb.append(", nodeId='").append(nodeId).append('\'');
        sb.append(", masterId='").append(masterId).append('\'');
        sb.append(", host='").append(host).append('\'');
        sb.append(", port=").append(port);
        sb.append(", nodeRole=").append(nodeRole);
        sb.append(", flags='").append(flags).append('\'');
        sb.append(", linkState='").append(linkState).append('\'');
        sb.append(", slotRange='").append(slotRange).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
