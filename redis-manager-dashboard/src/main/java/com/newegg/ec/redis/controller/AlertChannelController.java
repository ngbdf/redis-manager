package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.plugin.alert.service.IAlertChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Jay.H.Zou
 * @date 2019/10/4
 */
@RequestMapping("/alert/channel/*")
@Controller
public class AlertChannel {

    @Autowired
    private IAlertChannelService alertChannelService;

    @RequestMapping(value = "/addAlertChannel", method = RequestMethod.POST)
    @ResponseBody
    public Result addAlertChannel(@RequestBody AlertChannel alertChannel) {
        
        return Result.successResult();
    }

}
