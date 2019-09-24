package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Group;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.IGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.PathParam;
import java.util.List;
import java.util.Objects;

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

    @RequestMapping(value = "/getClusterList/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAllCluster(@PathVariable("groupId") Integer groupId) {
        List<Cluster> clusterList = clusterService.getClusterListByGroupId(groupId);
        if (clusterList == null) {
            return Result.failResult();
        }
        return Result.successResult(clusterList);
    }

    @RequestMapping(value = "/getCluster/{clusterId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getCluster(@PathVariable("clusterId") Integer clusterId) {
        Cluster cluster = clusterService.getClusterById(clusterId);
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
    public Result importCluster(Cluster cluster) {
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
            boolean result = clusterService.addCluster(cluster);
            return result ? Result.successResult() : Result.failResult();
        } catch (Exception e) {
            logger.error("Import cluster failed.", e);
            Result result = Result.failResult();
            result.setMessage(e.getMessage());
            return result;
        }
    }

    @RequestMapping(value = "/updateCluster", method = RequestMethod.POST)
    @ResponseBody
    public Result updateCluster(Cluster cluster) {
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
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/deleteCluster", method = RequestMethod.POST)
    @ResponseBody
    public Result deleteCluster(@RequestBody Integer clusterId) {
        try {
            boolean result = clusterService.deleteCluster(clusterId);
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
