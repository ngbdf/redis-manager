package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.RedisInstance;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public interface IRedisNodeService {

    List<RedisNode> getRedisNodeList(int clusterId);

    List<RedisInstance> getRedisInstance(int clusterId);

    int addRedisInstance(List<RedisInstance> redisInstanceList);

    int deleteRedisNodeByClusterId(int clusterId);

}
