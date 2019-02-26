package com.newegg.ec.cache.controller;

import com.newegg.ec.cache.core.entity.annotation.userapi.UserAccess;
import com.newegg.ec.cache.core.entity.model.ClusterCheckLog;
import com.newegg.ec.cache.core.entity.model.ClusterCheckRule;
import com.newegg.ec.cache.core.entity.model.Response;
import com.newegg.ec.cache.module.clusteralarm.IAlarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by tc72 on 2018/5/3.
 */
@Controller
@RequestMapping("/alarm")
@UserAccess
public class AlarmController {

    @Autowired
    private IAlarmService alarmService;

    @RequestMapping("/alarmSystem")
    public String ruleList(Model model) {
        return "alarmSystem";
    }

    @RequestMapping(value = "/getRuleList", method = RequestMethod.GET)
    @ResponseBody
    public Response getRuleList(@RequestParam String clusterId) {
        List<ClusterCheckRule> res = alarmService.getRuleList(clusterId);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/addRule", method = RequestMethod.POST)
    @ResponseBody
    public Response addRule(@RequestBody ClusterCheckRule rule) {
        Boolean res = alarmService.addRule(rule);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/checkAlarmRule", method = RequestMethod.POST)
    @ResponseBody
    public Response checkAlarmRule(@RequestBody ClusterCheckRule rule) {
        Boolean check = alarmService.checkRule(rule);
        if (check) {
            return Response.Info("success!");
        } else {
            return Response.Warn("you are fail");
        }
    }

    @RequestMapping(value = "/deleteRule", method = RequestMethod.GET)
    @ResponseBody
    public Response deleteRule(@RequestParam String ruleId) {
        Boolean boo = alarmService.deleteRule(ruleId);
        if (boo) {
            return Response.Info("success!");
        } else {
            return Response.Warn("you are fail");
        }
    }

    @RequestMapping(value = "/getCaseLogs", method = RequestMethod.GET)
    @ResponseBody
    public Response getCaseList(@RequestParam String clusterId) {
        List<ClusterCheckLog> res = alarmService.getCaseList(clusterId);
        return Response.Result(0, res);
    }

    @RequestMapping(value = "/deleteCaseLog", method = RequestMethod.GET)
    @ResponseBody
    public Response deleteCaseLog(@RequestParam String logId) {
        alarmService.deleteCaseLog(logId);
        return Response.Info("success!");
    }

    @RequestMapping(value = "/deleteAllLog", method = RequestMethod.GET)
    @ResponseBody
    public Response deleteAllLog(@RequestParam String cluster) {
        alarmService.deleteAllLog(cluster);
        return Response.Info("success!");

    }

    @RequestMapping(value = "/countTotal", method = RequestMethod.POST)
    @ResponseBody
    public Response countTotalAlarm(@RequestBody List<String> clusterIds) {

        Integer count = alarmService.countTotalLog(clusterIds);
        return Response.Result(0, count);
    }

    @RequestMapping(value = "/countWarningLogByClusterId", method = RequestMethod.POST)
    @ResponseBody
    public Response countWarningLogByClusterId(@RequestBody Integer clusterId) {
        Integer count = alarmService.countWarningLogByClusterId(clusterId);
        return Response.Result(0, count);
    }

}
