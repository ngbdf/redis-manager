package com.newegg.ec.redis;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * start
 *
 * @author Jay.H.Zou
 * @date 2019/7/17
 */
@EnableTransactionManagement
@MapperScan({"com.newegg.ec.redis.dao"})
@SpringBootApplication
public class RedisManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisManagerApplication.class);
    }

}
