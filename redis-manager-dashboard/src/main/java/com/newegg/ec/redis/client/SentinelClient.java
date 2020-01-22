package com.newegg.ec.redis.client;

import com.newegg.ec.redis.util.RedisUtil;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.newegg.ec.redis.client.RedisURI.TIMEOUT;

public class SentinelClient implements ISentinelClient {

    private Jedis jedis;

    public SentinelClient(RedisURI redisURI) {
        Set<HostAndPort> hostAndPortSet = redisURI.getHostAndPortSet();
        for (HostAndPort hostAndPort : hostAndPortSet) {
            try {
                jedis = new Jedis(hostAndPort.getHost(), hostAndPort.getPort(), TIMEOUT, TIMEOUT);
            } catch (JedisConnectionException e) {
                close();
            }
        }
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
    public List<Map<String, String>> getSentinelMasters() {
        return jedis.sentinelMasters();
    }

    @Override
    public List<String> getMasterAddrByName(String masterName) {
        return jedis.sentinelGetMasterAddrByName(masterName);
    }

    @Override
    public List<Map<String, String>> getSentinelSlaves(String masterName) {
        return jedis.sentinelSlaves(masterName);
    }

    @Override
    public String monitorMaster(String masterName, String ip, int port, int quorum) {
        return jedis.sentinelMonitor(masterName, ip, port, quorum);
    }

    @Override
    public String failoverMaster(String masterName) {
        return jedis.sentinelFailover(masterName);
    }

    @Override
    public String removeMaster(String masterName) {
        return jedis.sentinelRemove(masterName);
    }

    @Override
    public Long resetConfig(String pattern) {
        return jedis.sentinelReset(pattern);
    }

    @Override
    public String setConfig(String masterName, Map<String, String> parameterMap) {
        return jedis.sentinelSet(masterName, parameterMap);
    }

    @Override
    public void close() {
        try {
            if (jedis != null) {
                jedis.close();
            }
        } catch (Exception ignored) {

        }
    }
}
