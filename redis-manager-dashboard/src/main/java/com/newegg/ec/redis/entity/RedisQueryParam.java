package com.newegg.ec.redis.entity;

/**
 * Created by gl49 on 2018/5/4.
 */
public class RedisQueryParam {

    private int clusterId;

    private int database;

    private String key;

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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
