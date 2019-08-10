package com.newegg.ec.redis.client;

import com.newegg.ec.redis.entity.RedisNode;
import redis.clients.jedis.HostAndPort;

/**
 * Build redis client
 *
 * @author Jay.H.Zou
 * @date 7/18/2019
 */
public class RedisClientFactory {

    public static RedisClusterClient buildRedisClusterClient(RedisURI redisURI) {
        RedisClusterClient redisClusterClient = new RedisClusterClient(redisURI);
        return redisClusterClient;
    }

    public static RedisClusterClient buildRedisClusterClient(RedisNode redisNode, String requirePass) {
        RedisURI redisURI = new RedisURI(redisNode.getHost(), redisNode.getPort(), requirePass);
        return buildRedisClusterClient(redisURI);
    }

    public static RedisClusterClient buildRedisClusterClient(HostAndPort hostAndPort, String requirePass) {
        RedisURI redisURI = new RedisURI(hostAndPort, requirePass);
        return buildRedisClusterClient(redisURI);
    }

    public static RedisClient buildRedisClient(RedisURI redisURI) {
        RedisClient redisClient = new RedisClient(redisURI);
        redisClient.ping();
        return redisClient;
    }

    public static RedisClient buildRedisClient(RedisNode redisNode, String requirePass) {
        RedisURI redisURI = new RedisURI(redisNode.getHost(), redisNode.getPort(), requirePass);
        RedisClient redisClient = buildRedisClient(redisURI);
        return redisClient;
    }

    public static RedisClient buildRedisClient(HostAndPort hostAndPort, String requirePass) {
        RedisURI redisURI = new RedisURI(hostAndPort, requirePass);
        RedisClient redisClient = buildRedisClient(redisURI);
        return redisClient;
    }

}
