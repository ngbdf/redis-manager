package com.newegg.ec.redis.controller;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.plugin.install.InstallationTemplate;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.plugin.install.service.AbstractNodeOperation;
import com.newegg.ec.redis.plugin.install.service.InstallationOperation;
import com.newegg.ec.redis.plugin.install.service.impl.DockerNodeOperation;
import com.newegg.ec.redis.plugin.install.service.impl.MachineNodeOperation;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.IRedisNodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

import static com.newegg.ec.redis.plugin.install.entity.InstallationEnvironment.*;

/**
 * @author Jay.H.Zou
 * @date 9/28/2019
 */
@RequestMapping("/installation/*")
@Controller
public class InstallationController {

    private static final Logger logger = LoggerFactory.getLogger(InstallationController.class);

    @Autowired
    private DockerNodeOperation dockerNodeOperation;

    @Autowired
    private MachineNodeOperation machineNodeOperation;

    @Autowired
    private InstallationTemplate installationTemplate;

    @Autowired
    private IClusterService clusterService;

    @RequestMapping(value = "getDockerImages", method = RequestMethod.GET)
    @ResponseBody
    public Result getDockerImages() {
        try {
            List<String> imageList = dockerNodeOperation.getImageList();
            return Result.successResult(imageList);
        } catch (Exception e) {
            logger.error("Get docker image list failed.", e);
            return Result.failResult().setMessage("Get docker image list failed.");
        }
    }

    @RequestMapping(value = "getMachineImages", method = RequestMethod.GET)
    @ResponseBody
    public Result getMachineImages() {
        try {
            List<String> imageList = machineNodeOperation.getImageList();
            return Result.successResult(imageList);
        } catch (Exception e) {
            logger.error("Get machine image list failed.", e);
            return Result.failResult().setMessage("Get machine image list failed.");
        }
    }

    /********************************* Installation step *********************************/

    @RequestMapping(value = "installFlow", method = RequestMethod.POST)
    @ResponseBody
    public Result install(@RequestBody InstallationParam installationParam) {
        Integer installationEnvironment = installationParam.getCluster().getInstallationEnvironment();
        AbstractNodeOperation nodeOperation = clusterService.getNodeOperation(installationEnvironment);
        boolean result = installationTemplate.installFlow(nodeOperation, installationParam);
        return result ? Result.successResult() : Result.failResult();
    }

    /*@RequestMapping(value = "prepareForInstallation", method = RequestMethod.POST)
    @ResponseBody
    public Result prepareForInstallation(@RequestBody InstallationParam installationParam) {
        Integer installationEnvironment = installationParam.getInstallationEnvironment();
        AbstractNodeOperation nodeOperation = getNodeOperation(installationEnvironment);
        boolean prepareSuccess = installationTemplate.prepareForInstallation(nodeOperation, installationParam);
        if (!prepareSuccess) {
            return Result.failResult();
        }
        List<String> redisNodes = new LinkedList<>();
        Map<RedisNode, Collection<RedisNode>> topology = installationParam.getTopology().asMap();
        topology.forEach((masterNode, replicaCol) -> {
            redisNodes.add(masterNode.getHost() + ":" + masterNode.getPort() + masterNode.getNodeRole().getValue());
            replicaCol.forEach(replica -> {
                redisNodes.add(replica.getHost() + ":" + replica.getPort() + replica.getNodeRole().getValue());
            });
        });
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(installationParam));
        jsonObject.put("redisNodes", redisNodes);
        return Result.successResult(jsonObject);
    }*/

   /* @RequestMapping(value = "environmentCheck", method = RequestMethod.POST)
    @ResponseBody
    public Result environmentCheck(@RequestBody InstallationParam installationParam) {
        return Result.successResult();
    }

    @RequestMapping(value = "pullImage", method = RequestMethod.POST)
    @ResponseBody
    public Result pullImage(@RequestBody InstallationParam installationParam) {
        return Result.successResult();
    }

    @RequestMapping(value = "pullConfig", method = RequestMethod.POST)
    @ResponseBody
    public Result pullConfig(@RequestBody InstallationParam installationParam) {
        return Result.successResult();
    }

    @RequestMapping(value = "install", method = RequestMethod.POST)
    @ResponseBody
    public Result install(@RequestBody InstallationParam installationParam) {
        return Result.successResult();
    }

    @RequestMapping(value = "init", method = RequestMethod.POST)
    @ResponseBody
    public Result init(@RequestBody InstallationParam installationParam) {
        return Result.successResult();
    }*/

    /********************************* Installation step *********************************/

    @RequestMapping(value = "validateTopology", method = RequestMethod.POST)
    @ResponseBody
    public Result validateTopology(@RequestBody InstallationParam installationParam) {

        return null;
    }
}
