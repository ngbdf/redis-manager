package com.newegg.ec.redis.entity;

import java.sql.Timestamp;

/**
 * Important: 此类中的字段并非与 info 命令返回的字段一一对应，有些做过计算或截断
 *
 * Redis Info for monitor:
 *      Server
 *      Clients
 *      Memory
 *      Persistence
 *      Stats
 *      Replication
 *      CPU
 *      Cluster
 *      Keyspace
 *
 * Monitor metrics:
 *      response_time: √
 *
 *      connected_clients: √
 *      blocked_clients: √
 *
 *      mem_fragmentation_ratio: √
 *      used_memory: √
 *      used_memory_rss: √
 *      used_memory_dataset: √
 *
 *      total_commands_processed: √
 *
 *      keys: √
 *      expires: √
 *
 *      keyspace_hits_ratio:  √
 *
 *      used_cpu_sys: √
 * Scalable
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public class NodeInfo {

    private Integer infoId;

    private NodeRole role;

    private String node;

    private NodeInfoType.DataType dataType;

    private NodeInfoType.TimeType timeType;

    private boolean lastTime;

    private long responseTime;

    /**
     * Clients
     */
    private long connectedClients;

    private long clientLongestOutputList;

    private long clientBiggestInputBuf;

    private long blockedClients;

    /** Memory */
    /**
     * 由 Redis 分配器分配的内存总量，以字节（byte）为单位
     * usedMemory = used_memory / 1024 / 1024
     */
    private long usedMemory;

    /**
     * 从操作系统的角度，返回 Redis 已分配的内存总量（俗称常驻集大小）。这个值和 top 、 ps等命令的输出一致
     * usedMemoryRss = used_memory_rss / 1024 / 1024
     */
    private long usedMemoryRss;

    /**
     * Redis为了维护数据集的内部机制所需的内存开销，包括所有客户端输出缓冲区、查询缓冲区、AOF重写缓冲区和主从复制的backlog
     * usedMemoryOverhead = used_memory_overhead / 1024 / 1024
     */
    private long usedMemoryOverhead;

    /**
     * usedMemoryDataset = (used_memory - used_memory_overhead) / 1024 / 1024
     */
    private long usedMemoryDataset;

    /**
     * 100 * (used_memory_dataset / (used_memory - used_memory_startup))
     */
    private double usedMemoryDatasetPerc;

    private double memFragmentationRatio;

    /** Stats */
    /**
     * 新创建连接个数,如果新创建连接过多，过度地创建和销毁连接对性能有影响，说明短连接严重或连接池使用有问题，需调研代码的连接设置
     */
    private long totalConnectionsReceived;

    /**
     * redis 每分钟或每小时新建连接数
     */
    private long connectionsReceived;

    /**
     * redis处理的命令数 (total_commands_processed): 监控采集周期内的平均qps
     */
    private long totalCommandsProcessed;

    /**
     * redis 每分钟或每小时处理的命令数
     */
    private long commandsProcessed;

    /**
     * redis当前的qps (instantaneous_ops_per_sec): redis内部较实时的每秒执行的命令数
     */
    private long instantaneousOpsPerSec;
    /**
     * redis 网络入口流量字节数
     */
    private long totalNetInputBytes;

    /**
     * redis 每分钟或每小时网络入口流量字节数
     */
    private long netInputBytes;

    /**
     * redis 网络出口流量字节数
     */
    private long totalNetOutputBytes;

    /**
     * redis 每分钟或每小时网络出口流量字节数
     */
    private long netOutputBytes;

    /**
     * keyspace_hits
     */
    private long keyspaceHits;

    /**
     * keyspace_misses
     */
    private long keyspaceMisses;

    /**
     * for redis manager
     */
    private double keyspaceHitsRatio;

    /** Replication */
    // 计算主从复制情况

    /** CPU */
    private double usedCpuSys;

    private double usedCpuUser;

    /** Keyspace */
    /**
     * 当前节点所有db的 key 总数
     */
    private long keys;

    private long expires;

    private Timestamp updateTime;

    public NodeInfo() {}

    public NodeInfo(NodeInfoType.DataType dataType, NodeInfoType.TimeType timeType, boolean lastTime) {
        this.dataType = dataType;
        this.timeType = timeType;
        this.lastTime = lastTime;
    }

    public Integer getInfoId() {
        return infoId;
    }

    public void setInfoId(Integer infoId) {
        this.infoId = infoId;
    }

    public NodeRole getRole() {
        return role;
    }

    public void setRole(NodeRole role) {
        this.role = role;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
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

    public boolean isLastTime() {
        return lastTime;
    }

    public void setLastTime(boolean lastTime) {
        this.lastTime = lastTime;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public long getConnectedClients() {
        return connectedClients;
    }

    public void setConnectedClients(long connectedClients) {
        this.connectedClients = connectedClients;
    }

    public long getClientLongestOutputList() {
        return clientLongestOutputList;
    }

    public void setClientLongestOutputList(long clientLongestOutputList) {
        this.clientLongestOutputList = clientLongestOutputList;
    }

    public long getClientBiggestInputBuf() {
        return clientBiggestInputBuf;
    }

    public void setClientBiggestInputBuf(long clientBiggestInputBuf) {
        this.clientBiggestInputBuf = clientBiggestInputBuf;
    }

    public long getBlockedClients() {
        return blockedClients;
    }

    public void setBlockedClients(long blockedClients) {
        this.blockedClients = blockedClients;
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public long getUsedMemoryRss() {
        return usedMemoryRss;
    }

    public void setUsedMemoryRss(long usedMemoryRss) {
        this.usedMemoryRss = usedMemoryRss;
    }

    public long getUsedMemoryOverhead() {
        return usedMemoryOverhead;
    }

    public void setUsedMemoryOverhead(long usedMemoryOverhead) {
        this.usedMemoryOverhead = usedMemoryOverhead;
    }

    public long getUsedMemoryDataset() {
        return usedMemoryDataset;
    }

    public void setUsedMemoryDataset(long usedMemoryDataset) {
        this.usedMemoryDataset = usedMemoryDataset;
    }

    public double getUsedMemoryDatasetPerc() {
        return usedMemoryDatasetPerc;
    }

    public void setUsedMemoryDatasetPerc(double usedMemoryDatasetPerc) {
        this.usedMemoryDatasetPerc = usedMemoryDatasetPerc;
    }

    public double getMemFragmentationRatio() {
        return memFragmentationRatio;
    }

    public void setMemFragmentationRatio(double memFragmentationRatio) {
        this.memFragmentationRatio = memFragmentationRatio;
    }

    public long getTotalConnectionsReceived() {
        return totalConnectionsReceived;
    }

    public void setTotalConnectionsReceived(long totalConnectionsReceived) {
        this.totalConnectionsReceived = totalConnectionsReceived;
    }

    public long getConnectionsReceived() {
        return connectionsReceived;
    }

    public void setConnectionsReceived(long connectionsReceived) {
        this.connectionsReceived = connectionsReceived;
    }

    public long getTotalCommandsProcessed() {
        return totalCommandsProcessed;
    }

    public void setTotalCommandsProcessed(long totalCommandsProcessed) {
        this.totalCommandsProcessed = totalCommandsProcessed;
    }

    public long getCommandsProcessed() {
        return commandsProcessed;
    }

    public void setCommandsProcessed(long commandsProcessed) {
        this.commandsProcessed = commandsProcessed;
    }

    public long getInstantaneousOpsPerSec() {
        return instantaneousOpsPerSec;
    }

    public void setInstantaneousOpsPerSec(long instantaneousOpsPerSec) {
        this.instantaneousOpsPerSec = instantaneousOpsPerSec;
    }

    public long getTotalNetInputBytes() {
        return totalNetInputBytes;
    }

    public void setTotalNetInputBytes(long totalNetInputBytes) {
        this.totalNetInputBytes = totalNetInputBytes;
    }

    public long getNetInputBytes() {
        return netInputBytes;
    }

    public void setNetInputBytes(long netInputBytes) {
        this.netInputBytes = netInputBytes;
    }

    public long getTotalNetOutputBytes() {
        return totalNetOutputBytes;
    }

    public void setTotalNetOutputBytes(long totalNetOutputBytes) {
        this.totalNetOutputBytes = totalNetOutputBytes;
    }

    public long getNetOutputBytes() {
        return netOutputBytes;
    }

    public void setNetOutputBytes(long netOutputBytes) {
        this.netOutputBytes = netOutputBytes;
    }

    public long getKeyspaceHits() {
        return keyspaceHits;
    }

    public void setKeyspaceHits(long keyspaceHits) {
        this.keyspaceHits = keyspaceHits;
    }

    public long getKeyspaceMisses() {
        return keyspaceMisses;
    }

    public void setKeyspaceMisses(long keyspaceMisses) {
        this.keyspaceMisses = keyspaceMisses;
    }

    public double getKeyspaceHitsRatio() {
        return keyspaceHitsRatio;
    }

    public void setKeyspaceHitsRatio(double keyspaceHitsRatio) {
        this.keyspaceHitsRatio = keyspaceHitsRatio;
    }

    public double getUsedCpuSys() {
        return usedCpuSys;
    }

    public void setUsedCpuSys(double usedCpuSys) {
        this.usedCpuSys = usedCpuSys;
    }

    public double getUsedCpuUser() {
        return usedCpuUser;
    }

    public void setUsedCpuUser(double usedCpuUser) {
        this.usedCpuUser = usedCpuUser;
    }

    public long getKeys() {
        return keys;
    }

    public void setKeys(long keys) {
        this.keys = keys;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
