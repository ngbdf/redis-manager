package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Group;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.IGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.ws.rs.PathParam;
import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 7/20/2019
 */
@RequestMapping("/cluster/*")
@Controller
public class ClusterController {

    @Autowired
    private IGroupService groupService;

    @Autowired
    private IClusterService clusterService;

    @RequestMapping(value = "/getClusterList/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getAllCluster(@PathParam("groupId") Integer groupId) {
        List<Cluster> clusterList = clusterService.getClusterListByGroupId(groupId);
        if (clusterList == null || clusterList.isEmpty()) {
            return Result.failResult();
        }
        return Result.successResult(clusterList);
    }

    @RequestMapping(value = "/getCluster/{clusterId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getCluster(@PathParam("clusterId") Integer clusterId) {
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
    public Result saveCluster(Cluster cluster) {
        Group group = groupService.getGroupById(cluster.getGroupId());
        if (group == null) {
            return Result.failResult("Group not exist!");
        }
        Cluster exist = clusterService.getClusterByName(cluster.getClusterName());
        if (exist != null) {
            Result.failResult("Cluster name exist!");
        }
        boolean result = clusterService.addCluster(cluster);
        return result ? Result.successResult() : Result.failResult();
    }

    @RequestMapping(value = "/validateClusterName", method = RequestMethod.POST)
    public Result validateClusterName(String clusterName) {
        Cluster cluster = clusterService.getClusterByName(clusterName);
        return cluster == null ? Result.successResult() : Result.failResult();
    }

}
