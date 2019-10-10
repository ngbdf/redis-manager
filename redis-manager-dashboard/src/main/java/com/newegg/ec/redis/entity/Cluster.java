package com.newegg.ec.redis.entity;

/**
 * 描述集群基本情况
 * cluster info:
 * cluster_status:ok
 * cluster_slots_assigned:16384
 * cluster_slots_ok:16384
 * cluster_slots_pfail:0
 * cluster_slots_fail:0
 * cluster_known_nodes:6
 * cluster_size:3
 * cluster_current_epoch:4
 * cluster_my_epoch:0
 * cluster_stats_messages_ping_sent:1492419
 * cluster_stats_messages_pong_sent:1538939
 * cluster_stats_messages_sent:3031358
 * cluster_stats_messages_ping_received:1538939
 * cluster_stats_messages_pong_received:1492419
 * cluster_stats_messages_received:3031358
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public class Cluster {

    private Integer clusterId;

    private Integer groupId;

    private Integer userId;

    /**
     * 通过 token 连接
     */
    private String clusterToken;

    private String clusterName;

    private String nodes;

    private String redisMode;

    private String os;

    private String redisVersion;

    private String image;

    /**
     * Just for cluster mode
     */
    private boolean initialized;

    /**
     * 集群 key 总数
     */
    private long totalKeys;

    /**
     * 集群 expire 总数
     */
    private long totalExpires;

    private int dbSize;

    /**
     *
     */
    private ClusterState clusterState;

    /**
     * 已分配到集群节点的哈希槽数量（不是没有被绑定的数量）。16384个哈希槽全部被分配到集群节点是集群正常运行的必要条件
     */
    private int clusterSlotsAssigned;

    /**
     * 哈希槽状态不是FAIL 和 PFAIL 的数量
     */
    private int clusterSlotsOk;

    /**
     * 哈希槽状态是 PFAIL的数量。只要哈希槽状态没有被升级到FAIL状态，这些哈希槽仍然可以被正常处理。
     * PFAIL状态表示我们当前不能和节点进行交互，但这种状态只是临时的错误状态。
     */
    private int clusterSlotsPfail;

    /**
     * 哈希槽状态是FAIL的数量。如果值不是0，那么集群节点将无法提供查询服务，
     * 除非cluster-require-full-coverage被设置为no
     */
    private int clusterSlotsFail;

    /**
     * The total number of known nodes in the cluster, including nodes in HANDSHAKE state that may not currently be proper members of the cluster.
     */
    private int clusterKnownNodes;

    /**
     * The number of master nodes serving at least one hash slot in the cluster.
     */
    private int clusterSize;

    private String redisPassword;

    private Integer installationEnvironment;

    /**
     * 0: install by redis manager
     * 1: other way
     */
    private int installationType;

    private String ruleIds;

    private String channelIds;

    public enum ClusterState {
        UNKNOWN,

        HEALTH,

        BAD,

        WARN;
    }

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getClusterToken() {
        return clusterToken;
    }

    public void setClusterToken(String clusterToken) {
        this.clusterToken = clusterToken;
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

    public String getRedisVersion() {
        return redisVersion;
    }

    public void setRedisVersion(String redisVersion) {
        this.redisVersion = redisVersion;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
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

    public int getDbSize() {
        return dbSize;
    }

    public void setDbSize(int dbSize) {
        this.dbSize = dbSize;
    }

    public ClusterState getClusterState() {
        return clusterState;
    }

    public void setClusterState(ClusterState clusterState) {
        this.clusterState = clusterState;
    }

    public int getClusterSlotsAssigned() {
        return clusterSlotsAssigned;
    }

    public void setClusterSlotsAssigned(int clusterSlotsAssigned) {
        this.clusterSlotsAssigned = clusterSlotsAssigned;
    }

    public int getClusterSlotsOk() {
        return clusterSlotsOk;
    }

    public void setClusterSlotsOk(int clusterSlotsOk) {
        this.clusterSlotsOk = clusterSlotsOk;
    }

    public int getClusterSlotsPfail() {
        return clusterSlotsPfail;
    }

    public void setClusterSlotsPfail(int clusterSlotsPfail) {
        this.clusterSlotsPfail = clusterSlotsPfail;
    }

    public int getClusterSlotsFail() {
        return clusterSlotsFail;
    }

    public void setClusterSlotsFail(int clusterSlotsFail) {
        this.clusterSlotsFail = clusterSlotsFail;
    }

    public int getClusterKnownNodes() {
        return clusterKnownNodes;
    }

    public void setClusterKnownNodes(int clusterKnownNodes) {
        this.clusterKnownNodes = clusterKnownNodes;
    }

    public int getClusterSize() {
        return clusterSize;
    }

    public void setClusterSize(int clusterSize) {
        this.clusterSize = clusterSize;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public Integer getInstallationEnvironment() {
        return installationEnvironment;
    }

    public void setInstallationEnvironment(Integer installationEnvironment) {
        this.installationEnvironment = installationEnvironment;
    }

    public int getInstallationType() {
        return installationType;
    }

    public void setInstallationType(int installationType) {
        this.installationType = installationType;
    }

    public String getRuleIds() {
        return ruleIds;
    }

    public void setRuleIds(String ruleIds) {
        this.ruleIds = ruleIds;
    }

    public String getChannelIds() {
        return channelIds;
    }

    public void setChannelIds(String channelIds) {
        this.channelIds = channelIds;
    }
}
