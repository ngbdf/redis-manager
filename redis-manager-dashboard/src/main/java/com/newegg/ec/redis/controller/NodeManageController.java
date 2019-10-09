package com.newegg.ec.redis.controller;

import com.google.common.base.Strings;
import com.newegg.ec.redis.client.RedisClient;
import com.newegg.ec.redis.client.RedisClientFactory;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.plugin.install.service.AbstractNodeOperation;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.IMachineService;
import com.newegg.ec.redis.service.IRedisNodeService;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.util.NetworkUtil;
import com.newegg.ec.redis.util.RedisConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 9/25/2019
 */
@RequestMapping("/nodeManage/*")
@Controller
public class NodeManageController {

    private static final Logger logger = LoggerFactory.getLogger(NodeManageController.class);

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private IRedisService redisService;

    @Autowired
    private IRedisNodeService redisNodeService;

    @Autowired
    private IMachineService machineService;

    /**
     * 在此处理 node 之间的关系
     * 设置 inCluster, runStatus
     *
     * @param clusterId
     * @return
     */
    @RequestMapping(value = "/getAllNodeList/{clusterId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAllNodeList(@PathVariable("clusterId") Integer clusterId) {
        Cluster cluster = clusterService.getClusterById(clusterId);
        if (cluster == null) {
            return Result.failResult().setMessage("Get cluster failed.");
        }
        List<RedisNode> redisNodeList = redisService.getRedisNodeList(cluster);
        List<RedisNode> redisNodeSorted = redisNodeService.sortRedisNodeList(redisNodeList);
        return Result.successResult(redisNodeSorted);
    }

    @RequestMapping(value = "/getAllNodeListWithStatus/{clusterId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAllNodeListWithStatus(@PathVariable("clusterId") Integer clusterId) {
        List<RedisNode> redisNodeSorted = redisNodeService.getRedisNodeListByClusterId(clusterId);
        return Result.successResult(redisNodeSorted);
    }

    @RequestMapping(value = "/purgeMemory", method = RequestMethod.POST)
    @ResponseBody
    public Result purgeMemory(@RequestBody List<RedisNode> redisNodeList) {
        if (!verifyRedisNodeList(redisNodeList)) {
            return Result.failResult();
        }
        return Result.successResult();
    }

    @RequestMapping(value = "/forget", method = RequestMethod.POST)
    @ResponseBody
    public Result forget(@RequestBody List<RedisNode> redisNodeList) {
        Result result = clusterOperate(redisNodeList, (redisNode, redisClient) -> redisClient.clusterForget(redisNode.getNodeId()));
        return result;
    }

    @RequestMapping(value = "/moveSlot", method = RequestMethod.POST)
    @ResponseBody
    public Result moveSlot() {

        return Result.successResult();
    }

    @RequestMapping(value = "/beSlave", method = RequestMethod.POST)
    @ResponseBody
    public Result beSlave() {

        return Result.successResult();
    }

    @RequestMapping(value = "/failOver", method = RequestMethod.POST)
    @ResponseBody
    public Result failOver() {

        return Result.successResult();
    }

    @RequestMapping(value = "/start", method = RequestMethod.POST)
    @ResponseBody
    public Result start(@RequestBody List<RedisNode> redisNodeList) {
        Result result = nodeOperate(redisNodeList, (cluster, redisNode, abstractNodeOperation) -> {
            if (NetworkUtil.telnet(redisNode.getHost(), redisNode.getPort())) {
                return true;
            } else {
                return abstractNodeOperation.start(cluster, redisNode);
            }
        });
        return result;
    }

    @RequestMapping(value = "/stop", method = RequestMethod.POST)
    @ResponseBody
    public Result stop(@RequestBody List<RedisNode> redisNodeList) {
        Result result = nodeOperate(redisNodeList, (cluster, redisNode, abstractNodeOperation) -> {
            if (NetworkUtil.telnet(redisNode.getHost(), redisNode.getPort())) {
                return abstractNodeOperation.stop(cluster, redisNode);
            } else {
                return false;
            }
        });
        return result;
    }

