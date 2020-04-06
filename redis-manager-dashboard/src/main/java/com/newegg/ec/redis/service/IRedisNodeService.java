package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.service.AbstractNodeOperation;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public interface IRedisNodeService {

    List<RedisNode> getRedisNodeList(Integer clusterId);

    List<RedisNode> getMergedRedisNodeList(Integer clusterId);

    boolean existRedisNode(RedisNode redisNode);

    List<RedisNode> mergeRedisNode(List<RedisNode> realRedisNodeList, List<RedisNode> dbRedisNodeList);

    boolean addRedisNode(RedisNode redisNode);

    boolean addRedisNodeList(List<RedisNode> redisNodeList);

    boolean updateRedisNode(RedisNode redisNode);

    boolean deleteRedisNodeListByClusterId(Integer clusterId);

    boolean deleteRedisNodeById(Integer redisNodeId);

    List<RedisNode> sortRedisNodeList(List<RedisNode> redisNodeList);

}
