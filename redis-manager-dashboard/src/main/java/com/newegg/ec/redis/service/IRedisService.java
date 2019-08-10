package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.*;
import com.newegg.ec.redis.util.SlotBalanceUtil;
import redis.clients.jedis.HostAndPort;

import java.util.List;
import java.util.Map;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public interface IRedisService {

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

    /**
     * Node info list after calculating
     *
     * @param cluster
     * @param timeType
     * @return
     */
    List<NodeInfo> getNodeInfoList(Cluster cluster, NodeInfoType.TimeType timeType);

    /**
     * Node info after calculating
     *
     * @param clusterId
     * @param hostAndPort
     * @param redisPassword
     * @param timeType
     * @return
     */
    NodeInfo getNodeInfo(int clusterId, HostAndPort hostAndPort, String redisPassword, NodeInfoType.TimeType timeType);

    List<RedisSlowLog> getRedisSlowLog(Cluster cluster, SlowLogParam slowLogParam);

    AutoCommandResult scan(Cluster cluster, AutoCommandParam autoCommandParam);

    AutoCommandResult query(Cluster cluster, AutoCommandParam autoCommandParam);

    Object console(Cluster cluster, DataCommandsParam dataCommandsParam);

    boolean clusterForget(Cluster cluster, RedisNode forgetNode);

    /**
     * change master
     *
     * @param cluster
     * @param replicaNode: masterId 已被替换
     * @return
     */
    boolean clusterReplicate(Cluster cluster, RedisNode replicaNode);

    boolean clusterFailOver(Cluster cluster, RedisNode newMasterNode);

    /**
     * cluster install & cluster expansion
     * <p>
     * Important thing: change require-pass config before cluster meet
     * <p>
     * if failed, please try it
     *
     * @param cluster
     * @param redisNodeList
     * @return
     */
    boolean clusterMeet(Cluster cluster, List<RedisNode> redisNodeList);

    String clusterAddSlots(Cluster cluster, RedisNode masterNode, SlotBalanceUtil.Shade shade);

    String clusterAddSlotsBatch(Cluster cluster, Map<RedisNode, SlotBalanceUtil.Shade> masterNodeAndShade);

    void clusterMoveSlots(Cluster cluster, RedisNode targetNode, SlotBalanceUtil.Shade shade);

    boolean standaloneReplicaOf(Cluster cluster, RedisNode masterNode, RedisNode redisNode);

    boolean standaloneReplicaNoOne(Cluster cluster, RedisNode redisNode);

}
