package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.config.SystemConfig;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.util.LinuxInfoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.SocketException;

/**
 * @author Jay.H.Zou
 * @date 10/29/2019
 */
@RequestMapping("/system/*")
@Controller
public class SystemSwitchController {

    @Autowired
    private SystemConfig systemConfig;

    @RequestMapping(value = "/humpbackEnabled", method = RequestMethod.GET)
    @ResponseBody
    public Result getHumpbackEnabled() {
        return Result.successResult(systemConfig.getHumpbackEnabled());
    }

    @RequestMapping(value = "/getServerAddress", method = RequestMethod.GET)
    @ResponseBody
    public Result getServerAddress() throws SocketException {
        return Result.successResult(LinuxInfoUtil.getIpAddress() + ":" + systemConfig.getServerPort());
    }

}
