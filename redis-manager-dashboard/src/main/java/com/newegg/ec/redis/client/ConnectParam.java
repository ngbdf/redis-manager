package com.newegg.ec.redis.client;

import redis.clients.jedis.HostAndPort;

/**
 * Redis connection param
 *
 * @author Jay.H.Zou
 * @date 2019/7/18
 */
public class ConnectParam {

    private String token;

    private HostAndPort hostAndPort;

    private String requirePass;

    private String clientName;

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

    @Override
    public String toString() {
        return "ConnectParam{" +
                "token='" + token + '\'' +
                ", hostAndPort=" + hostAndPort +
                ", requirePass='" + requirePass + '\'' +
                ", clientName='" + clientName + '\'' +
                '}';
    }
}
