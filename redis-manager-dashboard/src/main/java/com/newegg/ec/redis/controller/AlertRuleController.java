package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.plugin.alert.entity.AlertRule;
import com.newegg.ec.redis.plugin.alert.service.IAlertRuleService;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.util.SignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/10/7
 */
@RequestMapping("/alert/rule/*")
@Controller
public class AlertRuleController {

    @Autowired
    private IAlertRuleService alertRuleService;

    @Autowired
    private IClusterService clusterService;

    @RequestMapping(value = "/getAlertRuleListByGroupId/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAlertRuleListByGroupId(@PathVariable("groupId") Integer groupId) {
        List<AlertRule> alertRuleList = alertRuleService.getAlertRuleListByGroupId(groupId);
        return alertRuleList != null ? Result.successResult(alertRuleList) : Result.failResult();
    }

    @RequestMapping(value = "/getAlertRuleListByClusterId/{clusterId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAlertRuleListByClusterId(@PathVariable("clusterId") Integer clusterId) {
        Cluster cluster = clusterService.getClusterById(clusterId);
        String ruleIds = cluster.getRuleIds();
        String[] ruleIdArr = SignUtil.splitByCommas(ruleIds);

        List<Integer> ruleIdList = new ArrayList<>();
        for (String ruleId : ruleIdArr) {
            ruleIdList.add(Integer.parseInt(ruleId));
        }
        List<AlertRule> alertRuleList = alertRuleService.getAlertRuleByIds(ruleIdList);
        return alertRuleList != null ? Result.successResult(alertRuleList) : Result.failResult();
    }

    @RequestMapping(value = "/getAlertRule/{ruleId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAlertRule(@PathVariable("ruleId") Integer ruleId) {
        AlertRule alertRule = alertRuleService.getAlertRuleById(ruleId);
        return alertRule != null ? Result.successResult(alertRule) : Result.failResult();
    }

    @RequestMapping(value = "/addAlertRule", method = RequestMethod.POST)
    @ResponseBody
    public Result addAlertRule(@RequestBody AlertRule alertRule) {
        boolean result = alertRuleService.addAlertRule(alertRule);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/updateAlertRule", method = RequestMethod.POST)
    @ResponseBody
    public Result updateAlertRule(@RequestBody AlertRule alertRule) {
        boolean result = alertRuleService.updateAlertRule(alertRule);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/deleteAlertRule", method = RequestMethod.POST)
    @ResponseBody
    public Result deleteAlertRule(@RequestBody AlertRule alertRule) {
        boolean result = alertRuleService.deleteAlertRuleById(alertRule.getRuleId());
        return result ? Result.successResult() : Result.failResult();
    }

}
