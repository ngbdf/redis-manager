package com.newegg.ec.cache.app.controller.check;

import com.newegg.ec.cache.app.model.Response;
import com.newegg.ec.cache.app.util.MathExpressionCalculateUtil;
import com.newegg.ec.cache.core.userapi.UserAccess;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Created by gl49 on 2018/4/22.
 */
@Controller
@RequestMapping("/check")
@UserAccess(autoCreate = true)
public class CheckController {

    private static final Log logger = LogFactory.getLog(CheckController.class);

    @Resource
    private CheckLogic logic;

    @RequestMapping("/checkVersion")
    @ResponseBody
    public Response checkRedisVersion(@RequestParam int clusterId, @RequestParam String address) {
        int version = logic.checkRedisVersion(clusterId, address);
        return Response.Result(0, version);
    }

    @RequestMapping("/checkPortPass")
    @ResponseBody
    public Response checkPortPass(@RequestParam String ip, @RequestParam int port) {
        return logic.checkPortPass(ip, port, true);
    }

    @RequestMapping("/checkPortNotPass")
    @ResponseBody
    public Response checkPortNotPass(@RequestParam String ip, @RequestParam int port) {
        return logic.checkPortPass(ip, port, false);
    }

    @RequestMapping("/checkIp")
    @ResponseBody
    public Response checkIp(@RequestParam String ip) {
        return logic.checkIp(ip);
    }

    @RequestMapping("/checkAddress")
    @ResponseBody
    public Response checkAddress(@RequestParam String address) {
        return logic.checkAddress(address);
    }

    @RequestMapping("/checkUserPermisson")
    @ResponseBody
    public Response checkUserPermisson(@RequestParam String ip, @RequestParam String userName, @RequestParam String password) {
        return logic.checkUserPermisson(ip, userName, password);
    }

    @RequestMapping(value = "/checkClusterName", method = RequestMethod.GET)
    @ResponseBody
    public Response checkClusterName(@RequestParam String clusterId) {
        return logic.checkClusterNameByUserid(clusterId);
    }

    @RequestMapping(value = "/checkRule", method = RequestMethod.POST)
    @ResponseBody
    public Response checkRule(@RequestBody String req) {
        JSONObject object = JSONObject.fromObject(req);
        String formula = object.getString("formula");
        Boolean check = MathExpressionCalculateUtil.checkRule(formula);
        return check ? Response.Success() : Response.Error("fail");
    }

    @RequestMapping(value = "/checkBatchHumpbackContainerName", method = RequestMethod.POST)
    @ResponseBody
    public Response checkBatchHumpbackContainerName(@RequestBody String req) {
        JSONObject jsonObject = JSONObject.fromObject(req);
        return Response.Success();
    }

    @RequestMapping(value = "/checkBatchHostNotPass", method = RequestMethod.POST)
    @ResponseBody
    public Response checkBatchHostNotPass(@RequestBody String req) {
        JSONObject jsonObject = JSONObject.fromObject(req);
        return logic.checkBatchHostNotPass(jsonObject);
    }

    @RequestMapping(value = "/checkBatchDirPermission", method = RequestMethod.POST)
    @ResponseBody
    public Response checkBatchDirPermission(@RequestBody String req) {
        JSONObject jsonObject = JSONObject.fromObject(req);
        return logic.checkBatchDirPermission(jsonObject);
    }

    @RequestMapping(value = "/checkBatchWgetPermission", method = RequestMethod.POST)
    @ResponseBody
    public Response checkBatchWgetPermission(@RequestBody String req) {
        JSONObject jsonObject = JSONObject.fromObject(req);
        return logic.checkBatchWgetPermission(jsonObject);
    }

    @RequestMapping(value = "/checkBatchUserPermisson", method = RequestMethod.POST)
    @ResponseBody
    public Response checkBatchUserPermisson(@RequestBody String req) {
        JSONObject jsonObject = JSONObject.fromObject(req);
        logger.info("Request ==> " + req);
        return logic.checkBatchUserPermisson(jsonObject);
    }


}
