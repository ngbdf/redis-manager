package com.newegg.ec.redis.controller;

import com.alibaba.fastjson.JSONObject;
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
import com.newegg.ec.redis.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.HostAndPort;

import java.util.*;

import static com.newegg.ec.redis.util.RedisConfigUtil.*;
import static com.newegg.ec.redis.util.RedisUtil.CLUSTER;
import static com.newegg.ec.redis.util.TimeUtil.TEN_SECONDS;

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

    @RequestMapping(value = "/getNodeInfo", method = RequestMethod.POST)
    @ResponseBody
    public Result getNodeInfo(@RequestBody RedisNode redisNode) {
        Integer clusterId = redisNode.getClusterId();
        Cluster cluster = clusterService.getClusterById(clusterId);
        HostAndPort hostAndPort = new HostAndPort(redisNode.getHost(), redisNode.getPort());
        Map<String, String> infoMap = redisService.getNodeInfo(hostAndPort, cluster.getRedisPassword());
        if (infoMap == null) {
            return Result.failResult();
        }
        List<JSONObject> infoList = new ArrayList<>();
        infoMap.forEach((key, value) -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", key);
            jsonObject.put("value", value);
            jsonObject.put("description", RedisNodeInfoUtil.getNodeInfoItemDesc(key));
            infoList.add(jsonObject);
        });
        return Result.successResult(infoList);
    }

    @RequestMapping(value = "/getConfig", method = RequestMethod.POST)
    @ResponseBody
    public Result getConfig(@RequestBody RedisNode redisNode) {
        Integer clusterId = redisNode.getClusterId();
        Cluster cluster = clusterService.getClusterById(clusterId);
        Map<String, String> configMap = redisService.getConfig(redisNode, cluster.getRedisPassword(), null);
        if (configMap == null) {
            return Result.failResult();
        }
        List<JSONObject> configList = new ArrayList<>();
        configMap.forEach((key, value) -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", key);
            jsonObject.put("value", value);
            jsonObject.put("description", RedisConfigUtil.getConfigItemDesc(key));
            configList.add(jsonObject);
        });
        return Result.successResult(configList);
    }

    @RequestMapping(value = "/getConfigCurrentValue", method = RequestMethod.POST)
    @ResponseBody
    public Result getConfigCurrentValue(@RequestBody JSONObject jsonObject) {
        Cluster cluster = jsonObject.getObject("cluster", Cluster.class);
        String configKey = jsonObject.getString("configKey");
        List<RedisNode> redisNodeList = redisService.getRedisNodeList(cluster);
        List<JSONObject> configList = new ArrayList<>();
        redisNodeList.forEach(redisNode -> {
            Map<String, String> configMap = redisService.getConfig(redisNode, cluster.getRedisPassword(), configKey);
            JSONObject config = new JSONObject();
            config.put("redisNode", RedisUtil.getNodeString(redisNode));
            if (configMap != null) {
                config.put("configValue", configMap.get(configKey));
            }
            configList.add(config);
        });
        return Result.successResult(configList);
    }

    @RequestMapping(value = "/getRedisConfigKeyList", method = RequestMethod.GET)
    @ResponseBody
    public Result getRedisConfigKeyList() {
        Set<String> configKeyList = RedisConfigUtil.getConfigKeyList();
        Iterator<String> iterator = configKeyList.iterator();
        while (iterator.hasNext()) {
            String configKey = iterator.next();
            if (Objects.equals(configKey, REQUIRE_PASS)
                    || Objects.equals(configKey, MASTER_AUTH)
                    || Objects.equals(configKey, BIND)
                    || Objects.equals(configKey, PORT)
                    || Objects.equals(configKey, DIR)
                    || Objects.equals(configKey, DAEMONIZE)
            ) {
              iterator.remove();
            }
        }
        return Result.successResult(configKeyList);
    }

    @RequestMapping(value = "/updateRedisConfig", method = RequestMethod.POST)
    @ResponseBody
    public Result updateRedisConfig(@RequestBody JSONObject jsonObject) {
        Integer clusterId = jsonObject.getInteger("clusterId");
        RedisConfigUtil.RedisConfig redisConfig = jsonObject.getObject("redisConfig", RedisConfigUtil.RedisConfig.class);
        Cluster cluster = clusterService.getClusterById(clusterId);
        boolean result = redisService.setConfigBatch(cluster, redisConfig);
        return result ? Result.successResult() : Result.failResult();
    }

    /**
     * TODO: jedis 暂无该 API，需自己完成
     *
     * @param redisNodeList
     * @return
     */
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
        Result result = clusterOperate(redisNodeList, (cluster, redisNode) -> {
            String node = redisNode.getHost() + SignUtil.COLON + redisNode.getPort();
            String nodes = cluster.getNodes();
            if (nodes.contains(node)) {
                logger.warn("I can't forget " + node + ", because it in the database");
                return true;
            }
            return redisService.clusterForget(cluster, redisNode);
        });
        return result;
    }

    @RequestMapping(value = "/moveSlot", method = RequestMethod.POST)
    @ResponseBody
    public Result moveSlot(@RequestBody JSONObject jsonObject) {
        RedisNode redisNode = jsonObject.getObject("redisNode", RedisNode.class);
        SlotBalanceUtil.Shade slot = jsonObject.getObject("slotRange", SlotBalanceUtil.Shade.class);
        Cluster cluster = getCluster(redisNode.getClusterId());
        boolean result = redisService.clusterMoveSlots(cluster, redisNode, slot);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/replicateOf", method = RequestMethod.POST)
    @ResponseBody
    public Result replicateOf(@RequestBody List<RedisNode> redisNodeList) {
        Result result = clusterOperate(redisNodeList, (cluster, redisNode) -> redisService.clusterReplicate(cluster, redisNode.getMasterId(), redisNode));
        return result;
    }

    @RequestMapping(value = "/standaloneForget", method = RequestMethod.POST)
    @ResponseBody
    public Result standaloneForget(@RequestBody List<RedisNode> redisNodeList) {
        Result result = clusterOperate(redisNodeList, (cluster, redisNode) -> redisService.standaloneReplicaNoOne(cluster, redisNode));
        return result;
    }

    @RequestMapping(value = "/standaloneReplicateOf", method = RequestMethod.POST)
    @ResponseBody
    public Result standaloneReplicateOf(@RequestBody List<RedisNode> redisNodeList) {
        Result result = clusterOperate(redisNodeList, (cluster, redisNode) -> {
            String masterNode = redisNode.getMasterId();
            HostAndPort hostAndPort = RedisUtil.nodesToHostAndPort(masterNode);
            RedisNode masterRedisNode = RedisNode.masterRedisNode(hostAndPort);
            String resultMessage = redisService.standaloneReplicaOf(cluster, masterRedisNode, redisNode);
            return Strings.isNullOrEmpty(resultMessage);
        });
        return result;
    }

    @RequestMapping(value = "/failOver", method = RequestMethod.POST)
    @ResponseBody
    public Result failOver(@RequestBody List<RedisNode> redisNodeList) {
        Result result = clusterOperate(redisNodeList, (cluster, redisNode) -> redisService.clusterFailOver(cluster, redisNode));
        return result;
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
                return false;
            } else {
                abstractNodeOperation.remove(cluster, redisNode);
                redisNodeService.deleteRedisNodeById(redisNode.getRedisNodeId());
                return true;
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
    public Result importNode(@RequestBody List<RedisNode> redisNodeList) {
        try {
            RedisNode firstRedisNode = redisNodeList.get(0);
            final Cluster cluster = getCluster(firstRedisNode.getClusterId());
            String redisMode = cluster.getRedisMode();
            StringBuilder message = new StringBuilder();
            List<RedisNode> redisMasterNodeList = redisService.getRedisMasterNodeList(cluster);
            redisNodeList.forEach(redisNode -> {
                Iterator<RedisNode> iterator = redisMasterNodeList.iterator();
                RedisNode seed = null;
                while (iterator.hasNext()) {
                    RedisNode masterNode = iterator.next();
                    if (Objects.equals(redisNode, masterNode)) {
                        continue;
                    }
                    seed = masterNode;
                    break;
                }
                if (seed != null) {
                    String oneResult = null;
                    if (Objects.equals(redisMode, CLUSTER)) {
                        oneResult = redisService.clusterMeet(cluster, seed, redisNode);
                    } else {
                        redisService.standaloneReplicaOf(cluster, seed, redisNode);
                    }
                    if (Strings.isNullOrEmpty(oneResult)) {
                        boolean exist = redisNodeService.existRedisNode(redisNode);
                        if (!exist) {
                            try {
                                Thread.sleep(TEN_SECONDS);
                            } catch (InterruptedException e) {
                            }
                            List<RedisNode> redisNodes = redisService.getRedisNodeList(cluster);
                            redisNodes.forEach(node -> {
                                if (RedisUtil.equals(node, redisNode)) {
                                    node.setClusterId(cluster.getClusterId());
                                    node.setGroupId(cluster.getGroupId());
                                    redisNodeService.addRedisNode(node);
                                }
                            });
                        }
                    } else {
                        message.append(oneResult);
                    }
                }
            });
            return Strings.isNullOrEmpty(message.toString()) ? Result.successResult() : Result.failResult().setMessage(message.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failResult().setMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/initSlots", method = RequestMethod.POST)
    @ResponseBody
    public Result initSlots(@RequestBody Cluster cluster) {
        cluster = clusterService.getClusterById(cluster.getClusterId());
        String result = redisService.initSlots(cluster);
        return Strings.isNullOrEmpty(result) ? Result.successResult():Result.failResult().setMessage(result);
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
                String[] nodeArr = SignUtil.splitByCommas(cluster.getNodes());
                String node = redisNode.getHost() + SignUtil.COLON + redisNode.getPort();
                if (nodeArr.length > 1) {
                    StringBuilder newNodes = new StringBuilder();
                    for (String nodeItem : nodeArr) {
                        if (!Objects.equals(node, nodeItem) && !Strings.isNullOrEmpty(nodeItem)) {
                            newNodes.append(nodeItem).append(SignUtil.COMMAS);
                        }
                    }
                    cluster.setNodes(newNodes.toString());
                    clusterService.updateNodes(cluster);
                }
                boolean handleResult = clusterHandler.handle(cluster, redisNode);
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
                String[] nodeArr = SignUtil.splitByCommas(cluster.getNodes());
                String node = redisNode.getHost() + SignUtil.COLON + redisNode.getPort();
                if (nodeArr.length > 1) {
                    StringBuilder newNodes = new StringBuilder();
                    for (String nodeItem : nodeArr) {
                        if (!Objects.equals(node, nodeItem) && !Strings.isNullOrEmpty(nodeItem)) {
                            newNodes.append(nodeItem).append(SignUtil.COMMAS);
                        }
                    }
                    cluster.setNodes(newNodes.toString());
                    clusterService.updateNodes(cluster);
                }
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

        boolean handle(Cluster cluster, RedisNode redisNode);

    }

    interface NodeHandler {

        boolean handle(Cluster cluster, RedisNode redisNode, AbstractNodeOperation abstractNodeOperation);

    }

}
