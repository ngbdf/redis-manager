package com.newegg.ec.redis.client;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.List;
import java.util.Set;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public class RedisClusterClient implements IRedisClusterClient {

    private JedisCluster jedisCluster;

    private RedisClient redisClient;

    private static final int TIMEOUT = 5000;

    private static final int MAX_ATTEMPTS = 3;

    public RedisClusterClient(RedisURI redisURI) {
        Set<HostAndPort> hostAndPortSet = redisURI.getHostAndPortSet();
        String redisPassword = redisURI.getRequirePass();
        jedisCluster = new JedisCluster(hostAndPortSet, TIMEOUT, TIMEOUT, MAX_ATTEMPTS, redisPassword, new GenericObjectPoolConfig());
        redisClient = RedisClientFactory.buildRedisClient(redisURI);
    }

    @Override
    public JedisCluster getRedisClusterClient() {
        return jedisCluster;
    }

    @Override
    public String clusterNodes() {
        return null;
    }

    @Override
    public boolean exists(String key) {
        return jedisCluster.exists(key);
    }

    @Override
    public String type(String key) {
        return jedisCluster.type(key);
    }

    @Override
    public Long del(String key) {
        return jedisCluster.del(key);
    }

    @Override
    public Object query(String key) {
        return null;
    }

    @Override
    public List<String> scan(String key) {
        return null;
    }

    @Override
    public String object(String type) {
        return null;
    }

    @Override
    public void close() {
        if (redisClient != null) {
            redisClient.close();
        }
        if (jedisCluster != null) {
            jedisCluster.close();
        }
    }

}
