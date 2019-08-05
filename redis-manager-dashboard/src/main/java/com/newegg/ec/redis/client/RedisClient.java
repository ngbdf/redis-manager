package com.newegg.ec.redis.client;

import com.google.common.base.Strings;
import redis.clients.jedis.Client;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisMonitor;
import redis.clients.jedis.util.Slowlog;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public class RedisClient implements IRedisClient {

    private static final String OK = "OK";

    private static final String PONG = "pong";

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
    public String getInfo() {
        return jedis.info();
    }

    @Override
    public String getInfo(String section) {
        return jedis.info(section);
    }

    @Override
    public String getClusterInfo() {
        return jedis.clusterInfo();
    }

    /**
     * redis 4, 4+
     * memory info
     *
     * @return
     */
    @Override
    public String getMemory() {
        return null;
    }

    /**
     * redis 4, 4+
     * memory info
     *
     * @return
     */
    @Override
    public String getMemory(String subKey) {
        return null;
    }

    /**
     * master-slave 自己实现
     *
     * @return
     */
    @Override
    public String nodes() {
        return null;
    }

    @Override
    public boolean exists(String key) {
        return jedis.exists(key);
    }

    @Override
    public String type(String key) {
        return jedis.type(key);
    }

    @Override
    public Long del(String key) {
        return jedis.del(key);
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
    public Long dbSize() {
        return jedis.dbSize();
    }

    @Override
    public String bgSave() {
        return jedis.bgsave();
    }

    @Override
    public Long lastSave() {
        return jedis.lastsave();
    }

    @Override
    public String bgRewriteAof() {
        return jedis.bgrewriteaof();
    }

    @Override
    public String slaveOf(HostAndPort hostAndPort) {
        return jedis.slaveof(hostAndPort.getHost(), hostAndPort.getPort());
    }

    @Override
    public String role() {
        // TODO: 自己实现
        return null;
    }

    @Override
    public boolean auth(String password) {
        return Objects.equals(jedis.auth(password), OK);
    }

    @Override
    public boolean shutdown() {
        return Strings.isNullOrEmpty(jedis.shutdown());
    }

    /**
     * addr=127.0.0.1:43143 fd=6 age=183 idle=0 flags=N db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=32768 obl=0 oll=0 omem=0 events=r cmd=client
     * addr=127.0.0.1:43163 fd=5 age=35 idle=15 flags=N db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=0 obl=0 oll=0 omem=0 events=r cmd=ping
     * addr=127.0.0.1:43167 fd=7 age=24 idle=6 flags=N db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=0 obl=0 oll=0 omem=0 events=r cmd=get
     * @return
     */
    @Override
    public String clientList() {
        return jedis.clientList();
    }

    @Override
    public List<String> getConfig() {
        return jedis.configGet("*");
    }

    @Override
    public List<String> getConfig(String pattern) {
        return jedis.configGet(pattern);
    }

    @Override
    public boolean rewriteConfig() {
        return Objects.equals(jedis.configRewrite(), OK);
    }

    @Override
    public boolean ping() {
        return Objects.equals(jedis.ping(), PONG);
    }

    @Override
    public String object(String type) {
        return null;
    }

    @Override
    public List<Slowlog> getSlowLog(int size) {
        return jedis.slowlogGet(size);
    }

    @Override
    public String clientSetName(String clientName) {
        return jedis.clientSetname(clientName);
    }

    @Override
    public String clusterMeet(HostAndPort hostAndPort) {
        return jedis.clusterMeet(hostAndPort.getHost(), hostAndPort.getPort());
    }

    @Override
    public String clusterReplicate(String masterId) {
        return jedis.clusterReplicate(masterId);
    }

    @Override
    public String clusterFailOver() {
        return jedis.clusterFailover();
    }


    @Override
    public void close() {
        if (jedis != null) {
            jedis.close();
        }
    }
}
