package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.*;
import com.newegg.ec.redis.entity.RedisNode;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.util.Slowlog;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    List<RedisNode> getNodeList(Cluster cluster);

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

    boolean forget();


}
