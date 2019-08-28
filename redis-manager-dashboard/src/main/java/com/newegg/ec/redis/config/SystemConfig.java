package com.newegg.ec.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Jay.H.Zou
 * @date 7/6/2019
 */
@Configuration
public class SystemConfig implements WebMvcConfigurer, ApplicationListener<ContextRefreshedEvent> {

    public static final String CONFIG_ORIGINAL_PATH = "/redis/config/";

    public static final String MACHINE_PACKAGE_ORIGINAL_PATH = "/redis/machine/";

    @Value("${server.port}")
    private int serverPort;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

    }

   public int getServerPort(){
       return serverPort;
   }
}
