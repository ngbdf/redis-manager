package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.*;
import com.newegg.ec.redis.util.SlotBalanceUtil;
import javafx.util.Pair;
import redis.clients.jedis.HostAndPort;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public interface IRedisService {

    /**
     * Get keyspace info
     * <p>
     * eg: db1: [keys: 12345, expire: 1234]
     *
     * @param cluster
     * @return
     */
    Map<String, Map<String, Long>> getKeyspaceInfo(Cluster cluster);

    /**
     * Get database
     * <p>
     * eg: db1 = 12345
     *
     * @param cluster
     * @return
     */
    Map<String, Long> getDatabase(Cluster cluster);

    /**
     * Get node list
     *
     * @param cluster
     * @return
     */
    List<RedisNode> getRedisNodeList(Cluster cluster);

    /**
     * Get node list
     *
     * @param cluster
     * @return
     */
    List<RedisNode> getRedisMasterNodeList(Cluster cluster);

    Map<String, String> getClusterInfo(Cluster cluster);

    List<RedisSlowLog> getRedisSlowLog(Cluster cluster, SlowLogParam slowLogParam);

    Set<String> scan(Cluster cluster, AutoCommandParam autoCommandParam);

    AutoCommandResult query(Cluster cluster, AutoCommandParam autoCommandParam);

    Object console(Cluster cluster, DataCommandsParam dataCommandsParam);

    boolean clusterForget(Cluster cluster, RedisNode forgetNode);

    /**
     * change master
     *
     * @param cluster
     * @param masterId
     * @param slaveNode
     * @return
     */
    boolean clusterReplicate(Cluster cluster, String masterId, RedisNode slaveNode);

    boolean clusterFailOver(Cluster cluster, RedisNode newMasterNode);

    /**
     * cluster install & cluster expansion
     * <p>
     * Important thing: change require-pass config before cluster meet
     * <p>
     * if failed, please try it
     *
     * @param cluster
     * @param seed
     * @param redisNodeList
     * @return
     */
    String clusterMeet(Cluster cluster, RedisNode seed, List<RedisNode> redisNodeList);

    String clusterAddSlots(Cluster cluster, RedisNode masterNode, SlotBalanceUtil.Shade shade);

    String clusterAddSlotsBatch(Cluster cluster, Map<RedisNode, SlotBalanceUtil.Shade> masterNodeAndShade);

    /**
     * 迁移槽位
     *
     * @param cluster
     * @param targetNode
     * @param shade
     */
    boolean clusterMoveSlots(Cluster cluster, RedisNode targetNode, SlotBalanceUtil.Shade shade);

    /**
     * slave of
     *
     * @param cluster
     * @param masterNode
     * @param redisNode
     * @return
     */
    boolean standaloneReplicaOf(Cluster cluster, RedisNode masterNode, RedisNode redisNode);

    /**
     * Forget this node
     *
     * @param cluster
     * @param redisNode
     * @return
     */
    boolean standaloneReplicaNoOne(Cluster cluster, RedisNode redisNode);

    /**
     * Get config
     * @param cluster
     * @param redisNode
     * @return
     */
    Map<String, String> getConfig(Cluster cluster, RedisNode redisNode);

    /**
     * 修改Redis配置
     *
     * @param cluster
     * @param config
     * @return
     */
    boolean setConfigBatch(Cluster cluster, Pair<String, String> config);

    /**
     * 修改Redis配置
     *
     * @param cluster
     * @param redisNode
     * @param config
     * @return
     */
    boolean setConfig(Cluster cluster, RedisNode redisNode, Pair<String, String> config);

    /**
     * 自动生成配置文件
     *
     * @param cluster
     */
    void autoGenerateConfigFile(Cluster cluster);

}
