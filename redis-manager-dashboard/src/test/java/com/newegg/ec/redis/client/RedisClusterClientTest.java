package com.newegg.ec.redis.client;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.entity.RedisNode;
import org.junit.Test;
import redis.clients.jedis.HostAndPort;

import java.util.List;

import static org.junit.Assert.*;

public class RedisClusterClientTest {

    private static RedisURI redisURI = new RedisURI(new HostAndPort("127.0.0.1", 8017), null);

    private static RedisClusterClient redisClusterClient = RedisClientFactory.buildRedisClusterClient(redisURI);

    @Test
    public void getRedisClusterClient() {
    }

    @Test
    public void clusterNodes() throws Exception {
        List<RedisNode> redisNodeList = redisClusterClient.clusterNodes();
        for (RedisNode redisNode : redisNodeList) {
            String jsonString = JSONObject.toJSONString(redisNode);
            System.err.println(jsonString);
        }
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
    public void object1() {
    }

    @Test
    public void close() {
    }
}