package com.newegg.ec.redis.service.impl;

import com.newegg.ec.redis.client.*;
import com.newegg.ec.redis.entity.*;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.INodeInfoService;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.util.RedisNodeInfoUtil;
import com.newegg.ec.redis.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.HostAndPort;

import java.util.*;

import static com.newegg.ec.redis.util.RedisNodeInfoUtil.CLUSTER;
import static com.newegg.ec.redis.util.RedisNodeInfoUtil.STANDALONE;

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
    public Map<String, Long> getDatabase(Cluster cluster) {
        RedisURI redisURI = new RedisURI(cluster.getNodes(), cluster.getRedisPassword());
        String redisMode = cluster.getRedisMode();
        if (STANDALONE.equalsIgnoreCase(redisMode)) {
            try {
                RedisClient redisClient = RedisClientFactory.buildRedisClient(redisURI);
                Map<String, String> infoKeyspace = redisClient.getInfo(RedisClient.KEYSPACE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public List<RedisNode> getNodeList(Cluster cluster) {
        RedisURI redisURI = new RedisURI(cluster.getNodes(), cluster.getRedisPassword());
        String redisMode = cluster.getRedisMode();
        List<RedisNode> nodeList = null;
        if (STANDALONE.equalsIgnoreCase(redisMode)) {
            RedisClient redisClient = RedisClientFactory.buildRedisClient(redisURI);
            try {
                nodeList = redisClient.nodes();
            } catch (Exception e) {
                logger.error("Get redis node list failed, " + cluster, e);
            }
        } else if (CLUSTER.equalsIgnoreCase(redisMode)) {
            RedisClusterClient redisClusterClient = RedisClientFactory.buildRedisClusterClient(redisURI);
            try {
                nodeList = redisClusterClient.clusterNodes();
            } catch (Exception e) {
                logger.error("Get redis node list failed, " + cluster, e);
            }
        }
        return nodeList;
    }

    @Override
    public List<NodeInfo> getNodeInfoList(Cluster cluster, NodeInfoType.TimeType timeType) {
        String redisPassword = cluster.getRedisPassword();
        Set<HostAndPort> hostAndPortSet = getHostAndPortSet(cluster);
        List<NodeInfo> nodeInfoList = new ArrayList<>(hostAndPortSet.size());
        int clusterId = cluster.getClusterId();
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
            // 指标计算处理
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
    public List<RedisSlowLog> getRedisSlowLog(Cluster cluster, SlowLogParam slowLogParam) {
        return null;
    }

    private Set<HostAndPort> getHostAndPortSet(Cluster cluster) {
        List<RedisNode> redisNodeList = getNodeList(cluster);
        Set<HostAndPort> hostAndPortSet = new HashSet<>();
        for (RedisNode redisNode : redisNodeList) {
            hostAndPortSet.add(new HostAndPort(redisNode.getHost(), redisNode.getPort()));
        }
        return hostAndPortSet;
    }

    @Override
    public List<String> scan(DataCommandsParam dataCommandsParam) {
        return null;
    }

    @Override
    public AutoCommandResult query(AutoCommandParam autoCommandParam) {
        // standalone or cluster
        AutoCommandResult result = null;
        Cluster cluster = clusterService.getClusterById(autoCommandParam.getClusterId());
        try {
            if (cluster == null) {
                return null;
            }
            String nodes = cluster.getNodes();
            IDatabaseCommand client = null;
            String redisMode = cluster.getRedisMode();
            RedisURI redisURI = new RedisURI(RedisUtil.nodesToHostAndPortSet(nodes), cluster.getRedisPassword());
            if (STANDALONE.equalsIgnoreCase(redisMode)) {
                client = RedisClientFactory.buildRedisClient(redisURI);
            } else if (CLUSTER.equalsIgnoreCase(redisMode)) {
                client = RedisClientFactory.buildRedisClusterClient(redisURI);
            }
            result = client.query(autoCommandParam);
        } catch (Exception e) {
            logger.error("Auto query failed, " + cluster.getClusterName(), e);
        }
        return result;
    }

    @Override
    public Object console(DataCommandsParam dataCommandsParam) {
        return null;
    }

    @Override
    public boolean forget() {
        return false;
    }

}
