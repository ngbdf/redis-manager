package com.newegg.ec.redis.entity;

import com.google.common.base.Strings;
import redis.clients.jedis.ScanParams;

/**
 * @author Jay.H.Zou
 * @date 8/8/2019
 */
public class AutoCommandParam extends DataCommandsParam {

    private String cursor;

    private int count;

    private String key;

    public ScanParams buildScanParams() {
        ScanParams scanParams = new ScanParams();
        if (Strings.isNullOrEmpty(this.getCursor())) {
            this.setCursor("0");
        }
        scanParams.count(this.getCount());
        if (Strings.isNullOrEmpty(this.getKey())) {
            this.setKey("*");
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
