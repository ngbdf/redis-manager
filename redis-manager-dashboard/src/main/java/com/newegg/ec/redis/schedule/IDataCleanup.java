package com.newegg.ec.redis.schedule;

/**
 * Data cleanup interface
 *
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public interface IDataCleanup {

    /**
     * 定期清理
     */
    void cleanup();
}
