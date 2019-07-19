package com.newegg.ec.redis.client;

import redis.clients.jedis.HostAndPort;

/**
 * Redis connection param
 *
 * @author Jay.H.Zou
 * @date 2019/7/18
 */
public class RedisURI {

    private String token;

    private HostAndPort hostAndPort;

    private String requirePass;

    private String clientName;

    private String sentinelMasterId;

    private int database;


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public HostAndPort getHostAndPort() {
        return hostAndPort;
    }

    public void setHostAndPort(HostAndPort hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    public String getRequirePass() {
        return requirePass;
    }

    public void setRequirePass(String requirePass) {
        this.requirePass = requirePass;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getSentinelMasterId() {
        return sentinelMasterId;
    }

    public void setSentinelMasterId(String sentinelMasterId) {
        this.sentinelMasterId = sentinelMasterId;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }
}
