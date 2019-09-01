package com.newegg.ec.redis.schedule;

/**
 * Data collection interface
 *
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public interface IDataCollection {

    /**
     * 定期收集
     */
    void collect();

}
