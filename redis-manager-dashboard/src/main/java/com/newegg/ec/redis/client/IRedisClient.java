package com.newegg.ec.redis.client;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Slowlog;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/18
 */
public interface IRedisClient {



    /**
     * Get redis client
     *
     * @return
     */
    Jedis getRedisClient();

    /**
     * Ping redis
     *
     * @return
     */
    boolean ping();

    /**
     * Verify redis password
     *
     * @return
     */
    boolean auth();

    /**
     * Get redis configuration
     *
     * @return
     */
    String getConfig();

    /**
     * Get redis configuration with sub key
     *
     * @param keys
     * @return
     */
    String getConfig(String... keys);

    /**
     * Rewrite redis configuration
     *
     * @return
     */
    boolean rewriteConfig();

    /**
     * Get redis node info
     *
     * @return
     */
    String getInfo();

    /**
     * Get redis node info with sub key
     *
     * @param subKey
     * @return
     */
    String getInfo(String subKey);

    /**
     * Get redis memory info
     * <p>
     * Just support redis 4, 4+
     *
     * @return
     */
    String getMemory();

    /**
     * Get redis memory info with sub key
     * <p>
     * Just support redis 4, 4+
     *
     * @param subKey
     * @return
     */
    String getMemory(String subKey);

    /**
     * Get redis version
     *
     * @return
     */
    String getVersion();

    /**
     * Get slow log
     *
     * @return
     */
    String getSlowLog();

    /**
     * Scan redis
     *
     * @return
     */
    List<String> scan(String key);

    String nodes();

    /**
     * Query redis
     * @param key
     * @return
     */
    Object query(String key);

    /**
     * Get slow log
     * @param size
     * @return
     */
    List<Slowlog> getSlowLog(int size);

    /**
     * Close client
     *
     * @return
     */
    String close();
}
