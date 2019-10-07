package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import com.newegg.ec.redis.plugin.alert.service.IAlertChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/10/4
 */
@RequestMapping("/alert/channel/*")
@Controller
public class AlertChannelController {

    @Autowired
    private IAlertChannelService alertChannelService;

    @RequestMapping(value = "/getAlertChannelList/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAlertChannelList(@PathVariable("groupId") Integer groupId) {
        List<AlertChannel> alertChannelList = alertChannelService.getAlertChannelByGroupId(groupId);
        return alertChannelList != null ? Result.successResult(alertChannelList) : Result.failResult();
    }

    @RequestMapping(value = "/getAlertChannel/{channelId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAlertChannel(@PathVariable("channelId") Integer channelId) {
        AlertChannel alertChannel = alertChannelService.getAlertChannelById(channelId);
        return alertChannel != null ? Result.successResult(alertChannel) : Result.failResult();
    }

    @RequestMapping(value = "/addAlertChannel", method = RequestMethod.POST)
    @ResponseBody
    public Result addAlertChannel(@RequestBody AlertChannel alertChannel) {
        boolean result = alertChannelService.addAlertChannel(alertChannel);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/updateAlertChannel", method = RequestMethod.POST)
    @ResponseBody
    public Result updateAlertChannel(@RequestBody AlertChannel alertChannel) {
        boolean result = alertChannelService.updateAlertChannel(alertChannel);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/deleteAlertChannel", method = RequestMethod.POST)
    @ResponseBody
    public Result deleteAlertChannel(@RequestBody AlertChannel alertChannel) {
        boolean result = alertChannelService.deleteAlertChannelById(alertChannel.getChannelId());
        return result ? Result.successResult() : Result.failResult();
    }

}
