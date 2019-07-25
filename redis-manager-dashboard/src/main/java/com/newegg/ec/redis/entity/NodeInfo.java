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
 *      response_time: 平均响应时间 √
 *
 *      connected_clients: √
 *      blocked_clients:
 *
 *      mem_fragmentation_ratio: √
 *      used_memory: √
 *      used_memory_rss: √
 *      used_memory_dataset: √
 *
 *      total_commands_processed:
 *      instantaneous_ops_per_sec:
 *
 *      keys: √
 *      expires: √
 *
 *      keyspaceHitsRatio: 命中率
 *
 *      used_cpu_sys
 * Scalable
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public class NodeInfo {

    private String nodeInfoId;

    private String host;

    private long responseTime;

    private NodeInfoType.DataType dataType;

    private NodeInfoType.TimeType timeType;

    private Timestamp addTime;

    /**
     * Clients
     */
    private long connectedClients;

    private long clientLongestOutputList;

    private long clientBiggestInputBuf;

    private int blockedClients;

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
     * redis 处理的命令数
     */
    private long totalCommandsProcessed;

    /**
     * redis 每分钟处理的命令数
     */
    private long minuteCommandProcessed;
    /**
     * redis网络入口流量字节数
     */
    private long totalNetInputBytes;

    /**
     * redis网络出口流量字节数
     */
    private long totalNetOutputBytes;

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

    /** Keyspace */
    /**
     * 当前节点所有db的 key 总数
     */
    private long keys;

    private long expires;

    public String getNodeInfoId() {
        return nodeInfoId;
    }

    public void setNodeInfoId(String nodeInfoId) {
        this.nodeInfoId = nodeInfoId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
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

    public Timestamp getAddTime() {
        return addTime;
    }

    public void setAddTime(Timestamp addTime) {
        this.addTime = addTime;
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

    public int getBlockedClients() {
        return blockedClients;
    }

    public void setBlockedClients(int blockedClients) {
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

    public long getTotalCommandsProcessed() {
        return totalCommandsProcessed;
    }

    public void setTotalCommandsProcessed(long totalCommandsProcessed) {
        this.totalCommandsProcessed = totalCommandsProcessed;
    }

    public long getMinuteCommandProcessed() {
        return minuteCommandProcessed;
    }

    public void setMinuteCommandProcessed(long minuteCommandProcessed) {
        this.minuteCommandProcessed = minuteCommandProcessed;
    }

    public long getTotalNetInputBytes() {
        return totalNetInputBytes;
    }

    public void setTotalNetInputBytes(long totalNetInputBytes) {
        this.totalNetInputBytes = totalNetInputBytes;
    }

    public long getTotalNetOutputBytes() {
        return totalNetOutputBytes;
    }

    public void setTotalNetOutputBytes(long totalNetOutputBytes) {
        this.totalNetOutputBytes = totalNetOutputBytes;
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

    @Override
    public String toString() {
        return "NodeInfo{" +
                "nodeInfoId='" + nodeInfoId + '\'' +
                ", host='" + host + '\'' +
                ", responseTime=" + responseTime +
                ", dataType=" + dataType +
                ", timeType=" + timeType +
                ", addTime=" + addTime +
                ", connectedClients=" + connectedClients +
                ", clientLongestOutputList=" + clientLongestOutputList +
                ", clientBiggestInputBuf=" + clientBiggestInputBuf +
                ", blockedClients=" + blockedClients +
                ", usedMemory=" + usedMemory +
                ", usedMemoryRss=" + usedMemoryRss +
                ", usedMemoryOverhead=" + usedMemoryOverhead +
                ", usedMemoryDataset=" + usedMemoryDataset +
                ", usedMemoryDatasetPerc=" + usedMemoryDatasetPerc +
                ", memFragmentationRatio=" + memFragmentationRatio +
                ", totalConnectionsReceived=" + totalConnectionsReceived +
                ", totalCommandsProcessed=" + totalCommandsProcessed +
                ", minuteCommandProcessed=" + minuteCommandProcessed +
                ", totalNetInputBytes=" + totalNetInputBytes +
                ", totalNetOutputBytes=" + totalNetOutputBytes +
                ", keyspaceHits=" + keyspaceHits +
                ", keyspaceMisses=" + keyspaceMisses +
                ", keyspaceHitsRatio=" + keyspaceHitsRatio +
                ", usedCpuSys=" + usedCpuSys +
                ", keys=" + keys +
                ", expires=" + expires +
                '}';
    }
}
