package com.newegg.ec.redis.entity;


import redis.clients.jedis.ScanParams;

/**
 * @author Jay
 */
public class RedisQueryParam {

    private int clusterId;

    private int database;

    private String cursor;

    private int count;

    private String key;

    public ScanParams buildScanParams() {
        ScanParams scanParams = new ScanParams();
        scanParams.count(this.getCount());
        scanParams.match(this.getKey());
        return scanParams;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
