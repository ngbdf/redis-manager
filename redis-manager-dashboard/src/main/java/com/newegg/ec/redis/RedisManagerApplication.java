package com.newegg.ec.redis;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * start
 *
 * @author Jay.H.Zou
 * @date 2019/7/17
 */
@EnableScheduling
@EnableTransactionManagement
@MapperScan({"com.newegg.ec.redis.dao", "com.newegg.ec.redis.plugin"})
@SpringBootApplication
public class RedisManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisManagerApplication.class);
    }

}
