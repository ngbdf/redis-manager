package com.newegg.ec.redis.client;

import redis.clients.jedis.Jedis;

import java.util.List;
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

    List<Map<String, String>> getSentinelMasters();

    List<String> getMasterAddrByName(String masterName);

    List<Map<String, String>> getSentinelSlaves(String masterName);

    String monitorMaster(String masterName, String ip, int port, int quorum);

    String failoverMaster(String masterName);

    String removeMaster(String masterName);

    Long resetConfig(String pattern);

    String setConfig(String masterName, Map<String, String> parameterMap);

    void close();


}
