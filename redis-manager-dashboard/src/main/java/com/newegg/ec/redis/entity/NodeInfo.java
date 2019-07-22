package com.newegg.ec.redis.entity;

import java.sql.Timestamp;

/**
 * Redis Info for monitor:
 *      Server
 *      Clients
 *      Memory
 *      Persistence
 *      Stats
 *      Replication
 *      CPU
 *      Command Stats
 *      Cluster
 *      Keyspace
 * Scalable
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public class NodeInfo {

    private String nodeInfoId;

    private Timestamp collectionTime;

    /**
     * Server
     */
    private String redisVersion;

    private String redisMode;

    private String os;

    /**
     * Clients
     */
    private int connectedClient;

    private String clientLongestOutputList;

    private String clientBiggestInputBuf;

    private int blockedClients;

    /** Memory */
    /**
     * 由 Redis 分配器分配的内存总量，以字节（byte）为单位
     */
    private long usedMemory;

    private String usedMemoryHuman;

    /**
     * 从操作系统的角度，返回 Redis 已分配的内存总量（俗称常驻集大小）。这个值和 top 、 ps等命令的输出一致
     */
    private long usedMemoryRSS;

    private String usedMemoryRSSHuman;

    private String usedMemoryPeakPerc;

    /**
     * Redis为了维护数据集的内部机制所需的内存开销，包括所有客户端输出缓冲区、查询缓冲区、AOF重写缓冲区和主从复制的backlog
     */
    private long usedMemoryOverhead;

    /**
     * used_memory - used_memory_overhead
     */
    private long usedMemoryDataset;

    /**
     * 100% * (used_memory_dataset / (used_memory - used_memory_startup))
     */
    private String usedMemoryDatasetPerc;

    private String memFragmentationRatio;

    /** Stats */
    private long totalConnectionsReceived;

    private long totalCommandsProcessed;

    private long instantaneousOpsPerSec;

    private long totalNetInputBytes;

    private long totalNetOutputBytes;

    private long rejectedConnections;

    private long evictedKeys;

    private long keyspaceHits;

    private long keyspaceMisses;

    /**
     * for redis manager
     */
    private double keyspaceHitsRatio;

    /** Replication */
    // TODO:

    /** CPU */
    private String usedCPUSys;


}
