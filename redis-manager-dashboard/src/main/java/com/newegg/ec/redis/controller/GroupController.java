package com.newegg.ec.redis.controller;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.entity.Group;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.entity.User;
import com.newegg.ec.redis.plugin.alert.service.impl.AlertRecordService;
import com.newegg.ec.redis.service.IGroupService;
import com.newegg.ec.redis.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 9/18/2019
 */
@RequestMapping("/group/*")
@Controller
public class GroupController {

    private static final Logger logger = LoggerFactory.getLogger(GroupController.class);

    @Autowired
    private IGroupService groupService;

    @Autowired
    private IUserService userService;

    @Autowired
    private AlertRecordService alertRecordService;

    private static final String USER_NUMBER = "userNumber";

    private static final String ALERT_NUMBER = "alertNumber";

    /**
     * Health cluster number and bad cluster number calculating from cluster list info
     *
     * @param groupId
     * @return
     */
    @RequestMapping(value = "/overview/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getGroupPartOverview(@PathVariable("groupId") Integer groupId) {
        JSONObject overview = new JSONObject();
        overview.put(USER_NUMBER, userService.getUserNumber(groupId));
        overview.put(ALERT_NUMBER, alertRecordService.getAlertRecordNumber(groupId));
        return Result.successResult(overview);
    }

    @RequestMapping(value = "/info/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getGroup(@PathVariable("groupId") Integer groupId) {
        Group group = groupService.getGroupById(groupId);
        return group != null ? Result.successResult(group) : Result.failResult();
    }

    @RequestMapping(value = "/getGroupList", method = RequestMethod.POST)
    @ResponseBody
    public Result getGroupList(@RequestBody User user) {
        /*Integer userId = user.getUserId();
        if (user.getUserRole() == 0) {
            userId = null;
        }*/
        List<Group> groupList = groupService.getGroupByUserId(user.getUserId());
        if (groupList == null || groupList.isEmpty()) {
            return Result.failResult();
        }
        return Result.successResult(groupList);
    }

    @RequestMapping(value = "/addGroup", method = RequestMethod.POST)
    @ResponseBody
    public Result addGroup(@RequestBody Group group) {
        try {
            groupService.addGroup(group);
            return Result.successResult();
        } catch (Exception e) {
            logger.error("Add group failed.", e);
            return Result.failResult();
        }
    }

    @RequestMapping(value = "/updateGroup", method = RequestMethod.POST)
    @ResponseBody
    public Result updateGroup(@RequestBody Group group) {
        boolean result = groupService.updateGroup(group);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/validateGroupName/{groupName}", method = RequestMethod.GET)
    @ResponseBody
    public Result validateGroupName(@PathVariable("groupName") String groupName) {
        Group group = groupService.getGroupByName(groupName);
        return group == null ? Result.successResult() : Result.failResult(group);
    }
}
