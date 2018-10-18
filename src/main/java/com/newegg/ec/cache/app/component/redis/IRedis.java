package com.newegg.ec.cache.app.component.redis;

import com.newegg.ec.cache.app.model.RedisValue;

/**
 * Created by lzz on 2018/3/19.
 */
public interface IRedis {
    RedisValue getRedisValue(int db, String key);

    Object scanRedis(int db, String key);

    boolean importDataToCluster(String targetIp, int targetPort, String keyFormat) throws InterruptedException;

    void close();
}
