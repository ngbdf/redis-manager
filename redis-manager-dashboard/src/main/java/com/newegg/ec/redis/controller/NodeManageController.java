package com.newegg.ec.redis.controller;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.NodeRole;
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

import java.util.*;

import static com.newegg.ec.redis.entity.NodeRole.MASTER;

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
        Map<String, Set<RedisNode>> masterAndReplicaMap = new LinkedHashMap<>();

        redisNodeList.forEach(redisNode -> {
            String nodeId = redisNode.getNodeId();
            NodeRole nodeRole = redisNode.getNodeRole();
            String masterId = redisNode.getMasterId();
            if (Objects.equals(nodeRole, MASTER)) {
                masterId = nodeId;
            }
            Set<RedisNode> replicaSet = masterAndReplicaMap.get(masterId);
            if (replicaSet == null) {
                replicaSet = new TreeSet<>((o1, o2) -> {
                    int compareRole = o1.getNodeRole().compareTo(o2.getNodeRole());
                    if (compareRole != 0) {
                        return compareRole;
                    }
                    int compareHost = o1.getHost().compareTo(o2.getHost());
                    if (compareHost != 0) {
                        return compareHost;
                    }
                    return o1.getPort() - o2.getPort();
                });
            }
            replicaSet.add(redisNode);
            masterAndReplicaMap.put(masterId, replicaSet);
        });
        List<RedisNode> redisNodeHierarchy = new ArrayList<>();
        masterAndReplicaMap.values().forEach(redisNodeSet -> {
            redisNodeSet.forEach(redisNode -> redisNodeHierarchy.add(redisNode));
        });
        return Result.successResult(redisNodeHierarchy);
    }

}
