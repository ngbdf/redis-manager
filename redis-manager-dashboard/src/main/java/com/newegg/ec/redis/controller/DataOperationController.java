package com.newegg.ec.redis.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.entity.*;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.util.SignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.newegg.ec.redis.util.RedisUtil.CLUSTER;

/**
 * @author Jay.H.Zou
 * @date 9/26/2019
 */
@RequestMapping("/data/*")
@Controller
public class DataOperationController {

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private IRedisService redisService;

    @RequestMapping(value = "/getDBList/{clusterId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getDBList(@PathVariable("clusterId") Integer clusterId) {
        if (clusterId == null) {
            return Result.failResult().setMessage("Request Param is empty.");
        }
        Cluster cluster = clusterService.getClusterById(clusterId);
        if (cluster == null) {
            return Result.failResult().setMessage("Get cluster failed.");
        }
        List<JSONObject> databaseList = new ArrayList<>();
        Map<String, Long> databaseMap = redisService.getDatabase(cluster);
        databaseMap.forEach((key, val) -> {
            JSONObject item = new JSONObject();
            item.put("database", key);
            item.put("keys", val);
            databaseList.add(item);
        });
        return Result.successResult(databaseList);
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    @ResponseBody
    public Result queryRedis(@RequestBody AutoCommandParam autoCommandParam) {
        Cluster cluster = clusterService.getClusterById(autoCommandParam.getClusterId());
        if (cluster == null) {
            return Result.failResult().setMessage("Get cluster failed.");
        }
        AutoCommandResult queryResult = redisService.query(cluster, autoCommandParam);
        return queryResult != null ? Result.successResult(queryResult) : Result.failResult().setMessage("Query redis failed.");
    }

    @RequestMapping(value = "/scan", method = RequestMethod.POST)
    @ResponseBody
    public Result scanRedis(@RequestBody AutoCommandParam autoCommandParam) {
        Cluster cluster = clusterService.getClusterById(autoCommandParam.getClusterId());
        if (cluster == null) {
            return Result.failResult().setMessage("Get cluster failed.");
        }
        Set<String> scanResult = redisService.scan(cluster, autoCommandParam);
        return scanResult != null ? Result.successResult(scanResult) : Result.failResult().setMessage("Scan redis failed.");
    }

    @RequestMapping(value = "/sendCommand", method = RequestMethod.POST)
    @ResponseBody
    public Result sendCommand(@RequestBody DataCommandsParam dataCommandsParam) {
        Cluster cluster = clusterService.getClusterById(dataCommandsParam.getClusterId());
        Object console = redisService.console(cluster, dataCommandsParam);
        // 格式处理
        if (console == null ) {
            return Result.failResult();
        }
        return Result.successResult(console);
    }

}
