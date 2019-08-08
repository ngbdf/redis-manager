package com.newegg.ec.redis.entity;

import redis.clients.jedis.ScanResult;

/**
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
public class AutoCommandResult {

    private long ttl;

    private String type;

    private String cursor;

    private boolean completeIteration;

    private Object value;

    public AutoCommandResult() {
    }

    public AutoCommandResult(ScanResult<String> scanResult) {
        this.cursor = scanResult.getCursor();
        this.completeIteration = scanResult.isCompleteIteration();
        this.value = scanResult.getResult();
    }


    public AutoCommandResult(String cursor, boolean completeIteration, Object value) {
        this.cursor = cursor;
        this.completeIteration = completeIteration;
        this.value = value;
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

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public boolean isCompleteIteration() {
        return completeIteration;
    }

    public void setCompleteIteration(boolean completeIteration) {
        this.completeIteration = completeIteration;
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
        sb.append(", cursor='").append(cursor).append('\'');
        sb.append(", completeIteration=").append(completeIteration);
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }
}
