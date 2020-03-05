package com.newegg.ec.redis.entity;

/**
 * @author kz37
 * @date 2018/10/23
 */
public class TopKeyReportData {
    private String key;

    private String dataType;

    private long bytes;

    private long count;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
