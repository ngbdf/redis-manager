package com.newegg.ec.redis.controller.security;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.controller.oauth.IOAuthService;
import com.newegg.ec.redis.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by lzz on 2018/5/2.
 */
@Component
public class WebSecurityConfig implements WebMvcConfigurer {

    @Autowired
    private IOAuthService authService;

    @Bean
    public SecurityInterceptor getSecurityInterceptor() {
        return new SecurityInterceptor();
    }

    private JSONObject getPrdParam(String nasCode) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Code", nasCode);
        jsonObject.put("SiteKey", "1t9mf7bctjiik2na80716kmcw0");
        jsonObject.put("SiteSecret", "0dhnpn9lh15qa2excdepkfj21r2f2jf1i9nq6y124ksxv395ozsu");
        return jsonObject;
    }

    private JSONObject getDevParam(String nasCode) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Code", nasCode);
        jsonObject.put("SiteKey", "2gb6tcmz3udop1yy5cw1621djf");
        jsonObject.put("SiteSecret", "1zsj643ywec7o2ww0nsdonoebz201urtrxrmrsl29nr78t49p1vi");
        return jsonObject;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        InterceptorRegistration addInterceptor = registry.addInterceptor(getSecurityInterceptor());
        // 排除配置
        addInterceptor.excludePathPatterns("/user/login")
                .excludePathPatterns("/user/signOut")
                .excludePathPatterns("/user/getUserFromSession")
                .excludePathPatterns("/system/getAuthorization");
        // 拦截配置
        addInterceptor.addPathPatterns("/**");
    }

    public class SecurityInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            String requestURI = request.getRequestURI();
            System.err.println(requestURI);
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");
            if (user != null) {
                return true;
            }
            user = authService.oauthLogin(request);
            if (user != null) {
                user = new User();
                user.setUserId(35);
                user.setUserName("nihao");
                user.setUserRole(User.UserRole.SUPER_ADMIN);
                System.err.println(user + "====================");
                session.setAttribute("user", user);
                return true;
            }
            return true;
        }
    }
}