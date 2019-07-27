package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.*;
import redis.clients.jedis.util.Slowlog;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public interface IRedisService {

    List<String> getDBList();

    List<RedisNode> getNodeList();

    Cluster getInfoServer();

    List<String> scan(RedisQueryParam redisQueryParam);

    RedisQueryResult query(RedisQueryParam redisQueryParam);

    boolean forget();


}
