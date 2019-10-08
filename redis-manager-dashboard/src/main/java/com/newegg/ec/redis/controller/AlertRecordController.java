package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;
import com.newegg.ec.redis.plugin.alert.service.IAlertRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/10/8
 */
@RequestMapping("/alert/record/*")
@Controller
public class AlertRecordController {

    @Autowired
    private IAlertRecordService alertRecordService;

    @RequestMapping(value = "/getAlertRecordList/{clusterId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAlertRecordList(@PathVariable("clusterId") Integer clusterId) {
        List<AlertRecord> alertRecordList = alertRecordService.getAlertRecordByClusterId(clusterId);
        return alertRecordList != null ? Result.successResult(alertRecordList) : Result.failResult();
    }

    @RequestMapping(value = "/deleteAlertRecord", method = RequestMethod.POST)
    @ResponseBody
    public Result deleteAlertRecord(@RequestBody AlertRecord alertRecord) {
        boolean result = alertRecordService.deleteAlertRecordById(alertRecord.getRecordId());
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/deleteAlertRecordBatch", method = RequestMethod.POST)
    @ResponseBody
    public Result deleteAlertRecordBatch(@RequestBody List<AlertRecord> alertRecordList) {
        List<Integer> alertRecordIdList = new ArrayList<>();
        alertRecordList.forEach(alertRecord -> {
            alertRecordIdList.add(alertRecord.getRecordId());
        });
        boolean result = alertRecordService.deleteAlertRecordByIds(alertRecordIdList);
        return result ? Result.successResult() : Result.failResult();
    }
}
