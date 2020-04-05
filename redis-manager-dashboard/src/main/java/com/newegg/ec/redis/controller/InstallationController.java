package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.aop.annotation.OperationLog;
import com.newegg.ec.redis.config.SystemConfig;
import com.newegg.ec.redis.entity.OperationObjectType;
import com.newegg.ec.redis.entity.OperationType;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.plugin.install.InstallationTemplate;
import com.newegg.ec.redis.plugin.install.entity.InstallationLogContainer;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.plugin.install.service.AbstractNodeOperation;
import com.newegg.ec.redis.plugin.install.service.impl.DockerNodeOperation;
import com.newegg.ec.redis.plugin.install.service.impl.HumpbackNodeOperation;
import com.newegg.ec.redis.plugin.install.service.impl.MachineNodeOperation;
import com.newegg.ec.redis.service.IClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private HumpbackNodeOperation humpbackNodeOperation;

    @Autowired
    private InstallationTemplate installationTemplate;

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private SystemConfig systemConfig;

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

    @RequestMapping(value = "getHumpbackImages", method = RequestMethod.GET)
    @ResponseBody
    public Result getHumpbackImages() {
        try {
            List<String> imageList = humpbackNodeOperation.getImageList();
            return Result.successResult(imageList);
        } catch (Exception e) {
            logger.error("Get humpback image list failed.", e);
            return Result.failResult().setMessage("Get humpback image list failed.");
        }
    }

    /********************************* Installation step *********************************/

    @RequestMapping(value = "installFlow", method = RequestMethod.POST)
    @ResponseBody
    @OperationLog(type = OperationType.INSTALL, objType = OperationObjectType.CLUSTER)
    public Result install(@RequestBody InstallationParam installationParam) {
        Integer installationEnvironment = installationParam.getCluster().getInstallationEnvironment();
        AbstractNodeOperation nodeOperation = clusterService.getNodeOperation(installationEnvironment);
        boolean result = installationTemplate.installFlow(nodeOperation, installationParam);
        InstallationLogContainer.remove(installationParam.getCluster().getClusterName());
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/validateClusterName/{clusterName}", method = RequestMethod.GET)
    @ResponseBody
    public Result validateClusterName(@PathVariable("clusterName") String clusterName) {
        return Result.successResult();
    }

    @RequestMapping(value = "/getInstallationLogs/{clusterName}", method = RequestMethod.GET)
    @ResponseBody
    public Result getInstallationLogs(@PathVariable("clusterName") String clusterName) {
        List<String> logs = InstallationLogContainer.getLogs(clusterName);
        return Result.successResult(logs);
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

}