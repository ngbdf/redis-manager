package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.*;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.INodeInfoService;
import com.newegg.ec.redis.service.IRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 7/20/2019
 */
@RequestMapping("/monitor/*")
@Controller
public class MonitorController {

    @Autowired
    private INodeInfoService nodeInfoService;

    @Autowired
    private IRedisService redisService;

    @Autowired
    private IClusterService clusterService;

    @RequestMapping(value = "/getNodeInfoList", method = RequestMethod.POST)
    @ResponseBody
    public Result getNodeInfoList(@RequestBody NodeInfoParam nodeInfoParam) {
        // System.err.println(nodeInfoParam);
        List<NodeInfo> nodeInfoList = nodeInfoService.getNodeInfoList(nodeInfoParam);
        return nodeInfoList != null ? Result.successResult(nodeInfoList) : Result.failResult();
    }

    @RequestMapping(value = "/getSlowLogList", method = RequestMethod.POST)
    @ResponseBody
    public Result getSlowLogList(@RequestBody SlowLogParam slowLogParam){
        Cluster cluster = clusterService.getClusterById(slowLogParam.getClusterId());
        if (cluster == null) {
            return Result.failResult().setMessage("Cluster not exist.");
        }
        List<RedisSlowLog> redisSlowLog = redisService.getRedisSlowLog(cluster, slowLogParam);
        return Result.successResult(redisSlowLog);
    }

}
