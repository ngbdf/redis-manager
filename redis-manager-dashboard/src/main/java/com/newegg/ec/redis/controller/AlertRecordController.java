package com.newegg.ec.redis.controller;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.aop.annotation.OperationLog;
import com.newegg.ec.redis.entity.OperationObjectType;
import com.newegg.ec.redis.entity.OperationType;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;
import com.newegg.ec.redis.plugin.alert.service.IAlertRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jay.H.Zou
 * @date 2019/10/8
 */
@RequestMapping("/alert/record/*")
@Controller
public class AlertRecordController {

    @Autowired
    private IAlertRecordService alertRecordService;

    @RequestMapping(value = "/getAlertRecord/cluster/{clusterId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAlertRecordList(@PathVariable("clusterId") Integer clusterId,@RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "20") Integer pageSize) {
        Map<String, Object> returnMap = alertRecordService.getAlertRecordByClusterId(clusterId, pageNo, pageSize);
        return returnMap !=null ? Result.successResult(returnMap) : Result.failResult();
    }

    @RequestMapping(value = "/deleteAlertRecordBatch", method = RequestMethod.POST)
    @ResponseBody
    @OperationLog(type = OperationType.DELETE, objType = OperationObjectType.ALERT_RECORD)
    @SuppressWarnings("unchecked")
    public Result deleteAlertRecordBatch(@RequestBody JSONObject jsonObject) {
        List<Integer> recordIdList = (List<Integer>)jsonObject.get("recordsIds");
        boolean result = alertRecordService.deleteAlertRecordByIds(recordIdList);
        return result ? Result.successResult() : Result.failResult();
    }

}
