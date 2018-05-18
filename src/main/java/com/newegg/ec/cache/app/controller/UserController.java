package com.newegg.ec.cache.app.controller;

import com.newegg.ec.cache.app.model.Common;
import com.newegg.ec.cache.core.userapi.UserAccess;
import com.newegg.ec.cache.app.logic.UserLogic;
import com.newegg.ec.cache.app.model.Cluster;
import com.newegg.ec.cache.app.model.Response;
import com.newegg.ec.cache.app.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
    private UserLogic logic;

    @RequestMapping("/login")
    public String login(Model model){
        return "login";
    }

    @RequestMapping(value = "/listUser", method = RequestMethod.GET)
    @ResponseBody
    public Response list(){
        List<User> userList = logic.getUserList();
        return Response.Result(0, userList);
    }

    @RequestMapping(value = "/getUser", method = RequestMethod.GET)
    @ResponseBody
    public Response getUser(@RequestParam int id){
        User user = logic.getUser( id );
        return Response.Result(0, user);
    }

    @RequestMapping(value = "/addUser", method = RequestMethod.POST)
    @ResponseBody
    public Response addUser(@RequestBody User user){
        boolean res = logic.addUser( user );
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/removeUser", method = RequestMethod.GET)
    @ResponseBody
    public Response removeUser(@RequestParam int id){
        boolean res = logic.removeUser( id );
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/listGroup", method = RequestMethod.GET)
    @ResponseBody
    public Response listGroup(@SessionAttribute(Common.SESSION_USER_KEY) User user){
        List<String> list = new ArrayList<>();
        list.add("admin");
        list.add("others");
        return Response.Result(0, list);
    }
}
