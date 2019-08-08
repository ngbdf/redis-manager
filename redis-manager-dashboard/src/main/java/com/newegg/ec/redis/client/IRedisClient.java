package com.newegg.ec.redis.client;

import com.newegg.ec.redis.entity.NodeRole;
import com.newegg.ec.redis.entity.RedisNode;
import redis.clients.jedis.ClusterReset;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.util.Slowlog;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
    Map<String, String> getInfo() throws Exception;

    /**
     * Get redis node info with sub key
     *
     * @param section
     * @return
     */
    Map<String, String> getInfo(String section) throws Exception;

    Map<String, String> getClusterInfo() throws Exception;

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

    /**
     * just for standalone mode
     * @return
     */
    List<RedisNode> nodes() throws Exception;

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

    NodeRole role() throws Exception;

    /** 客户端于服务端 */
    /**
     * 测试密码是否正确
     * @param password
     * @return
     */
    boolean auth(String password);

    /**
     * stop node
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
    Map<String, String> getConfig();

    /**
     * Get redis configuration with sub key
     *
     * @param pattern
     * @return
     */
    Map<String, String> getConfig(String pattern);

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
    boolean clientSetName(String clientName);

    String clusterMeet(HostAndPort hostAndPort);

    /**
     * Be slave
     * 重新配置一个节点成为指定master的salve节点
     *
     * @param nodeId
     * @return
     */
    String clusterReplicate(String nodeId);

    /**
     * Be master
     * 该命令只能在群集slave节点执行，让slave节点进行一次人工故障切换
     * @return
     */
    String clusterFailOver();

    String clusterAddSlots(int... slots);

    String clusterForget(String nodeId);

    String clusterReset(ClusterReset reset);

    String clusterSlaves(String nodeId);

    /**
     * Close client
     *
     * @return
     */
    void close();
}
