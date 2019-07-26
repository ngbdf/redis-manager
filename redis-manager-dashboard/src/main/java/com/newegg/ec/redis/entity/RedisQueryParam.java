package com.newegg.ec.redis.entity;

/**
 * Created by gl49 on 2018/5/4.
 */
public class RedisQueryParam {

    private String clusterId;

    private String address;

    private int database;

    private String key;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
