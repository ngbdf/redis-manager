package com.newegg.ec.redis.controller;

import com.google.common.base.Strings;
import com.newegg.ec.redis.aop.annotation.OperationLog;
import com.newegg.ec.redis.config.SystemConfig;
import com.newegg.ec.redis.controller.oauth.AuthService;
import com.newegg.ec.redis.entity.OperationObjectType;
import com.newegg.ec.redis.entity.OperationType;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.entity.User;
import com.newegg.ec.redis.service.IUserService;
import com.newegg.ec.redis.util.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

import static com.newegg.ec.redis.config.SystemConfig.AVATAR_PATH;

/**
 * @author Jay.H.Zou
 * @date 7/20/2019
 */
@RequestMapping("/user/*")
@Controller
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    private AuthService authService;

    private static final String USER_ID = "userId";

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public Result login(@RequestBody User user, HttpServletRequest request) {
        User userLogin = userService.getUserByNameAndPassword(user);
        if (userLogin == null) {
            return Result.failResult();
        }
        HttpSession session = request.getSession();
        session.setAttribute("user", userLogin);
        session.setMaxInactiveInterval(60 * 60);
        return Result.successResult(userLogin);
    }

    @RequestMapping(value = "/signOut", method = RequestMethod.POST)
    @ResponseBody
    public Result signOut(HttpServletRequest request) {
        request.getSession().setAttribute("user", null);
        if (systemConfig.getAuthorizationEnabled()) {
           authService.signOut();
        }
        return Result.successResult();
    }

    @RequestMapping(value = "/oauth2Login", method = RequestMethod.GET)
    @ResponseBody
    public Result oauth2Login(HttpServletRequest request) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        return user != null ? Result.successResult(user) : Result.failResult();
    }

    @RequestMapping(value = "/getUserFromSession", method = RequestMethod.GET)
    @ResponseBody
    public Result getUserFromSession(HttpSession session) {
        User userLogin = (User) session.getAttribute("user");
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
        return Result.successResult(exist.getUserRole());
    }

    @RequestMapping(value = "/getUserList/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getUserRole(@PathVariable Integer groupId) {
        List<User> userList = userService.getUserByGroupId(groupId);
        return userList != null ? Result.successResult(userList) : Result.failResult();
    }

    @RequestMapping(value = "/getGrantedUserList/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getGrantUserList(@PathVariable Integer groupId) {
        List<User> userList = userService.getGrantUserByGroupId(groupId);
        return userList != null ? Result.successResult(userList) : Result.failResult();
    }

    @RequestMapping(value = "/addUser", method = RequestMethod.POST)
    @ResponseBody
    @OperationLog(type = OperationType.ADD,objType = OperationObjectType.USER)
    public Result addUser(@RequestBody User user) {
        boolean result = userService.addUser(user);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/isGranted/{grantGroupId}/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public Result isGranted(@PathVariable("grantGroupId") Integer grantGroupId, @PathVariable("userId") Integer userId) {
        if (grantGroupId == null || userId == null) {
            return Result.failResult();
        }
        boolean result = userService.isGranted(grantGroupId, userId);
        return result ? Result.failResult() : Result.successResult();
    }

    @RequestMapping(value = "/grantUser", method = RequestMethod.POST)
    @ResponseBody
    @OperationLog(type = OperationType.GRANT,objType = OperationObjectType.USER)
    public Result grantUser(@RequestBody User user) {
        boolean result = userService.grantUser(user);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/updateUser", method = RequestMethod.POST)
    @ResponseBody
    @OperationLog(type = OperationType.UPDATE,objType = OperationObjectType.USER)
    public Result updateUser(@RequestBody User user) {
        boolean result = userService.updateUser(user);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/deleteUser", method = RequestMethod.POST)
    @ResponseBody
    @OperationLog(type = OperationType.DELETE,objType = OperationObjectType.USER)
    public Result deleteUser(@RequestBody User user) {
        boolean result = userService.deleteUserById(user.getUserId());
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/revokeUser", method = RequestMethod.POST)
    @ResponseBody
    @OperationLog(type = OperationType.REVOKE,objType = OperationObjectType.USER)
    public Result revokeUser(@RequestBody User user) {
        boolean result = userService.revokeUser(user);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/validateUserName/{userName}", method = RequestMethod.GET)
    @ResponseBody
    public Result validateUserName(@PathVariable String userName) {
        User user = userService.getUserByName(userName);
        return user == null ? Result.successResult() : Result.failResult(user);
    }

    @RequestMapping(value = "/saveAvatar", method = RequestMethod.POST)
    @ResponseBody
    public Result updateAvatar(@RequestParam("avatarFile") MultipartFile avatarFile, @RequestParam Map<String, Object> user) {
        if (user == null || user.get(USER_ID) == null) {
            return Result.failResult().setMessage("user id is empty");
        }
        String userIdStr = user.get(USER_ID).toString();

        if (Strings.isNullOrEmpty(userIdStr)) {
            return Result.failResult();
        }
        Integer userId = Integer.parseInt(userIdStr);
        try {
            ImageUtil.saveImage(avatarFile, systemConfig.getAvatarPath(), userId);
            String avatar = AVATAR_PATH + ImageUtil.getImageName(userId);
            User userAvatar = new User();
            userAvatar.setUserId(userId);
            userAvatar.setAvatar(avatar);
            return userService.updateUserAvatar(userAvatar) ? Result.successResult() : Result.failResult();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failResult();
        }
    }
}
