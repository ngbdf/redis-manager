package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.entity.SentinelMaster;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.service.ISentinelMastersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.ws.rs.PathParam;
import java.util.List;

/**
 * @author Everly.J.Ju
 * @date 2020/2/12
 */
@RequestMapping("/sentinel/*")
@Controller
public class SentinelMasterController {

    private static final Logger logger = LoggerFactory.getLogger(SentinelMasterController.class);

    @Autowired
    private IRedisService redisService;

    @Autowired
    private ISentinelMastersService sentinelMastersService;

    @RequestMapping(value = "/getSentinelMasterList/{clusterId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getMasterInformation(@PathParam("clusterId") Integer clusterId) {
        List<SentinelMaster> sentinelMaster = sentinelMastersService.getSentinelMasterByClusterId(clusterId);
        return Result.successResult(sentinelMaster);
    }

    //新增，移除 sentinel master
    @RequestMapping(value = "/addSentinelMaster", method = RequestMethod.POST)
    @ResponseBody
    public Result add(@RequestBody SentinelMaster sentinelMaster) {
        boolean result = sentinelMastersService.addSentinelMaster(sentinelMaster);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/deleteSentinelMaster", method = RequestMethod.POST)
    @ResponseBody
    public Result delete(@RequestBody SentinelMaster sentinelMaster) {
        Integer sentinelMasterId = sentinelMaster.getSentinelMasterId();
        boolean result = sentinelMastersService.deleteSentinelMasterById(sentinelMasterId);
        return result ? Result.successResult() : Result.failResult();
    }
}
