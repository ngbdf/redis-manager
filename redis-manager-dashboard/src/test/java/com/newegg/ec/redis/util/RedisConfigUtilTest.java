package com.newegg.ec.redis.util;

import org.junit.Test;

import java.io.IOException;


/**
 * @author Jay.H.Zou
 * @date 2019/8/12
 */
public class RedisConfigUtilTest {

    @Test
    public void createConf() throws IOException {
        RedisConfigUtil.generateRedisConfig("E:/", 5, RedisConfigUtil.NORMAL_TYPE, null);
    }
}
