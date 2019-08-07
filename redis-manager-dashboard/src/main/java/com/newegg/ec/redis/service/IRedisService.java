package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.*;
import com.newegg.ec.redis.entity.RedisNode;
import redis.clients.jedis.HostAndPort;

import java.util.List;
import java.util.Set;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public interface IRedisService {

    /**
     *
     * @param hostAndPortSet
     * @param redisPassword
     * @return
     */
    List<String> getDBList(Set<HostAndPort> hostAndPortSet, String redisPassword);

    /**
     * 获取集群节点
     *
     * @param hostAndPortSet
     * @param redisPassword
     * @return
     */
    List<RedisNode> getNodeList(Set<HostAndPort> hostAndPortSet, String redisPassword);

    /**
     * 获取集群
     * @param clusterId
     * @param hostAndPortSet
     * @param redisPassword
     * @param timeType
     * @return
     */
    List<NodeInfo> getNodeInfoList(int clusterId, Set<HostAndPort> hostAndPortSet, String redisPassword, NodeInfoType.TimeType timeType);

    NodeInfo getNodeInfo(int clusterId, HostAndPort hostAndPort, String redisPassword, NodeInfoType.TimeType timeType);

    Cluster getClusterInfo(Set<HostAndPort> hostAndPortSet, String redisPassword);

    Cluster getServerInfo(Set<HostAndPort> hostAndPortSet, String redisPassword);

    List<String> scan(RedisQueryParam redisQueryParam);

    RedisQueryResult query(RedisQueryParam redisQueryParam);

    boolean forget();


}
