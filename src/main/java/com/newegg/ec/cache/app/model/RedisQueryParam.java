package com.newegg.ec.cache.app.model;

/**
 * Created by gl49 on 2018/5/4.
 */
public class RedisQueryParam {
    private String address;
    private int db;
    private String key;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getDb() {
        return db;
    }

    public void setDb(int db) {
        this.db = db;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
