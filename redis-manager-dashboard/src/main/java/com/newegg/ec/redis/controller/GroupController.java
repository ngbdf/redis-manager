package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.Group;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.plugin.alert.service.impl.AlertRecordService;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.IGroupService;
import com.newegg.ec.redis.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.ws.rs.PathParam;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jay.H.Zou
 * @date 9/18/2019
 */
@RequestMapping("/group/*")
@Controller
public class GroupController {

    @Autowired
    private IGroupService groupService;

    @Autowired
    private IUserService userService;

    @Autowired
    private AlertRecordService alertRecordService;

    private static final String USER_NUMBER = "user";

    private static final String ALERT_NUMBER = "alert";

    /**
     * Health cluster number and bad cluster number calculating from cluster list info
     *
     * @param groupId
     * @return
     */
    @RequestMapping(value = "/overview/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getGroupPartOverview(@PathParam("groupId") Integer groupId) {
        Map<String, Integer> overviewMap = new HashMap<>();
        overviewMap.put(USER_NUMBER, userService.getUserNumber(groupId));
        overviewMap.put(ALERT_NUMBER, alertRecordService.getAlertNumber());
        return Result.successResult(overviewMap);
    }

    @RequestMapping(value = "/info/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getGroup(@PathParam("groupId") Integer groupId) {
        Group group = groupService.getGroupById(groupId);
        return group != null ? Result.successResult(group) : Result.failResult();
    }

    @RequestMapping(value = "/getGroupList/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getGroupList(@PathParam("userId") Integer userId) {
        List<Group> groupList = groupService.getGroupByUserId(userId);
        if (groupList == null || groupList.isEmpty()) {
            return Result.failResult();
        }
        return Result.successResult(groupList);
    }

}
