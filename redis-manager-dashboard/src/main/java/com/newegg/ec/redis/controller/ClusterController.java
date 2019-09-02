package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Jay.H.Zou
 * @date 7/20/2019
 */
@RequestMapping("/cluster/*")
@Controller
public class ClusterController {


    @RequestMapping(value = "/saveCluster", method = RequestMethod.POST)
    public Result saveCluster(Cluster cluster) {
        return Result.successResult();
    }

}
