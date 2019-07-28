package com.newegg.ec.redis.entity;

import java.sql.Timestamp;

/**
 * 参考 CacheCloud
 *
 * @author Jay.H.Zou
 * @date 2019/7/28
 */
public class RedisConfig {

    private int redisConfigId;

    private String configKey;

    private String configValue;

    private String info;

    private Timestamp updateTime;

    /**
     * 类型：0:redis普通节点, 1.cluster节点特殊配置, 2:sentinel节点配置
     */
    private int type;
    /**
     * redis server version
     * 4: redis4, 5: redis 5
     */
    private int version;

    /**
     * 1有效,0无效
     */
    private boolean status;

    public int getRedisConfigId() {
        return redisConfigId;
    }

    public void setRedisConfigId(int redisConfigId) {
        this.redisConfigId = redisConfigId;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
