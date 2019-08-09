package com.newegg.ec.redis.client;

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

    public static RedisClient buildRedisClient(RedisURI redisURI) {
        RedisClient redisClient = new RedisClient(redisURI);
        redisClient.ping();
        return redisClient;
    }

}
