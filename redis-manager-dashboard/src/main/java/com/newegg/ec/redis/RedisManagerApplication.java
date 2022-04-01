package com.newegg.ec.redis;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * start
 *
 * @author Jay.H.Zou
 * @date 2019/7/17
 */
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
@MapperScan({"com.newegg.ec.redis.dao", "com.newegg.ec.redis.plugin"})
@EnableEurekaServer
@SpringBootApplication
public class RedisManagerApplication {
    private final Logger log = LoggerFactory.getLogger(RedisManagerApplication.class);
    private String datasourceUrl;
    public static void main(String[] args) {
        SpringApplication.run(RedisManagerApplication.class);
    }

}
