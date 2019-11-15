package com.newegg.ec.redis.client;

import com.newegg.ec.redis.entity.AutoCommandParam;
import com.newegg.ec.redis.entity.AutoCommandResult;
import com.newegg.ec.redis.entity.DataCommandsParam;

/**
 * 数据相关操作
 *
 * @author Jay.H.Zou
 * @date 2019/8/5
 */
public interface IDatabaseCommand {

    String TYPE_NONE = "none";

    String TYPE_STRING = "string";

    String TYPE_HASH = "hash";

    String TYPE_LIST = "list";

    String TYPE_SET = "set";

    String TYPE_ZSET = "zset";

    /**
     * string
     */
    String GET = "GET";

    String SET = "SET";

    /**
     * hash
     */
    String HGET = "HGET";

    String HMGET = "HMGET";

    String HGETALL = "HGETALL";

    String HKEYS = "HKEYS";

    String HSET = "HSET";

    /**
     * list
     */
    String LINDEX = "LINDEX";

    String LLEN = "LLEN";

    String LRANGE = "LRANGE";

    String LPUSH = "LPUSH";

    String RPUSH = "RPUSH";

    /**
     * set
     */
    String SCARD = "SCARD";

    String SADD = "SADD";

    String SMEMBERS = "SMEMBERS";

    String SRANDMEMBER = "SRANDMEMBER";

    /**
     * sorted set
     */
    String ZCARD = "ZCARD";

    String ZSCORE = "ZSCORE";

    String ZCOUNT = "ZCOUNT";

    String ZRANGE = "ZRANGE";

    String ZADD = "ZADD";

    String TYPE = "TYPE";

    String DEL = "DEL";

    boolean exists(String key);

    String type(String key);

    long ttl(String key);

    Long del(String key);

    /**
     * Query redis
     *
     * @param autoCommandParam
     * @return
     */
    AutoCommandResult query(AutoCommandParam autoCommandParam);

    Object string(DataCommandsParam dataCommandsParam);

    Object hash(DataCommandsParam dataCommandsParam);

    Object list(DataCommandsParam dataCommandsParam);

    Object set(DataCommandsParam dataCommandsParam);

    Object zset(DataCommandsParam dataCommandsParam);

    Object type(DataCommandsParam dataCommandsParam);

    Object del(DataCommandsParam dataCommandsParam);

}
