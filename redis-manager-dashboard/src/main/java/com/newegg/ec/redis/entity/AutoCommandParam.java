package com.newegg.ec.redis.entity;

import com.google.common.base.Strings;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

/**
 * @author Jay.H.Zou
 * @date 8/8/2019
 */
public class AutoCommandParam extends DataCommandsParam {

    private String cursor;

    private Integer count;

    private String key;

    public ScanParams buildScanParams() {
        ScanParams scanParams = new ScanParams();
        if (Strings.isNullOrEmpty(this.getCursor())) {
            this.setCursor("0");
        }
        scanParams.count(this.getCount());
        if (Strings.isNullOrEmpty(this.getKey())) {
            this.setKey("*");
        } else if (key.indexOf("*") != 0
                && key.indexOf("*") != (key.length() - 1)) {
            key = "*" + key + "*";
        }
        scanParams.match(this.getKey());
        return scanParams;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
