package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.RedisConfig;

import java.util.List;

/**
 *
 * @author Jay.H.Zou
 * @date 2019/7/28
 */
public interface RedisConfDao {

    int insertRedisConf();

    List<RedisConfig> getConfig();
}
