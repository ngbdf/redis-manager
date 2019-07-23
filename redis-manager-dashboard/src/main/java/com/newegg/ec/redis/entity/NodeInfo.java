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
 *      Cluster
 *      Keyspace
 * Scalable
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public class NodeInfo {

    private String nodeInfoId;

    private String host;

    private String ip;

    private int port;

    private long responseTime;

    private NodeInfoType nodeInfoType;

    private Timestamp addTime;

    /**
     * Server
     */
    private String redisVersion;

    private String redisMode;

    private String os;

    /**
     * Clients
     */
    private int connectedClients;

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
    private long usedMemoryRss;

    private String usedMemoryRssHuman;

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
    /**
     * 新创建连接个数,如果新创建连接过多，过度地创建和销毁连接对性能有影响，说明短连接严重或连接池使用有问题，需调研代码的连接设置
     */
    private long totalConnectionsReceived;

    /**
     * redis处理的命令数
     */
    private long totalCommandsProcessed;

    /**
     * redis当前的qps，redis内部较实时的每秒执行的命令数
     */
    private long instantaneousOpsPerSec;

    /**
     * redis网络入口流量字节数
     */
    private long totalNetInputBytes;

    /**
     * redis网络出口流量字节数
     */
    private long totalNetOutputBytes;

    /**
     * 拒绝的连接个数，redis连接个数达到maxclients限制，拒绝新连接的个数
     */
    private long rejectedConnections;

    /**
     * 运行以来剔除(超过了maxmemory后)的key的数量
     */
    private long evictedKeys;

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
    // TODO: 计算主从复制情况

    /** CPU */
    // TODO: 计算每分钟CPU使用率
    private double usedCpuSys;

    private double usedCpuUser;

    /** Keyspace */
    /**
     * 当前节点所有db的 key 总数
     */
    private long keys;

    private long expires;


}
