package com.newegg.ec.redis.client;

import com.google.common.base.Strings;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Slowlog;

import java.util.List;
import java.util.Set;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public class RedisClient implements IRedisClient {

    /**
     * info subkey
     */
    public static final String SERVER = "server";

    private Jedis jedis;

    public RedisClient(RedisURI redisURI) {
        String redisPassword = redisURI.getRequirePass();
        Set<HostAndPort> hostAndPortSet = redisURI.getHostAndPortSet();
        jedis = new Jedis(hostAndPortSet.iterator().next());
        if (!Strings.isNullOrEmpty(redisPassword)) {
            jedis.auth(redisPassword);
        }
    }

    @Override
    public Jedis getJedisClient() {
        return jedis;
    }

    @Override
    public String ping() {
        return jedis.ping();
    }

    @Override
    public boolean auth() {
        return false;
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public String getConfig(String... keys) {
        return null;
    }

    @Override
    public boolean rewriteConfig() {
        return false;
    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public String getInfo(String subKey) {
        return jedis.info(subKey);
    }

    @Override
    public String getMemory() {
        return null;
    }

    @Override
    public String getMemory(String subKey) {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public String getSlowLog() {
        return null;
    }

    @Override
    public List<String> scan(String key) {
        return null;
    }

    @Override
    public String nodes() {
        return null;
    }

    @Override
    public Object query(String key) {
        return null;
    }

    @Override
    public List<Slowlog> getSlowLog(int size) {
        return null;
    }

    @Override
    public String clusterMeet(String ip, int port) {
        return null;
    }

    @Override
    public String clusterReplicate(String masterId) {
        return null;
    }

    @Override
    public String clusterFailover() {
        return null;
    }

    @Override
    public void close() {
        if (jedis != null) {
            jedis.close();
        }
    }
}
