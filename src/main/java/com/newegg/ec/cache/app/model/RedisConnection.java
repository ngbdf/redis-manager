package com.newegg.ec.cache.app.model;

/**
 * @author: Jay.H.Zou
 * @date: 2018/5/29
 */
public class RedisConnection {
    private String ip;
    private Integer port;
    private String password;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "RedisConnection{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", password='" + password + '\'' +
                '}';
    }
}
