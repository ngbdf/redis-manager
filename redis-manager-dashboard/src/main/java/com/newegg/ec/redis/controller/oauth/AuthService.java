package com.newegg.ec.redis.controller.oauth;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.newegg.ec.redis.config.SystemConfig;
import com.newegg.ec.redis.entity.User;
import com.newegg.ec.redis.util.LinuxInfoUtil;
import com.newegg.ec.redis.util.SignUtil;
import com.newegg.ec.redis.util.httpclient.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.SocketException;


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
        // build request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("Code", data);
        requestBody.put("SiteKey", systemConfig.getSiteKey());
        requestBody.put("SiteSecret", systemConfig.getSiteSecret());
        String authorizationServer = systemConfig.getAuthorizationServer();
        String postResponse = null;
        try {
            postResponse = HttpClientUtil.post(authorizationServer + "/api/token", requestBody);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        JSONObject response = JSONObject.parseObject(postResponse);
        String token = response.getString("Token");
        JSONObject userInfo = null;
        if (!Strings.isNullOrEmpty(token)) {
            String userInfoStr;
            try {
                userInfoStr = HttpClientUtil.get(authorizationServer + "/api/user?token=" + token);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            userInfo = JSONObject.parseObject(userInfoStr);
        }
        if (userInfo == null) {
            return null;
        }
        User user = new User();
        String userName = userInfo.getString("UserName");
        String avatar = userInfo.getJSONObject("DomainUser").getString("Avatar");
        user.setUserName(userName);
        user.setAvatar(avatar);
        return user;
    }

    @Override
    public boolean signOut() {
        String urlTemplate = "%s/api/%s?callback=%s";
        String authorizationServer = systemConfig.getAuthorizationServer();
        String siteKey = systemConfig.getSiteKey();
        String serverAddress = "";
        try {
            serverAddress = LinuxInfoUtil.getIpAddress() + SignUtil.COLON + systemConfig.getServerPort();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            HttpClientUtil.get(String.format(urlTemplate, authorizationServer, siteKey, serverAddress));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
