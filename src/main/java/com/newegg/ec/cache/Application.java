package com.newegg.ec.cache;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 *
 * @author gl49
 * @date 2018/4/20
 */
@EnableScheduling
@SpringBootApplication
@MapperScan(basePackages = {"com.newegg.ec.cache"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
