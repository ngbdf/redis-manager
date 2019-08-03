package com.newegg.ec.redis.entity;

import com.newegg.ec.redis.plugin.install.entity.InstallationEnvironment;

/**
 * 描述集群基本情况
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public class Cluster {

    private int clusterId;

    private String groupId;

    private String admins;

    private String clusterToken;

    private String clusterName;

    private String nodes;

    private String redisMode;

    private String os;

    private String redisVersion;

    /**
     * 集群 key 总数
     */
    private long totalKeys;

    /**
     * 集群 expire 总数
     */
    private long totalExpires;

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
     *  哈希槽状态是 PFAIL的数量。只要哈希槽状态没有被升级到FAIL状态，这些哈希槽仍然可以被正常处理。
     *  PFAIL状态表示我们当前不能和节点进行交互，但这种状态只是临时的错误状态。
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

    /**
     * db number
     */
    private int dbNumber;

    private String redisPassword;

    private InstallationEnvironment installationEnv;

    /**
     * 0: install by redis manager
     * 1: other way
     */
    private int installationType;

    public enum ClusterState {
        UNKNOWN,

        GOOD,

        BAD,

        WARN;
    }


    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getAdmins() {
        return admins;
    }

    public void setAdmins(String admins) {
        this.admins = admins;
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

    public int getDbNumber() {
        return dbNumber;
    }

    public void setDbNumber(int dbNumber) {
        this.dbNumber = dbNumber;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public InstallationEnvironment getInstallationEnv() {
        return installationEnv;
    }

    public void setInstallationEnv(InstallationEnvironment installationEnv) {
        this.installationEnv = installationEnv;
    }

    public int getInstallationType() {
        return installationType;
    }

    public void setInstallationType(int installationType) {
        this.installationType = installationType;
    }
}
