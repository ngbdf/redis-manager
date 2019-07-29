package com.newegg.ec.cache.core.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Created by lzz on 2018/5/2.
 */
@Configuration
public class WebSecurityConfig implements WebMvcConfigurer{

    private final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/public/**");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        logger.info("add interceptors");
        registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/alert/*","/cluster/*","/monitor/*","/node/*");

    }

}