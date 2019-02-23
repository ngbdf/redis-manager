package com.newegg.ec.cache.core.entity.redis;

/**
 * Created by lf52 on 2019/2/23.
 */
public class RedisConnectParam {

    private String ip;
    private Integer port;
    private String redisPassword;

    public RedisConnectParam() {}

    public RedisConnectParam(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    public RedisConnectParam(String ip, Integer port, String redisPassword) {
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
