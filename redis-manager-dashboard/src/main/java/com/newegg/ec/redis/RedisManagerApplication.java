package com.newegg.ec.redis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * start
 *
 * @author Jay.H.Zou
 * @date 2019/7/17
 */
@SpringBootApplication
public class RedisManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisManagerApplication.class);
    }

}
