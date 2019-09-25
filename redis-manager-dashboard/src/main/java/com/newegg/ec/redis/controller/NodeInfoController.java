package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.NodeInfo;
import com.newegg.ec.redis.entity.NodeInfoParam;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.service.INodeInfoService;
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
@RequestMapping("/nodeInfo/*")
@Controller
public class NodeInfoController {

    @Autowired
    private INodeInfoService nodeInfoService;

    @RequestMapping(value = "/getNodeInfoList", method = RequestMethod.POST)
    @ResponseBody
    public Result getNodeInfoList(@RequestBody NodeInfoParam nodeInfoParam) {
        System.err.println(nodeInfoParam);
        List<NodeInfo> nodeInfoList = nodeInfoService.getNodeInfoList(nodeInfoParam);
        return nodeInfoList != null ? Result.successResult(nodeInfoList) : Result.failResult();
    }

}
