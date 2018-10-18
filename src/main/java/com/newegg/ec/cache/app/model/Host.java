package com.newegg.ec.cache.app.model;

/**
 * Created by gl49 on 2018/4/21.
 */
public class Host {
    private String ip;
    private int port;

    public Host() {

    }

    public Host(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

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

    @Override
    public String toString() {
        return "Host{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