    @RequestMapping(value = "/restart", method = RequestMethod.POST)
    @ResponseBody
    public Result restart(@RequestBody List<RedisNode> redisNodeList) {
        Result result = nodeOperate(redisNodeList, (cluster, redisNode, abstractNodeOperation) -> {
            if (NetworkUtil.telnet(redisNode.getHost(), redisNode.getPort())) {
                return abstractNodeOperation.restart(cluster, redisNode);
            } else {
                return abstractNodeOperation.start(cluster, redisNode);
            }
        });
        return result;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public Result delete(@RequestBody List<RedisNode> redisNodeList) {
        Result result = nodeOperate(redisNodeList, (cluster, redisNode, abstractNodeOperation) -> {
            if (NetworkUtil.telnet(redisNode.getHost(), redisNode.getPort())) {
                return abstractNodeOperation.remove(cluster, redisNode);
            } else {
                return abstractNodeOperation.remove(cluster, redisNode);
            }
        });
        return result;
    }

    @RequestMapping(value = "/editConfig", method = RequestMethod.POST)
    @ResponseBody
    public Result editConfig(@RequestBody List<RedisNode> redisNodeList, RedisConfigUtil.RedisConfig redisConfig) {

        return Result.successResult();
    }

    @RequestMapping(value = "/importNode", method = RequestMethod.POST)
    @ResponseBody
    public Result importNode(@RequestBody RedisNode redisNode) {

        return Result.successResult();
    }


    private Cluster getCluster(Integer clusterId) {
        return clusterService.getClusterById(clusterId);
    }

    private boolean verifyRedisNodeList(List<RedisNode> redisNodeList) {
        return redisNodeList != null && !redisNodeList.isEmpty();
    }

    /**
     * cluster operation
     *
     * @param redisNodeList
     * @param clusterHandler
     * @return
     */
    private Result clusterOperate(List<RedisNode> redisNodeList, ClusterHandler clusterHandler) {
        if (!verifyRedisNodeList(redisNodeList)) {
            return Result.failResult();
        }
        Integer clusterId = redisNodeList.get(0).getClusterId();
        Cluster cluster = getCluster(clusterId);
        StringBuffer messageBuffer = new StringBuffer();
        redisNodeList.forEach(redisNode -> {
            try {
                RedisClient redisClient = RedisClientFactory.buildRedisClient(redisNode, cluster.getRedisPassword());
                boolean handleResult = clusterHandler.handle(redisNode, redisClient);
                if (!handleResult) {
                    messageBuffer.append(redisNode.getHost() + ":" + redisNode.getPort() + " operation failed.\n");
                }
            } catch (Exception e) {
                logger.error("Operation failed.", e);
            }
        });
        String message = messageBuffer.toString();
        return Strings.isNullOrEmpty(message) ? Result.successResult() : Result.failResult().setMessage(message);
    }

    /**
     * node operation
     *
     * @param redisNodeList
     * @param nodeHandler
     * @return
     */
    private Result nodeOperate(List<RedisNode> redisNodeList, NodeHandler nodeHandler) {
        if (!verifyRedisNodeList(redisNodeList)) {
            return Result.failResult();
        }
        Integer clusterId = redisNodeList.get(0).getClusterId();
        Cluster cluster = getCluster(clusterId);
        AbstractNodeOperation nodeOperation = clusterService.getNodeOperation(cluster.getInstallationEnvironment());
        StringBuffer messageBuffer = new StringBuffer();
        redisNodeList.forEach(redisNode -> {
            try {
                boolean handleResult = nodeHandler.handle(cluster, redisNode, nodeOperation);
                if (!handleResult) {
                    messageBuffer.append(redisNode.getHost() + ":" + redisNode.getPort() + " operation failed.\n");
                }
            } catch (Exception e) {
                logger.error("Node operation failed.", e);
            }
        });
        String message = messageBuffer.toString();
        return Strings.isNullOrEmpty(message) ? Result.successResult() : Result.failResult().setMessage(message);
    }

    interface ClusterHandler {

        boolean handle(RedisNode redisNode, RedisClient redisClient);

    }

    interface NodeHandler {

        boolean handle(Cluster cluster, RedisNode redisNode, AbstractNodeOperation abstractNodeOperation);

    }

}
