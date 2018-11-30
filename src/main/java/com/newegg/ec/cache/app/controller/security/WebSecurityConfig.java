package com.newegg.ec.cache.app.controller.security;

import com.newegg.ec.cache.app.model.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by lzz on 2018/5/2.
 */
@Configuration
public class WebSecurityConfig extends WebMvcConfigurerAdapter {
    @Bean
    public SecurityInterceptor getSecurityInterceptor() {
        return new SecurityInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration addInterceptor = registry.addInterceptor(getSecurityInterceptor());
        // 排除配置
        addInterceptor.excludePathPatterns("/user/verifyLogin");
        addInterceptor.excludePathPatterns("/user/login");
        // 拦截配置
        addInterceptor.addPathPatterns("/**");
    }

    private class SecurityInterceptor extends HandlerInterceptorAdapter {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            HttpSession session = request.getSession();
            /*
            User user = new User();
            user.setId(1);
            user.setUserGroup("admin");
            session.setAttribute(Constants.SESSION_USER_KEY, user);
            */
            if (session.getAttribute(Constants.SESSION_USER_KEY) != null) {
                return true;
            } else {
                // 跳转登录
                response.sendRedirect("/user/login");
                return false;
            }
        }
    }
}