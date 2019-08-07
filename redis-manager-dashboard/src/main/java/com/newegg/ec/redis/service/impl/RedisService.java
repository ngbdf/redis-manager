package com.newegg.ec.redis.service.impl;

import com.newegg.ec.redis.client.*;
import com.newegg.ec.redis.entity.*;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.INodeInfoService;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.util.RedisClusterUtil;
import com.newegg.ec.redis.util.RedisNodeInfoUtil;
import com.newegg.ec.redis.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.HostAndPort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.newegg.ec.redis.util.RedisNodeInfoUtil.OS;
import static com.newegg.ec.redis.util.RedisNodeInfoUtil.REDIS_MODE;
import static com.newegg.ec.redis.util.RedisUtil.CLUSTER;
import static com.newegg.ec.redis.util.RedisUtil.STANDALONE;

/**
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
public class RedisService implements IRedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    private INodeInfoService nodeInfoService;

    @Autowired
    private IClusterService clusterService;

    @Override
    public List<String> getDBList(Set<HostAndPort> hostAndPortSet, String redisPassword) {
        return null;
    }

    @Override
    public List<RedisNode> getNodeList(Set<HostAndPort> hostAndPortSet, String redisPassword) {
        RedisURI redisURI = new RedisURI(hostAndPortSet, redisPassword);
        RedisClusterClient redisClusterClient = RedisClientFactory.buildRedisClusterClient(redisURI);
        return null;
    }

    @Override
    public List<NodeInfo> getNodeInfoList(int clusterId, Set<HostAndPort> hostAndPortSet, String redisPassword, NodeInfoType.TimeType timeType) {
        List<NodeInfo> nodeInfoList = new ArrayList<>(hostAndPortSet.size());
        for (HostAndPort hostAndPort : hostAndPortSet) {
            NodeInfo nodeInfo = getNodeInfo(clusterId, hostAndPort, redisPassword, timeType);
            if (nodeInfo == null) {
                continue;
            }
            nodeInfoList.add(nodeInfo);
        }
        return nodeInfoList;
    }

    @Override
    public NodeInfo getNodeInfo(int clusterId, HostAndPort hostAndPort, String redisPassword, NodeInfoType.TimeType timeType) {
        NodeInfo nodeInfo = null;
        String node = hostAndPort.getHost() + ":" + hostAndPort.getPort();
        try {
            RedisURI redisURI = new RedisURI(hostAndPort, redisPassword);
            RedisClient redisClient = RedisClientFactory.buildRedisClient(redisURI);
            Map<String, String> infoMap = redisClient.getInfo();

            // 获取上一次的 NodeInfo 来计算某些字段的差值
            NodeInfoParam nodeInfoParam = new NodeInfoParam(clusterId, NodeInfoType.DataType.NODE, timeType, node);
            NodeInfo lastTimeNodeInfo = nodeInfoService.getLastTimeNodeInfo(nodeInfoParam);
            nodeInfo = RedisNodeInfoUtil.parseInfoToObject(infoMap, lastTimeNodeInfo);
            nodeInfo.setDataType(NodeInfoType.DataType.NODE);
            nodeInfo.setLastTime(true);
            nodeInfo.setTimeType(timeType);
        } catch (Exception e) {
            logger.error("Build node info failed, node = " + node, e);
        }
        return nodeInfo;
    }

    @Override
    public Cluster getClusterInfo(Set<HostAndPort> hostAndPortSet, String redisPassword) {
        Cluster cluster = new Cluster();
        RedisURI redisURI = new RedisURI(hostAndPortSet, redisPassword);
        RedisClient redisClient = RedisClientFactory.buildRedisClient(redisURI);
        try {
            Map<String, String> serverInfoMap = redisClient.getInfo(RedisClient.SERVER);
            cluster.setRedisMode(serverInfoMap.get(REDIS_MODE));
            cluster.setOs(serverInfoMap.get(OS));
        } catch (Exception e) {
            logger.error("Parse server info failed.", e);
        }
        return cluster;
    }

    @Override
    public Cluster getServerInfo(Set<HostAndPort> hostAndPortSet, String redisPassword) {
        Cluster cluster = null;
        RedisURI redisURI = new RedisURI(hostAndPortSet, redisPassword);
        RedisClient redisClient = RedisClientFactory.buildRedisClient(redisURI);
        String clusterInfo = redisClient.getClusterInfo();
        try {
            cluster = RedisClusterUtil.parseClusterInfoToObject(clusterInfo);
        } catch (IOException e) {
            logger.error("Parse cluster info failed.", e);
        }
        return cluster;
    }

    @Override
    public List<String> scan(RedisQueryParam redisQueryParam) {
        return null;
    }

    @Override
    public RedisQueryResult query(RedisQueryParam redisQueryParam) {
        // standalone or cluster
        Cluster cluster = clusterService.getClusterById(redisQueryParam.getClusterId());
        String nodes = cluster.getNodes();
        IDatabaseCommand jedisCommands = null;
        String redisMode = cluster.getRedisMode();
        RedisURI redisURI = new RedisURI(RedisUtil.nodesToHostAndPortSet(nodes), cluster.getRedisPassword());
        if (STANDALONE.equalsIgnoreCase(redisMode)) {
            jedisCommands = RedisClientFactory.buildRedisClient(redisURI);
        } else if (CLUSTER.equalsIgnoreCase(redisMode)) {
            jedisCommands = RedisClientFactory.buildRedisClusterClient(redisURI);
        }
        return null;
    }

    @Override
    public boolean forget() {
        return false;
    }

}
