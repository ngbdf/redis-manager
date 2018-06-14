package com.newegg.ec.cache.app.model;

/**
 * Created by gl49 on 2018/6/13.
 */
public class RedisValue {
    private long ttl;
    private String type;
    private Object result;

    public RedisValue(long ttl, String type, Object result) {
        this.ttl = ttl;
        this.type = type;
        this.result = result;
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

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
