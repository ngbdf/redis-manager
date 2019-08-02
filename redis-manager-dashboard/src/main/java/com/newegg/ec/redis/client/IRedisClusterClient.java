package com.newegg.ec.redis.client;

import redis.clients.jedis.JedisCluster;

/**
 * @author Jay.H.Zou
 * @date 2019/7/18
 */
public interface IRedisClusterClient extends IRedisClient{

    JedisCluster getRedisClusterClient();

    String getClusterInfo();

    String getNodeList();

    String getMasterList();

    String getSlaveList();

}
