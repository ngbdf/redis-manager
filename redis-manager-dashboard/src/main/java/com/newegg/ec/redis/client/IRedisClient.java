package com.newegg.ec.redis.client;

import com.newegg.ec.redis.entity.AutoCommandParam;
import com.newegg.ec.redis.entity.AutoCommandResult;
import com.newegg.ec.redis.entity.NodeRole;
import com.newegg.ec.redis.entity.RedisNode;
import javafx.util.Pair;
import redis.clients.jedis.ClusterReset;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.MigrateParams;
import redis.clients.jedis.util.Slowlog;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    Set<String> scan(AutoCommandParam autoCommandParam);

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
     *
     * @return
     */
    List<RedisNode> nodes() throws Exception;

    Long dbSize();

    /**
     * 持久化
     */
    String bgSave();

    Long lastSave();

    String bgRewriteAof();

    NodeRole role() throws Exception;

    /** 客户端于服务端 */
    /**
     * 测试密码是否正确
     *
     * @param password
     * @return
     */
    boolean auth(String password);

    /**
     * stop node
     *
     * @return ""
     */
    boolean shutdown();

    String clientList();

    /**
     * change config
     *
     * @param keyAndValue
     * @return
     */
    boolean setConfig(Pair<String, String> keyAndValue);

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

    /**
     * 该命令主要用于nodes.conf节点状态文件丢失或被删除的情况下重新生成文件。
     * @return
     */
    String clusterSaveConfig();

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
     * @return OK
     */
    boolean clientSetName(String clientName);

    String clusterMeet(String host, int port);

    /**
     * Be slave
     * 重新配置一个节点成为指定master的salve节点
     *
     * @param nodeId
     * @return
     */
    boolean clusterReplicate(String nodeId);

    /**
     * Be master
     * 该命令只能在群集slave节点执行，让slave节点进行一次人工故障切换
     *
     * @return
     */
    boolean clusterFailOver();

    String clusterAddSlots(int... slots);

    String clusterSetSlotNode(int slot, String nodeId);

    String clusterSetSlotImporting(int slot, String nodeId);

    String clusterSetSlotMigrating(int slot, String nodeId);

    List<String> clusterGetKeysInSlot(int slot, int count);

    String clusterSetSlotStable(int slot);

    boolean clusterForget(String nodeId);

    String clusterReset(ClusterReset reset);

    String migrate(String host, int port, String key, int destinationDb, int timeout);

    String migrate(String host, int port, int destinationDB,
                   int timeout, MigrateParams params, String... keys);

//    String clusterSlaves(String nodeId);

    /**
     * old name: slaveOf
     *
     * @param host
     * @param port
     * @return OK
     */
    boolean replicaOf(String host, int port);

    /**
     * standalone forget this node
     *
     * @return
     */
    String replicaNoOne();

    String memoryPurge();

    /**
     * Close client
     *
     * @return
     */
    void close();
}
