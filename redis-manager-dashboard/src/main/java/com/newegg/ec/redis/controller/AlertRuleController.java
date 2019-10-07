package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.plugin.alert.entity.AlertRule;
import com.newegg.ec.redis.plugin.alert.service.IAlertRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    @RequestMapping(value = "/getAlertRuleList/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAlertRuleList(@PathVariable("groupId") Integer groupId) {
        List<AlertRule> alertRuleList = alertRuleService.getAlertRuleListByGroupId(groupId);
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
