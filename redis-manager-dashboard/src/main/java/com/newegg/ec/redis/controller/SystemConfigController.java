package com.newegg.ec.redis.controller;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.config.SystemConfig;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.plugin.install.entity.InstallationEnvironment;
import com.newegg.ec.redis.util.LinuxInfoUtil;
import com.newegg.ec.redis.util.SignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 10/29/2019
 */
@RequestMapping("/system/*")
@Controller
public class SystemConfigController implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private SystemConfig systemConfig;

    private List<Integer> INSTALLATION_ENVIRONMENT = new ArrayList<>();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        INSTALLATION_ENVIRONMENT.add(InstallationEnvironment.DOCKER);
        INSTALLATION_ENVIRONMENT.add(InstallationEnvironment.MACHINE);
        if (systemConfig.getHumpbackEnabled()) {
            INSTALLATION_ENVIRONMENT.add(InstallationEnvironment.HUMPBACK);
        }
    }

    @RequestMapping(value = "/humpbackEnabled", method = RequestMethod.GET)
    @ResponseBody
    public Result getHumpbackEnabled() {
        return Result.successResult(systemConfig.getHumpbackEnabled());
    }

    @RequestMapping(value = "/getAuthorization", method = RequestMethod.GET)
    @ResponseBody
    public Result getAuthorization() {
        JSONObject authorization = new JSONObject();
        authorization.put("enabled", systemConfig.getAuthorizationEnabled());
        authorization.put("server", systemConfig.getAuthorizationServer());
        authorization.put("siteKey", systemConfig.getSiteKey());
        authorization.put("companyName", systemConfig.getCompanyName());
        return Result.successResult(authorization);
    }

    @RequestMapping(value = "/getInstallationEnvironment", method = RequestMethod.GET)
    @ResponseBody
    public Result getInstallationEnvironment() {
        return Result.successResult(INSTALLATION_ENVIRONMENT);
    }

}
