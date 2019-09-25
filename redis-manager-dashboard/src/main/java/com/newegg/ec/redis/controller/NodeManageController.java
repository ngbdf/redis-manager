package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.IRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 9/25/2019
 */
@RequestMapping("/nodeManage/*")
@Controller
public class NodeManageController {

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private IRedisService redisService;

    @RequestMapping(value = "/getAllNodeList/{clusterId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAllNodeList(@PathVariable("clusterId") Integer clusterId) {
        Cluster cluster = clusterService.getClusterById(clusterId);
        if (cluster == null) {
            return Result.failResult().setMessage("Get cluster failed.");
        }
        List<RedisNode> redisNodeList = redisService.getRedisNodeList(cluster);
        return Result.successResult(redisNodeList);
    }

}
