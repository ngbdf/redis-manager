package com.newegg.ec.redis.controller.oauth;

import com.newegg.ec.redis.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * authorization code
 *
 * @author Jay.H.Zou
 * @date 10/19/2019
 */
@Component
public class AuthService implements IOAuthService {

    @Value("${redis-manager.auth.authorization.site-key}")
    private String siteKey;

    @Value("${redis-manager.auth.authorization.site-secret}")
    private String siteSecret;

    @Override
    public User oauthLogin(HttpServletRequest request) {
        return null;
    }
}
