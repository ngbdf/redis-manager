package com.newegg.ec.redis.client;

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
    void getSentinelClient();

    /**
     * Get sentinel base info
     */
    void getSentinelInfo();



}
