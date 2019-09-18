package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.RedisInstance;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Manage redis nodes in docker
 *
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
@Mapper
public interface IRedisNodeDao {

    /**
     * 获取节点基础信息
     *
     * @param clusterId
     * @return
     */
    List<RedisNode> selectSimpleRedisNodeList(int clusterId);

    /**
     * 获取节点详细信息
     *
     * @param clusterId
     * @return
     */
    List<RedisInstance> selectRedisNodeList(int clusterId);

    int updateRedisNode(RedisInstance redisInstance);

    int insertRedisNodeList(List<RedisInstance> redisInstanceList);

    int deleteRedisNodeListByClusterId(int clusterId);

    int deleteRedisNodeById(int redisNodeId);

}
