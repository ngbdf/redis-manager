package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.entity.User;
import com.newegg.ec.redis.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Jay.H.Zou
 * @date 7/20/2019
 */
@RequestMapping("/user/*")
@Controller
public class UserController {

    @Autowired
    private IUserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public Result login(User user) {
        User userLogin = userService.getUserByNameAndPassword(user);
        if (userLogin == null) {
            return Result.failResult();
        }
        return Result.successResult(userLogin);
    }

    @RequestMapping(value = "/getUserRole", method = RequestMethod.POST)
    @ResponseBody
    public Result getUserRole(Integer groupId, Integer userId) {
        User user = userService.getUserRole(groupId, userId);
        if (user == null) {
            return Result.successResult(User.UserRole.MEMBER);
        }
        return Result.successResult(user.getUserRole());
    }

}
