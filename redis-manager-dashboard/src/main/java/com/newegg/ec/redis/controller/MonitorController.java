package com.newegg.ec.redis.controller;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    @RequestMapping(value = "/getNodeInfoDataList", method = RequestMethod.POST)
    @ResponseBody
    public Result getNodeInfoList(@RequestBody NodeInfoParam nodeInfoParam) {
        List<NodeInfo> nodeInfoList = nodeInfoService.getNodeInfoList(nodeInfoParam);
        if (nodeInfoList == null) {
            return Result.failResult();
        }
        if(nodeInfoList.isEmpty()) {
            return Result.successResult(nodeInfoList);
        }
        Multimap<String, NodeInfo> nodeInfoListMap = ArrayListMultimap.create();
        nodeInfoList.forEach(nodeInfo -> nodeInfoListMap.put(nodeInfo.getNode(), nodeInfo));
        List<Collection<NodeInfo>> nodeInfoDataList = new ArrayList<>();
        nodeInfoListMap.keySet().forEach(key -> {
            Collection<NodeInfo> oneNodeInfoList = nodeInfoListMap.get(key);
            nodeInfoDataList.add(oneNodeInfoList);
        });
        return Result.successResult(nodeInfoDataList);
    }

    @RequestMapping(value = "/getSlowLogList", method = RequestMethod.POST)
    @ResponseBody
    public Result getSlowLogList(@RequestBody SlowLogParam slowLogParam) {
        Cluster cluster = clusterService.getClusterById(slowLogParam.getClusterId());
        if (cluster == null) {
            return Result.failResult();
        }
        List<RedisSlowLog> redisSlowLog = redisService.getRedisSlowLog(cluster, slowLogParam);
        return Result.successResult(redisSlowLog);
    }

}
