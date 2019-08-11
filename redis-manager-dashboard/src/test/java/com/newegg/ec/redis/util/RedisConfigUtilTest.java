package com.newegg.ec.redis.util;

import java.io.IOException;

import static com.newegg.ec.redis.util.RedisConfigUtil.NORMAL;

/**
 * @author Jay.H.Zou
 * @date 2019/8/12
 */
public class RedisConfigUtilTest {

    public static void main(String[] args) throws IOException {
        RedisConfigUtil.createRedisConfig("E:/", 5, NORMAL);
    }
}
