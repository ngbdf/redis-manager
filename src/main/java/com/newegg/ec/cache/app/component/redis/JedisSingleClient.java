package com.newegg.ec.cache.app.component.redis;

import redis.clients.jedis.*;

/**
 * Created by lzz on 2018/3/19.
 */
public class JedisSingleClient extends RedisClientBase implements IRedis {
    protected Jedis singleClient;
    public JedisSingleClient(String ip, int port){
        singleClient = new Jedis(ip, port);
    }

    public ScanResult<String> redisScan(String path, String cursor, ScanParams params){
        return singleClient.scan(cursor, params);
    }

    @Override
    public void close() {
        if( null != singleClient ){
            try {
                singleClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public Object scanRedis(int db, String key) {
        return super.scanRedis(this, key);
    }

    @Override
    public Object getRedisValue(int db, String key) {
        return super.getRedisValue(this, db, key);
    }

    @Override
    public JedisCommands getRedisCommands() {
        return singleClient;
    }

    @Override
    public MultiKeyCommands redisMultiKeyCommands() {
        return singleClient;
    }
}
