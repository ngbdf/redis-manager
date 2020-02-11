package com.newegg.ec.redis.client;

import com.newegg.ec.redis.entity.RedisNode;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Set;

/**
 * Build redis client
 *
 * @author Jay.H.Zou
 * @date 7/18/2019
 */
public class RedisClientFactory {

    public static RedisClusterClient buildRedisClusterClient(RedisURI redisURI) {
        return new RedisClusterClient(redisURI);
    }

    public static RedisClusterClient buildRedisClusterClient(RedisNode redisNode, String requirePass) {
        RedisURI redisURI = new RedisURI(redisNode.getHost(), redisNode.getPort(), requirePass);
        return buildRedisClusterClient(redisURI);
    }

    public static RedisClient buildRedisClient(RedisURI redisURI) {
        RedisClient redisClient = new RedisClient(redisURI);
        if (redisClient.getJedisClient() == null) {
            throw new JedisConnectionException("All seed node can't connect.");
        }
        return redisClient;
    }

    public static RedisClient buildRedisClient(RedisNode redisNode) {
        return buildRedisClient(redisNode, null);
    }

    public static RedisClient buildRedisClient(RedisNode redisNode, String requirePass) {
        RedisURI redisURI = new RedisURI(redisNode.getHost(), redisNode.getPort(), requirePass);
        return buildRedisClient(redisURI);
    }

    public static RedisClient buildRedisClient(HostAndPort hostAndPort, String requirePass) {
        RedisURI redisURI = new RedisURI(hostAndPort, requirePass);
        return buildRedisClient(redisURI);
    }

    public static RedisClient buildRedisClient(HostAndPort hostAndPort) {
        return buildRedisClient(hostAndPort, null);
    }

    public static RedisClient buildRedisClient(String host, int port) {
        return buildRedisClient(new HostAndPort(host, port));
    }

    public static RedisClient buildRedisClient(Set<HostAndPort> hostAndPorts) {
        return buildRedisClient(hostAndPorts.iterator().next());
    }

}
