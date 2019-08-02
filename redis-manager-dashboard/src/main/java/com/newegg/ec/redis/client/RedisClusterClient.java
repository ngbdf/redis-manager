package com.newegg.ec.redis.client;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.util.Slowlog;

import java.util.List;
import java.util.Map;
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
        redisClient = ClientFactory.buildRedisClient(redisURI);
    }

    @Override
    public JedisCluster getRedisClusterClient() {
        return jedisCluster;
    }

    @Override
    public String getClusterInfo() {
        Jedis jedis = redisClient.getJedisClient();
        return jedis.clusterInfo();
    }

    @Override
    public String getNodeList() {
        Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
        System.err.println(clusterNodes);
        String nodes = redisClient.nodes();
        System.err.println(nodes);
        return null;
    }


    @Override
    public String getMasterList() {
        return null;
    }

    @Override
    public String getSlaveList() {
        return null;
    }

    @Override
    public Jedis getJedisClient() {
        return redisClient.getJedisClient();
    }

    @Override
    public String ping() {
        return null;
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
        return redisClient.getInfo(subKey);
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
        if (redisClient != null) {
            redisClient.close();
        }
        if (jedisCluster != null) {
            jedisCluster.close();
        }
    }
}
