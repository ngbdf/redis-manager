package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.plugin.alert.service.impl.AlertRecordService;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jay.H.Zou
 * @date 9/18/2019
 */
@RequestMapping("/group/*")
@Controller
public class GroupController {

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private IUserService userService;

    @Autowired
    private AlertRecordService alertRecordService;

    private static final String USER_NUMBER = "user";

    private static final String HEALTH_NUMBER = "health";

    private static final String BAD_NUMBER = "bad";

    private static final String ALERT_NUMBER = "alert";

    @RequestMapping(value = "/overview", method = RequestMethod.GET)
    @ResponseBody
    public Result getGroupOverview() {
        Map<String, Integer> overviewMap = new HashMap<>();
        overviewMap.put(USER_NUMBER, userService.getUserNumber());
        overviewMap.put(HEALTH_NUMBER, clusterService.getHealthNumber());
        overviewMap.put(BAD_NUMBER, clusterService.getBadNumber());
        overviewMap.put(ALERT_NUMBER, alertRecordService.getAlertNumber());
        return Result.successResult(overviewMap);
    }

}
