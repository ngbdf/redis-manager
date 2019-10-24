package com.newegg.ec.redis.controller;

import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Group;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.IGroupService;
import com.newegg.ec.redis.service.IRedisNodeService;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.util.SignUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.PathParam;
import java.util.*;

/**
 * @author Jay.H.Zou
 * @date 7/20/2019
 */
@RequestMapping("/cluster/*")
@Controller
public class ClusterController {

    private static final Logger logger = LoggerFactory.getLogger(ClusterController.class);

    @Autowired
    private IGroupService groupService;

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private IRedisService redisService;

    @Autowired
    private IRedisNodeService redisNodeService;

    @RequestMapping(value = "/getClusterList/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAllCluster(@PathVariable("groupId") Integer groupId) {
        List<Cluster> clusterList = clusterService.getClusterListByGroupId(groupId);
        if (clusterList == null) {
            return Result.failResult();
        }
        return Result.successResult(clusterList);
    }

    @RequestMapping(value = "/getCluster/{groupId}/{clusterId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getCluster(@PathVariable("groupId") Integer groupId, @PathVariable("clusterId") Integer clusterId) {
        Cluster cluster = clusterService.getClusterByIdAndGroup(groupId, clusterId);
        if (cluster != null) {
            return Result.successResult(cluster);
        }
        return Result.failResult();
    }

    /**
     * Import redis cluster not created by redis-manager
     *
     * @param cluster
     * @return
     */
    @RequestMapping(value = "/importCluster", method = RequestMethod.POST)
    @ResponseBody
    public Result importCluster(@RequestBody Cluster cluster) {
        Group group = groupService.getGroupById(cluster.getGroupId());
        if (group == null) {
            Result result = Result.failResult();
            result.setMessage("Group not exist!");
            return result;
        }
        Cluster exist = clusterService.getClusterByName(cluster.getClusterName());
        if (exist != null) {
            Result result = Result.failResult();
            result.setMessage("Cluster name exist!");
            return result;
        }
        try {
            cluster.setInstallationType(1);
            clusterService.addCluster(cluster);
            List<RedisNode> realRedisNodeList = redisService.getRedisNodeList(cluster);
            realRedisNodeList.forEach(redisNode -> {
                redisNode.setGroupId(cluster.getGroupId());
                redisNode.setClusterId(cluster.getClusterId());
            });
            redisNodeService.addRedisNodeList(realRedisNodeList);
            return Result.successResult();
        } catch (Exception e) {
            logger.error("Import cluster failed.", e);
            Result result = Result.failResult();
            result.setMessage(e.getMessage());
            return result;
        }
    }

    @RequestMapping(value = "/updateCluster", method = RequestMethod.POST)
    @ResponseBody
    public Result updateCluster(@RequestBody Cluster cluster) {
        Group group = groupService.getGroupById(cluster.getGroupId());
        if (group == null) {
            Result result = Result.failResult();
            result.setMessage("Group not exist!");
            return result;
        }
        Cluster exist = clusterService.getClusterByName(cluster.getClusterName());
        // filter itself
        if (exist != null && !Objects.equals(exist.getClusterId(), cluster.getClusterId())) {
            Result result = Result.failResult();
            result.setMessage("Cluster name exist!");
            return result;
        }
        boolean result = clusterService.updateCluster(cluster);
        if (!result) {
            return Result.failResult();
        }
        cluster = clusterService.getClusterById(cluster.getClusterId());
        return Result.successResult(cluster);
    }

    @RequestMapping(value = "/addAlertRule", method = RequestMethod.POST)
    @ResponseBody
    public Result addAlertRule(@RequestBody Cluster cluster) {
        Cluster oldCuster = clusterService.getClusterById(cluster.getClusterId());
        String ruleIds = mergeAlertIds(oldCuster.getRuleIds(), cluster.getRuleIds());
        oldCuster.setRuleIds(ruleIds);
        boolean result = clusterService.updateClusterRuleIds(oldCuster);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/deleteAlertRule", method = RequestMethod.POST)
    @ResponseBody
    public Result deleteAlertRule(@RequestBody Cluster cluster) {
        Cluster oldCuster = clusterService.getClusterById(cluster.getClusterId());
        String ruleIds = removeAlertIds(oldCuster.getRuleIds(), cluster.getRuleIds());
        oldCuster.setRuleIds(ruleIds);
        boolean result = clusterService.updateClusterRuleIds(oldCuster);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/addAlertChannel", method = RequestMethod.POST)
    @ResponseBody
    public Result addAlertChannel(@RequestBody Cluster cluster) {
        Cluster oldCuster = clusterService.getClusterById(cluster.getClusterId());
        String channelIds = mergeAlertIds(oldCuster.getChannelIds(), cluster.getChannelIds());
        oldCuster.setChannelIds(channelIds);
        boolean result = clusterService.updateClusterChannelIds(oldCuster);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/deleteAlertChannel", method = RequestMethod.POST)
    @ResponseBody
    public Result deleteAlertChannel(@RequestBody Cluster cluster) {
        Cluster oldCuster = clusterService.getClusterById(cluster.getClusterId());
        String channelIds = removeAlertIds(oldCuster.getChannelIds(), cluster.getChannelIds());
        oldCuster.setChannelIds(channelIds);
        boolean result = clusterService.updateClusterChannelIds(oldCuster);
        return result ? Result.successResult() : Result.failResult();
    }

    private String mergeAlertIds(String oldIds, String newIds) {
        Set<String> idSet = new LinkedHashSet<>();
        if (!Strings.isNullOrEmpty(oldIds)) {
            String[] oldIdArr = SignUtil.splitByCommas(oldIds);
            idSet.addAll(Arrays.asList(oldIdArr));
        }
        if (!Strings.isNullOrEmpty(newIds)) {
            String[] newIdArr = SignUtil.splitByCommas(newIds);
            idSet.addAll(Arrays.asList(newIdArr));
        }
        StringBuilder ids = new StringBuilder();
        idSet.forEach((id) -> ids.append(id).append(SignUtil.COMMAS));
        return ids.toString();
    }

    private String removeAlertIds(String oldIds, String newIds) {
        Set<String> idSet = new LinkedHashSet<>();
        if (!Strings.isNullOrEmpty(oldIds)) {
            String[] oldIdArr = SignUtil.splitByCommas(oldIds);
            idSet.addAll(Arrays.asList(oldIdArr));
        }
        if (!Strings.isNullOrEmpty(newIds)) {
            String[] newIdArr = SignUtil.splitByCommas(newIds);
            for (String id : newIdArr) {
                idSet.remove(id);
            }
        }
        StringBuilder ids = new StringBuilder();
        idSet.forEach((id) -> ids.append(id).append(SignUtil.COMMAS));
        return ids.toString();
    }

    @RequestMapping(value = "/deleteCluster", method = RequestMethod.POST)
    @ResponseBody
    public Result deleteCluster(@RequestBody Cluster cluster) {
        try {
            boolean result = clusterService.deleteCluster(cluster.getClusterId());
            return result ? Result.successResult() : Result.failResult();
        } catch (Exception e) {
            logger.error("Delete cluster failed.", e);
            return Result.failResult().setMessage("Cluster name exist!");
        }
    }

    @RequestMapping(value = "/validateClusterName/{clusterName}", method = RequestMethod.GET)
    @ResponseBody
    public Result validateClusterName(@PathVariable("clusterName") String clusterName) {
        Cluster cluster = clusterService.getClusterByName(clusterName);
        return cluster == null ? Result.successResult() : Result.failResult(cluster);
    }

}
