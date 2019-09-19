package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.RedisNode;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Manage redis nodes
 * 可能不需要
 *
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
@Deprecated
public interface IRedisNodeDao {

    /**
     * 获取节点基础信息
     *
     * @param clusterId
     * @return
     */
    List<RedisNode> selectSimpleRedisNodeList(Integer clusterId);

    /**
     * 获取节点详细信息
     *
     * @param clusterId
     * @return
     */
    List<RedisNode> selectRedisNodeList(Integer clusterId);

    int updateRedisNode(RedisNode redisInstance);

    int insertRedisNodeList(List<RedisNode> redisInstanceList);

    int deleteRedisNodeListByClusterId(Integer clusterId);

    int deleteRedisNodeById(Integer redisNodeId);

}
