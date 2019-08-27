package com.newegg.ec.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Jay.H.Zou
 * @date 7/6/2019
 */
@Configuration
public class SystemConfig implements WebMvcConfigurer {

    public static final String CONFIG_ORIGINAL_PATH = "/redis/config/";

    public static final String MACHINE_PACKAGE_ORIGINAL_PATH = "/redis/machine/";

    @Value("${server.port}")
    private int serverPort;

   /* @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //文件最大KB,MB
        factory.setMaxFileSize(DataSize.ofBytes(10485760));
        //设置总上传数据总大小
        factory.setMaxRequestSize(DataSize.ofBytes(10485760));
        return factory.createMultipartConfig();
    }*/

   public int getServerPort(){
       return serverPort;
   }


}
