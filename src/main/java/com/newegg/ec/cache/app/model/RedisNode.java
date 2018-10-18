package com.newegg.ec.cache.app.model;

/**
 * Created by gl49 on 2018/5/9.
 */
public class RedisNode {
    int port;
    private String ip;
    private RedisNodeType role;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public RedisNodeType getRole() {
        return role;
    }

    public void setRole(RedisNodeType role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "RedisNode{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", role=" + role +
                '}';
    }
}
