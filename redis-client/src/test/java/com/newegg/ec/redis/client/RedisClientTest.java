package com.newegg.ec.redis.client;

import com.newegg.ec.redis.client.extend.RedisClient;
import org.junit.Test;

/**
 * Created by lf52 on 2019/2/22.
 */
public class RedisClientTest {

    @Test
    public void testRedisClient(){

        RedisClient redisClient = new RedisClient("10.16.46.195", 8800);
        String result = null;
        try {
            // result = redisClient.redisCommandOpt("*2\r\n$6\r\nconfig\r\n$7\r\nrewrite\r\n");
            result = redisClient.redisCommandOpt("12345678","*2\r\n$6\r\nconfig\r\n$7\r\nrewrite\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            redisClient.closeClient();
        }
        System.out.println(result);
    }
}
