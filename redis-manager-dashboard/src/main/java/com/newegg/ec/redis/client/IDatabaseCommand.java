package com.newegg.ec.redis.client;

import java.util.List;

/**
 * 数据相关操作
 *
 * @author Jay.H.Zou
 * @date 2019/8/5
 */
public interface IDatabaseCommand {

    boolean exists(String key);

    String type(String key);

    Long del(String key);

    /**
     * Query redis
     *
     * @param key
     * @return
     */
    Object query(String key);

    /**
     * Scan redis
     *
     * @return
     */
    List<String> scan(String key);

    String object(String type);
}
