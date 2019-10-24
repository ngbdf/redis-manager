package com.newegg.ec.redis.config;

import com.google.common.base.Strings;
import com.newegg.ec.redis.exception.ConfigurationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

import static com.newegg.ec.redis.util.RedisUtil.CLUSTER;
import static com.newegg.ec.redis.util.RedisUtil.STANDALONE;
import static com.newegg.ec.redis.util.TimeUtil.FIVE_MINUTES;

/**
 * @author Jay.H.Zou
 * @date 7/6/2019
 */
@Configuration
public class SystemConfig implements WebMvcConfigurer {

    public static final String CONFIG_ORIGINAL_PATH = "/redis-manager/conf/";

    public static final String MACHINE_PACKAGE_ORIGINAL_PATH = "/redis-manager/machine/";

    public static final String AVATAR_PATH = "/redis-manager/avatar/";

    @Value("${server.port}")
    private int serverPort;

    @Value("${redis-manager.install.conf-path}")
    private String configPath;

    @Value("${redis-manager.install.machine.package-path}")
    private String machinePackagePath;

    @Value("${redis-manager.auth.avatar-path}")
    private String avatarPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (Strings.isNullOrEmpty(configPath)) {
            throw new ConfigurationException("conf-path is empty.");
        }
        File file = new File(configPath);
        if (!file.exists()) {
            File clusterConfPath = new File(configPath + CLUSTER);
            clusterConfPath.mkdirs();
            File standaloneConfPath = new File(configPath + STANDALONE);
            standaloneConfPath.mkdirs();
        }

        if (Strings.isNullOrEmpty(machinePackagePath)) {
            throw new ConfigurationException("machine.package-path is empty.");
        }
        File file2 = new File(machinePackagePath);
        if (!file2.exists()) {
            file2.mkdirs();
        }

        if (Strings.isNullOrEmpty(avatarPath)) {
            throw new ConfigurationException("avatar-path is empty.");
        }
        File file3 = new File(avatarPath);
        if (!file3.exists()) {
            file3.mkdirs();
        }

        registry.addResourceHandler(CONFIG_ORIGINAL_PATH + "**").addResourceLocations("file:" + configPath);
        registry.addResourceHandler(MACHINE_PACKAGE_ORIGINAL_PATH + "**").addResourceLocations("file:" + machinePackagePath);
    }

    @Override
    public void configureAsyncSupport(final AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(FIVE_MINUTES);
        configurer.registerCallableInterceptors(timeoutInterceptor());
    }
    @Bean
    public TimeoutCallableProcessingInterceptor timeoutInterceptor() {
        return new TimeoutCallableProcessingInterceptor();
    }


    public int getServerPort() {
        return serverPort;
    }
}
