package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.service.IOperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author fw13
 * @date 2019/11/20 15:48
 */
@RequestMapping("/operation/*")
@Controller
public class OperationLogController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IOperationLogService operationLogService;

    @RequestMapping(value = "group/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAllNodeList(@PathVariable("groupId") Integer groupId, @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> returnMap = operationLogService.getLogsByGroupId(groupId, pageNo, pageSize);
        return returnMap !=null ? Result.successResult(returnMap) : Result.failResult();
    }
}
