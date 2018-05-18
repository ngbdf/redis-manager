package com.newegg.ec.cache.app.component.redis;

/**
 * Created by lzz on 2018/3/19.
 */
public interface IRedis {
    Object getRedisValue(int db, String key);
    Object scanRedis(int db, String key);
    void close();
}
