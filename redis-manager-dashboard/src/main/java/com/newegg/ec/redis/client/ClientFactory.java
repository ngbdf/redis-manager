package com.newegg.ec.redis.client;

/**
 * Build redis client
 *
 * @author Jay.H.Zou
 * @date 7/18/2019
 */
public class ClientFactory {


    public static RedisClusterClient buildRedisClusterClient(RedisURI redisURI) {
        return new RedisClusterClient(redisURI);
    }

    public static RedisClient buildRedisClient(RedisURI redisURI) {
        return new RedisClient(redisURI);
    }

}
