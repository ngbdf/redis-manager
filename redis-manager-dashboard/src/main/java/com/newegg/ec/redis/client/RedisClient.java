package com.newegg.ec.redis.client;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Slowlog;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public class RedisClient implements IRedisClient{

    /** info subkey */
    public static final String SERVER = "server";

    private RedisURI redisURI;

    public RedisClient(RedisURI redisURI) {
        this.redisURI = redisURI;
    }

    @Override
    public Jedis getRedisClient() {
        return null;
    }

    @Override
    public boolean ping() {
        return false;
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
        return null;
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
    public String close() {
        return null;
    }
}
