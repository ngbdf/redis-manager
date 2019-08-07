package com.newegg.ec.redis.client;

import org.junit.Test;
import redis.clients.jedis.HostAndPort;

import java.util.Map;

import static org.junit.Assert.*;

public class RedisClientTest {

    private static RedisURI redisURI = new RedisURI(new HostAndPort("172.16.35.77", 8012), null);

    private static RedisClient redisClient = RedisClientFactory.buildRedisClient(redisURI);

    @Test
    public void getJedisClient() {
    }

    @Test
    public void getInfo() {
        Map<String, String> configMap = redisClient.getConfig();
        configMap.forEach((key, val) -> {
            System.err.println(key + "=" + val);
        });
    }

    @Test
    public void getInfo1() {
    }

    @Test
    public void getClusterInfo() {
    }

    @Test
    public void getMemory() {
    }

    @Test
    public void getMemory1() {
    }

    @Test
    public void nodes() {
    }

    @Test
    public void exists() {
    }

    @Test
    public void type() {
    }

    @Test
    public void del() {
    }

    @Test
    public void query() {
    }

    @Test
    public void scan() {
    }

    @Test
    public void dbSize() {
    }

    @Test
    public void bgSave() {
    }

    @Test
    public void lastSave() {
    }

    @Test
    public void bgRewriteAof() {
    }

    @Test
    public void slaveOf() {
    }

    @Test
    public void role() {
    }

    @Test
    public void auth() {
    }

    @Test
    public void shutdown() {
    }

    @Test
    public void clientList() {
    }

    @Test
    public void getConfig() {

    }


    @Test
    public void rewriteConfig() {
    }

    @Test
    public void ping() {
    }

    @Test
    public void object1() {
    }

    @Test
    public void getSlowLog() {
    }

    @Test
    public void clientSetName() {
    }

    @Test
    public void clusterMeet() {
    }

    @Test
    public void clusterReplicate() {
    }

    @Test
    public void clusterFailOver() {
    }

    @Test
    public void clusterAddSlots() {
    }

    @Test
    public void clusterForget() {
    }

    @Test
    public void clusterSlaves() {
    }
}