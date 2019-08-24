package com.newegg.ec.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

import static com.newegg.ec.redis.util.RedisConfigUtil.CONFIG_PATH;
import static com.newegg.ec.redis.util.SignUtil.SLASH;

/**
 * @author Jay.H.Zou
 * @date 7/6/2019
 */
@Configuration
public class SystemConfig implements WebMvcConfigurer {

    @Value("${redis-manager.install.data-dir:/data/redis-manager/}")
    private String dataDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!dataDir.endsWith(SLASH)) {
            dataDir += SLASH;
        }
        File file = new File(dataDir);
        if (!file.exists()) {
            if (file.mkdirs()) {
                throw new RuntimeException(dataDir + " create failed.");
            }
        }
        registry.addResourceHandler(CONFIG_PATH + "**").addResourceLocations("file:" + dataDir);
    }

   /* @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //文件最大KB,MB
        factory.setMaxFileSize(DataSize.ofBytes(10485760));
        //设置总上传数据总大小
        factory.setMaxRequestSize(DataSize.ofBytes(10485760));
        return factory.createMultipartConfig();
    }*/

   public String getDataDir() {
       return dataDir;
   }

}
