package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.entity.SentinelMaster;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.service.ISentinelMastersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

    @RequestMapping(value = "/getSentinelMasterList/{clusterId}", method = RequestMethod.POST)
    @ResponseBody
    public Result getMasterInformation(@PathParam("clusterId") Integer clusterId) {
        List<SentinelMaster> sentinelMaster = sentinelMastersService.getSentinelMasterByClusterId(clusterId);
        return Result.successResult(sentinelMaster);
    }

    @RequestMapping(value = "/getSentinelMasterDetail/{sentinelMasterId}",method = RequestMethod.GET)
    @ResponseBody
    public Result getSentinelDetail(@PathParam("sentinelMasterId") Integer sentinelMasterId){

        return Result.successResult();
    }

}
