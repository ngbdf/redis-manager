package com.newegg.ec.redis.config; /**
 * 
 */


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author Hulva Luva.H
 * @since 2018年4月12日
 *
 */
@Configuration
public class RestTemplateConfig {
  @Bean
  public RestTemplate buildRestTemplate() {
    return new RestTemplate(simpleClientHttpRequestFactory());
  }

  public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setReadTimeout(10000);
    factory.setConnectTimeout(15000);
    return factory;
  }
}
