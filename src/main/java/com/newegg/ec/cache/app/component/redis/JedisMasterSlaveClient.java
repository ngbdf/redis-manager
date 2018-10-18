package com.newegg.ec.cache.app.component.redis;

import com.newegg.ec.cache.app.model.RedisValue;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;

/**
 * Created by lzz on 2018/3/19.
 */
public class JedisMasterSlaveClient extends RedisClientBase implements IRedis {
    private Jedis jedis;
    private String ip;
    private int port;

    public JedisMasterSlaveClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
        jedis = new Jedis(ip, port);
    }

    @Override
    public RedisValue getRedisValue(int db, String key) {
        jedis.select(db);
        return super.getRedisValue(this, db, key);
    }

    @Override
    public Object scanRedis(int db, String key) {
        jedis.select(db);
        return super.scanRedis(this, key);
    }

    @Override
    public boolean importDataToCluster(String targetIp, int targetPort, String keyFormat) {
        throw new RuntimeException("not support");
    }

    @Override
    public JedisCommands getRedisCommands() {
        return jedis;
    }

    @Override
    public MultiKeyCommands redisMultiKeyCommands() {
        return jedis;
    }


    @Override
    public void close() {
        if (null != jedis) {
            try {
                jedis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
