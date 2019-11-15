package com.newegg.ec.redis.entity;

import redis.clients.jedis.ScanResult;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
public class AutoCommandResult {

    private long ttl;

    private String type;

    private Object value;

    public AutoCommandResult() {
    }

    public AutoCommandResult(List<String> result) {
        this.value = result;
    }

    public AutoCommandResult(ScanResult<String> scanResult) {
        this.value = scanResult.getResult();
    }

    public AutoCommandResult(long ttl, String type, Object value) {
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

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AutoCommandResult{");
        sb.append("ttl=").append(ttl);
        sb.append(", type='").append(type).append('\'');
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }
}
