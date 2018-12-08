package com.newegg.ec.cache.app.model;

/**
 * Created by gl49 on 2018/6/13.
 */
public class RedisValue {
    private long ttl;
    private String type;
    private Object redisValue;

    public RedisValue(){

    }

    public RedisValue(long ttl, String type, Object result) {
        this.ttl = ttl;
        this.type = type;
        this.redisValue = result;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getRedisValue() {
        return redisValue;
    }

    public void setRedisValue(Object redisValue) {
        this.redisValue = redisValue;
    }
}
