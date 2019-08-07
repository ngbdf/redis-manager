package com.newegg.ec.redis.client;

import com.newegg.ec.redis.entity.RedisQueryParam;
import com.newegg.ec.redis.entity.RedisQueryResult;

import java.util.List;

/**
 * 数据相关操作
 *
 * @author Jay.H.Zou
 * @date 2019/8/5
 */
public interface IDatabaseCommand {

    String NONE = "none";

    String STRING = "string";

    String HASH = "hash";

    String LIST = "list";

    String SET = "set";

    String ZSET = "zset";

    boolean exists(String key);

    String type(String key);

    long ttl(String key);

    Long del(String key);

    /**
     * Query redis
     *
     * @param redisQueryParam
     * @return
     */
    RedisQueryResult query(RedisQueryParam redisQueryParam);

    /**
     * Scan redis
     *
     * @return
     */
    List<String> scan(String key);

    String object(String type);
}
