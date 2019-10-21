package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.entity.User;
import com.newegg.ec.redis.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public Result login(@RequestBody User user) {
        User userLogin = userService.getUserByNameAndPassword(user);
        if (userLogin == null) {
            return Result.failResult();
        }
        return Result.successResult(userLogin);
    }

    @RequestMapping(value = "/oauth2", method = RequestMethod.POST)
    @ResponseBody
    public Result oauth2(@RequestBody User user) {
        User userLogin = userService.getUserByNameAndPassword(user);
        if (userLogin == null) {
            return Result.failResult();
        }
        return Result.successResult(userLogin);
    }

    @RequestMapping(value = "/getUser/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getUser(@PathVariable("userId") Integer userId) {
        User user = userService.getUserById(userId);
        return user != null ? Result.successResult(user) : Result.failResult();
    }

    @RequestMapping(value = "/getUserRole", method = RequestMethod.POST)
    @ResponseBody
    public Result getUserRole(@RequestBody User user) {
        User exist = userService.getUserRole(user.getGroupId(), user.getUserId());
        if (exist == null) {
            return Result.successResult(User.UserRole.MEMBER);
        }
        return Result.successResult(user.getUserRole());
    }

    @RequestMapping(value = "/getUserList/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getUserRole(@PathVariable Integer groupId) {
        List<User> userList = userService.getUserByGroupId(groupId);
        return userList != null ? Result.successResult(userList) : Result.failResult();
    }

    @RequestMapping(value = "/addUser", method = RequestMethod.POST)
    @ResponseBody
    public Result addUser(@RequestBody User user) {
        boolean result = userService.addUser(user);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/updateUser", method = RequestMethod.POST)
    @ResponseBody
    public Result updateUser(@RequestBody User user) {
        boolean result = userService.updateUser(user);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/deleteUser", method = RequestMethod.POST)
    @ResponseBody
    public Result deleteUser(@RequestBody User user) {
        boolean result = userService.deleteUserById(user.getUserId());
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/validateUserName/{userName}", method = RequestMethod.GET)
    @ResponseBody
    public Result validateUserName(@PathVariable String userName) {
        User user = userService.getUserByName(userName);
        return user == null ? Result.successResult() : Result.failResult();
    }
}
