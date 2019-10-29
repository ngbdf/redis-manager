package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.config.SystemConfig;
import com.newegg.ec.redis.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Jay.H.Zou
 * @date 10/29/2019
 */
@RequestMapping("/switch/*")
@Controller
public class SystemSwitchController {

    @Autowired
    private SystemConfig systemConfig;

    @RequestMapping(value = "/humpbackEnabled", method = RequestMethod.GET)
    @ResponseBody
    public Result getHumpbackEnabled() {
        return Result.successResult(systemConfig.getHumpbackEnabled());
    }

}
