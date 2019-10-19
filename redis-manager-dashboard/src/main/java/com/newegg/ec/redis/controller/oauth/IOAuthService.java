package com.newegg.ec.redis.controller.oauth;

import com.newegg.ec.redis.entity.User;

/**
 * @author Jay.H.Zou
 * @date 10/19/2019
 */
public interface IOAuthService {

    User oauthLogin();

}
