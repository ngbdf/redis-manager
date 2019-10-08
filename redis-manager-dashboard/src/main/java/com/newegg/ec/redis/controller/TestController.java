package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.controller.websocket.InstallationWebSocketHandler;
import com.newegg.ec.redis.entity.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Jay.H.Zou
 * @date 10/8/2019
 */
@Controller
public class TestController {

    @RequestMapping(value = "/sendMessage", method = RequestMethod.GET)
    @ResponseBody
    public Result sendMessage() {
        String clusterName = "Shanghai";
        InstallationWebSocketHandler.appendLog(clusterName, "From server...");
        return Result.successResult();
    }

}
