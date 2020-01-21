package com.newegg.ec.redis.client;

import com.newegg.ec.redis.util.RedisUtil;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SentinelClient implements ISentinelClient {

    private Jedis jedis;
    private JedisSentinelPool sentinelPool;

    public SentinelClient(String masterName, RedisURI redisURI) {
        Set<String> sentinels = new HashSet<>();
        Set<HostAndPort> hostAndPortSet = redisURI.getHostAndPortSet();
        for (HostAndPort hostAndPort : hostAndPortSet) {
            sentinels.add(hostAndPort.getHost() + ":" + hostAndPort.getPort());
        }
        sentinelPool = new JedisSentinelPool(masterName, sentinels);
        jedis = sentinelPool.getResource();
    }

    @Override
    public Jedis getSentinelClient() {
        return jedis;
    }

    @Override
    public Map<String, String> getSentinelInfo() throws Exception {
        return RedisUtil.parseInfoToMap(jedis.info());
    }

    @Override
    public void close() {
        if (jedis != null) {
            jedis.close();
        }
        if (sentinelPool != null) {
            sentinelPool.destroy();
        }
    }
}
