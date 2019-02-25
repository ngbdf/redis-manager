package com.newegg.ec.cache.controller;

import com.newegg.ec.cache.core.entity.annotation.userapi.UserAccess;
import com.newegg.ec.cache.core.entity.constants.Constants;
import com.newegg.ec.cache.core.entity.model.Response;
import com.newegg.ec.cache.core.entity.model.User;
import com.newegg.ec.cache.module.usermanager.UserService;
import com.newegg.ec.cache.util.RequestUtil;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gl49 on 2018/4/20.
 */
@Controller
@RequestMapping("/user")
@UserAccess
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @RequestMapping(value = "/listUser", method = RequestMethod.GET)
    @ResponseBody
    public Response list() {
        List<User> userList = userService.getUserList();
        return Response.Result(0, userList);
    }

    @RequestMapping(value = "/verifyLogin", method = RequestMethod.GET)
    @ResponseBody
    public Response verifyLogin(@RequestParam String username, @RequestParam String password) {
        User user = userService.getUser(username);
        if (null != user && user.getPassword().equals(password)) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            HttpSession session = request.getSession();
            session.setAttribute(Constants.SESSION_USER_KEY, user);
            return Response.Success();
        }
        return Response.Error("用户名或密码填写有误");
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @ResponseBody
    public Response logout() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        session.removeAttribute(Constants.SESSION_USER_KEY);
        return Response.Info("logout");
    }

    @RequestMapping(value = "/getUser", method = RequestMethod.GET)
    @ResponseBody
    public Response getUser(@RequestParam int id) {
        User user = userService.getUser(id);
        return Response.Result(0, user);
    }

    @RequestMapping(value = "/autoGetUser", method = RequestMethod.GET)
    @ResponseBody
    public Response autoGetUser() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(Constants.SESSION_USER_KEY);
        return Response.Result(0, user);
    }

    @RequestMapping(value = "/addUser", method = RequestMethod.POST)
    @ResponseBody
    public Response addUser(@RequestBody JSONObject user) {

       if("admin".equals(RequestUtil.getUser().getUsername())) {
           boolean res = userService.addUser(user);
           return Response.Result(0, res);
       }else {
           return Response.Warn("Illegal Operation");
       }

    }

    @RequestMapping(value = "/removeUser", method = RequestMethod.GET)
    @ResponseBody
    public Response removeUser(@RequestParam int id) {

        if("admin".equals(RequestUtil.getUser().getUsername())) {
            boolean res = userService.removeUser(id);
            return Response.Result(0, res);
        }else {
            return Response.Warn("Illegal Operation");
        }

    }

    @RequestMapping(value = "/listGroup", method = RequestMethod.GET)
    @ResponseBody
    public Response listGroup(@SessionAttribute(Constants.SESSION_USER_KEY) User user) {
        List<String> list = new ArrayList<>();
        if (user.getUserGroup().equals(Constants.ADMIN_GROUP)) {
            list = userService.getGroups();
        } else {
            list.add(user.getUserGroup());
        }
        return Response.Result(0, list);
    }

}
