package com.newegg.ec.cache.module.clusterbuild.common.redis;

import com.newegg.ec.cache.core.entity.redis.ConnectionParam;
import com.newegg.ec.cache.core.entity.redis.RedisValue;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;

/**
 * Created by lzz on 2018/3/19.
 */
public class RedisStandAloneClient extends RedisClientBase implements IRedis {
    private Jedis jedis;
    private String ip;
    private int port;
    private String password;

    public RedisStandAloneClient(ConnectionParam param) {
        this.ip = param.getIp();
        this.port = param.getPort();
        this.password = param.getRedisPassword();
        jedis = new Jedis(ip, port);
        if(StringUtils.isNotBlank(this.password)) {
            jedis.auth(this.password);
        }
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
