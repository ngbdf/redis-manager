package com.newegg.ec.redis.service.impl;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.plugin.install.entity.RedisNode;
import com.newegg.ec.redis.entity.RedisQueryParam;
import com.newegg.ec.redis.entity.RedisQueryResult;
import com.newegg.ec.redis.service.IRedisService;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
public class RedisService implements IRedisService {

    @Override
    public List<String> getDBList() {
        return null;
    }

    @Override
    public List<RedisNode> getNodeList() {
        return null;
    }

    @Override
    public Cluster getInfoServer() {
        return null;
    }

    @Override
    public List<String> scan(RedisQueryParam redisQueryParam) {
        return null;
    }

    @Override
    public RedisQueryResult query(RedisQueryParam redisQueryParam) {
        return null;
    }

    @Override
    public boolean forget() {
        return false;
    }
}
