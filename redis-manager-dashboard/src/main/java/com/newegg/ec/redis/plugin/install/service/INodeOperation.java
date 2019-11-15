package com.newegg.ec.redis.plugin.install.service;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;

/**
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public interface INodeOperation {

    boolean start(Cluster cluster, RedisNode redisNode);

    boolean stop(Cluster cluster, RedisNode redisNode);

    boolean restart(Cluster cluster, RedisNode redisNode);

    boolean remove(Cluster cluster, RedisNode redisNode);

}
