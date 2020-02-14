package com.newegg.ec.redis.controller;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.entity.SentinelMaster;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.service.ISentinelMastersService;
import com.newegg.ec.redis.util.RedisNodeInfoUtil;
import com.newegg.ec.redis.util.SignUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private IClusterService clusterService;

    @RequestMapping(value = "/getSentinelMasterList/{clusterId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getMasterInformation(@PathVariable("clusterId") Integer clusterId) {
        List<SentinelMaster> sentinelMaster = sentinelMastersService.getSentinelMasterByClusterId(clusterId);
        return Result.successResult(sentinelMaster);
    }

    @RequestMapping(value = "/getSentinelMasterByName", method = RequestMethod.POST)
    @ResponseBody
    public Result getSentinelMasterById(@RequestBody SentinelMaster sentinelMaster) {
        SentinelMaster sentinelMasterByName = sentinelMastersService.getSentinelMasterByMasterName(sentinelMaster.getClusterId(), sentinelMaster.getName());
        return sentinelMasterByName != null ? Result.successResult(sentinelMaster) : Result.failResult();
    }

    @RequestMapping(value = "/getSentinelMasterInfo", method = RequestMethod.POST)
    @ResponseBody
    public Result getSentinelMasterInfo(@RequestBody SentinelMaster sentinelMaster) {
        Map<String, String> masterMap = redisService.getSentinelMasterInfoByName(sentinelMaster);
        if (masterMap == null) {
            return Result.failResult();
        }
        List<JSONObject> infoList = new ArrayList<>();
        masterMap.forEach((key, value) -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", key);
            jsonObject.put("value", value);
            infoList.add(jsonObject);
        });
        return Result.successResult(infoList);
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
    public Result deleteSentinelMaster(@RequestBody SentinelMaster sentinelMaster) {
        Integer sentinelMasterId = sentinelMaster.getSentinelMasterId();
        boolean result = sentinelMastersService.deleteSentinelMasterById(sentinelMasterId);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/monitorMaster", method = RequestMethod.POST)
    @ResponseBody
    public Result monitorMaster(@RequestBody SentinelMaster sentinelMaster) {
        boolean result = redisService.monitorMaster(sentinelMaster);
        if (result) {
            boolean saveResult = sentinelMastersService.addSentinelMaster(sentinelMaster);
            return saveResult ? Result.successResult() : Result.failResult();
        }
        return Result.failResult();
    }

    @RequestMapping(value = "/updateSentinelMaster", method = RequestMethod.POST)
    @ResponseBody
    public Result updateSentinelMaster(@RequestBody SentinelMaster sentinelMaster) {
        boolean result = redisService.sentinelSet(sentinelMaster);
        if (result) {
            boolean saveResult = sentinelMastersService.updateSentinelMaster(sentinelMaster);
            return saveResult ? Result.successResult() : Result.failResult();
        }
        return Result.failResult();
    }

    @RequestMapping(value = "/failoverMaster", method = RequestMethod.POST)
    @ResponseBody
    public Result failoverMaster(@RequestBody SentinelMaster sentinelMaster) {
        boolean result = redisService.failoverMaster(sentinelMaster);
        if (result) {
            Map<String, String> sentinelMasterInfoByName = redisService.getSentinelMasterInfoByName(sentinelMaster);
            String ip = sentinelMasterInfoByName.get("ip");
            Integer port = Integer.parseInt(sentinelMasterInfoByName.get("port"));
            sentinelMaster.setHost(ip);
            sentinelMaster.setPort(port);
            sentinelMaster.setLastMasterNode(ip + SignUtil.COLON + port);
            boolean saveResult = sentinelMastersService.updateSentinelMaster(sentinelMaster);
            return saveResult ? Result.successResult() : Result.failResult();
        }
        return Result.failResult();
    }

    @RequestMapping(value = "/getMasterSlaves", method = RequestMethod.POST)
    @ResponseBody
    public Result getMasterSlaves(@RequestBody SentinelMaster sentinelMaster) {
        List<Map<String, String>> slaves = redisService.sentinelSlaves(sentinelMaster);
        return slaves != null ? Result.successResult(slaves) : Result.failResult();
    }

}
