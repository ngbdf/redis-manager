package com.newegg.ec.redis.client;

import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * Sentinel client operation interface
 *
 * @author Jay.H.Zou
 * @date 2020/1/15
 */
public interface ISentinelClient {

    /**
     * Get sentinel client
     */
    Jedis getSentinelClient();

    /**
     * Get sentinel base info
     */
    Map<String, String> getSentinelInfo() throws Exception;

    void close();


}
