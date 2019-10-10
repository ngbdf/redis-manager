package com.newegg.ec.redis.client;

import com.newegg.ec.redis.entity.RedisNode;
import redis.clients.jedis.HostAndPort;

import java.util.Collections;
import java.util.Set;

import static com.newegg.ec.redis.entity.RedisClientName.REDIS_MANAGER_ClIENT;
import static com.newegg.ec.redis.util.RedisUtil.nodesToHostAndPortSet;

/**
 * Redis connection param
 *
 * @author Jay.H.Zou
 * @date 2019/7/18
 */
public class RedisURI {

    public static final int TIMEOUT = 5000;

    public static final int MAX_ATTEMPTS = 3;

    private String token;

    private Set<HostAndPort> hostAndPortSet;

    private String requirePass;

    private String clientName = REDIS_MANAGER_ClIENT;

    private String sentinelMasterId;

    private int database;

    public RedisURI(HostAndPort hostAndPort, String requirePass) {
        this(Collections.singleton(hostAndPort), requirePass);
    }

    public RedisURI(String host, int port, String requirePass) {
        this(new HostAndPort(host, port), requirePass);
    }

    public RedisURI(RedisNode redisNode, String requirePass) {
        this(new HostAndPort(redisNode.getHost(), redisNode.getPort()), requirePass);
    }

    public RedisURI(String nodes, String requirePass) {
        Set<HostAndPort> hostAndPortSet = nodesToHostAndPortSet(nodes);
        this.hostAndPortSet = hostAndPortSet;
        this.requirePass = requirePass;
    }

    public RedisURI(Set<HostAndPort> hostAndPortSet, String requirePass) {
        this(hostAndPortSet, requirePass, REDIS_MANAGER_ClIENT, null, 0);
    }

    public RedisURI(Set<HostAndPort> hostAndPortSet, String requirePass, String clientName, String sentinelMasterId, int database) {
        this(null, hostAndPortSet, requirePass, clientName, sentinelMasterId, database);
    }

    public RedisURI(String token, Set<HostAndPort> hostAndPortSet, String requirePass, String clientName, String sentinelMasterId, int database) {
        this.token = token;
        this.hostAndPortSet = hostAndPortSet;
        this.requirePass = requirePass;
        this.clientName = clientName;
        this.sentinelMasterId = sentinelMasterId;
        this.database = database;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRequirePass() {
        return requirePass;
    }

    public void setRequirePass(String requirePass) {
        this.requirePass = requirePass;
    }

    public Set<HostAndPort> getHostAndPortSet() {
        return hostAndPortSet;
    }

    public void setHostAndPortSet(Set<HostAndPort> hostAndPortSet) {
        this.hostAndPortSet = hostAndPortSet;
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
