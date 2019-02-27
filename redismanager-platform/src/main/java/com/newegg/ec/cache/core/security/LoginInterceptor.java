package com.newegg.ec.cache.core.security;

import com.newegg.ec.cache.core.entity.constants.Constants;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by lf52 on 2019/2/27.
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        //获取session
        HttpSession session = request.getSession(true);
        //判断用户ID是否存在，不存在就跳转到登录界面
        if (session.getAttribute(Constants.SESSION_USER_KEY) != null) {
            session.setAttribute(Constants.SESSION_USER_KEY, session.getAttribute(Constants.SESSION_USER_KEY));
            return true;
        } else {
            // 跳转登录
            response.sendRedirect("/user/login");
            return false;
        }
    }
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }
}