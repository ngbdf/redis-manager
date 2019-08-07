package com.newegg.ec.redis.entity;

/**
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
public class RedisQueryResult {

    private long ttl;

    private String type;

    private Object value;

    public RedisQueryResult(long ttl, String type, Object value) {
        this.ttl = ttl;
        this.type = type;
        this.value = value;
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

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
