package com.newegg.ec.redis.controller.oauth;

import com.newegg.ec.redis.entity.User;
import org.springframework.beans.factory.annotation.Value;

/**
 * authorization code
 *
 * @author Jay.H.Zou
 * @date 10/19/2019
 */
public class AuthService implements IOAuthService {

    @Value("${redis-manager.auth.authorization.server}")
    private String authorizationServer;

    @Override
    public User oauthLogin() {

        return null;
    }
}
