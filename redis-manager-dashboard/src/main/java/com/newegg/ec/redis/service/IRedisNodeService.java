package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.RedisNode;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public interface IRedisNodeService {

    List<RedisNode> getRedisNodeList(Integer clusterId);

    List<RedisNode> getRedisInstance(Integer clusterId);

    int addRedisInstance(List<RedisNode> redisInstanceList);

    int deleteRedisNodeByClusterId(Integer clusterId);

}
