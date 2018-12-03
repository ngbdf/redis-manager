package com.newegg.ec.cache.app.model;

/**
 * @author: Jay.H.Zou
 * @date: 2018/5/29
 */
public class ConnectionParam {
    private String ip;
    private Integer port;
    private String redisPassword;

    public ConnectionParam() {}

    public ConnectionParam(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    public ConnectionParam(String ip, Integer port, String redisPassword) {
        this.ip = ip;
        this.port = port;
        this.redisPassword = redisPassword;
    }

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

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    @Override
    public String toString() {
        return "ConnectionParam{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", redisPassword='" + redisPassword + '\'' +
                '}';
    }
}
