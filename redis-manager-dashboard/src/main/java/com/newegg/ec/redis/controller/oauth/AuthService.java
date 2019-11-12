package com.newegg.ec.redis.controller.oauth;

import com.newegg.ec.redis.config.SystemConfig;
import com.newegg.ec.redis.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * authorization code
 *
 * @author Jay.H.Zou
 * @date 10/19/2019
 */
@Component
public class AuthService implements IOAuthService<String> {

    @Autowired
    private SystemConfig systemConfig;

    @Override
    public User oauthLogin(String data) {
        return null;
    }

    @Override
    public boolean signOut() {
        return false;
    }
}
