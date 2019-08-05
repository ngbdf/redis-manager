package com.newegg.ec.redis.client;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Slowlog;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/18
 */
public interface IRedisClient extends IDatabaseCommand {

    /**
     * Get jedis client
     *
     * @return
     */
    Jedis getJedisClient();

    /**
     * Get redis node info
     *
     * @return
     */
    String getInfo();

    /**
     * Get redis node info with sub key
     *
     * @param section
     * @return
     */
    String getInfo(String section);

    String getClusterInfo();

    /**
     * Get redis memory info
     * <p>
     * Just support redis 4, 4+
     *
     * @return
     */
    String getMemory();

    /**
     * Get redis memory info with sub key
     * <p>
     * Just support redis 4, 4+
     *
     * @param subKey
     * @return
     */
    String getMemory(String subKey);

    String nodes();

    Long dbSize();

    /** 持久化 */
    String bgSave();

    Long lastSave();

    String bgRewriteAof();

    /**
     *  O(N)
     * @param hostAndPort
     * @return OK
     */
    String slaveOf(HostAndPort hostAndPort);

    String role();

    /** 客户端于服务端 */
    boolean auth(String password);

    /**
     *
     * @return ""
     */
    boolean shutdown();

    String clientList();

    /** config */
    /**
     * Get redis configuration
     *
     * @return
     */
    List<String> getConfig();

    /**
     * Get redis configuration with sub key
     *
     * @param pattern
     * @return
     */
    List<String> getConfig(String pattern);

    /**
     * Rewrite redis configuration
     *
     * @return
     */
    boolean rewriteConfig();

    /** Debug */

    /**
     * Ping redis
     *
     * @return
     */
    boolean ping();

    /**
     * Get slow log
     *
     * @return
     */
    List<Slowlog> getSlowLog(int size);

    /**
     *
     * @return OK
     */
    String clientSetName(String clientName);

    String clusterMeet(HostAndPort hostAndPort);

    /**
     * Be slave
     * 重新配置一个节点成为指定master的salve节点
     *
     * @param masterId
     * @return
     */
    String clusterReplicate(String masterId);

    /**
     * Be master
     * 该命令只能在群集slave节点执行，让slave节点进行一次人工故障切换
     * @return
     */
    String clusterFailOver();

    /**
     * Close client
     *
     * @return
     */
    void close();
}
