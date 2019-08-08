package com.newegg.ec.redis.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.client.*;
import com.newegg.ec.redis.entity.*;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.INodeInfoService;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.util.RedisNodeInfoUtil;
import com.newegg.ec.redis.util.RedisUtil;
import com.newegg.ec.redis.util.SplitUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.util.Slowlog;

import java.sql.Timestamp;
import java.util.*;

import static com.newegg.ec.redis.util.RedisNodeInfoUtil.CLUSTER;
import static com.newegg.ec.redis.util.RedisNodeInfoUtil.STANDALONE;
import static com.newegg.ec.redis.util.RedisUtil.nodesToHostAndPort;

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

    @Value("${redis-manager.monitor.slow-log-limit-all:5}")
    private int slowLogLimitAll;

    @Value("${redis-manager.monitor.slow-log-limit-single:100}")
    private int slowLogLimitSingle;

    @Override
    public Map<String, Long> getDatabase(Cluster cluster) {
        RedisURI redisURI = new RedisURI(cluster.getNodes(), cluster.getRedisPassword());
        String redisMode = cluster.getRedisMode();
        Map<String, Long> database = new LinkedHashMap<>();
        if (STANDALONE.equalsIgnoreCase(redisMode)) {
            try {
                RedisClient redisClient = RedisClientFactory.buildRedisClient(redisURI);
                Map<String, String> infoKeyspace = redisClient.getInfo(RedisClient.KEYSPACE);
                if (infoKeyspace.isEmpty()) {
                    return null;
                }
                for (Map.Entry<String, String> entry : infoKeyspace.entrySet()) {
                    String dbIndex = entry.getKey();
                    String value = entry.getValue();
                    if (Strings.isNullOrEmpty(value)) {
                        continue;
                    }
                    String[] subContents = SplitUtil.splitByCommas(value);
                    String[] keyAndNumber = SplitUtil.splitByEqualSign(subContents[0]);
                    long number = Long.valueOf(keyAndNumber[1]);
                    database.put(dbIndex, number);
                }
            } catch (Exception e) {
                logger.error("Get database map failed, " + cluster, e);
            }
        }
        return database;
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
        String node = hostAndPort.toString();
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
        List<RedisSlowLog> redisSlowLogList = new ArrayList<>();
        List<RedisNode> nodeList;
        int limit;
        String node = slowLogParam.getNode();
        if (Strings.isNullOrEmpty(node)) {
            nodeList = getNodeList(cluster);
            limit = slowLogLimitAll;
        } else {
            nodeList = new ArrayList<>();
            HostAndPort hostAndPort = nodesToHostAndPort(node);
            RedisNode redisNode = new RedisNode(hostAndPort.getHost(), hostAndPort.getPort());
            nodeList.add(redisNode);
            limit = slowLogLimitSingle;
        }
        for (RedisNode redisNode : nodeList) {
            HostAndPort hostAndPort = null;
            try {
                hostAndPort = new HostAndPort(redisNode.getHost(), redisNode.getPort());
                RedisURI redisURI = new RedisURI(hostAndPort, cluster.getRedisPassword());
                RedisClient redisClient = RedisClientFactory.buildRedisClient(redisURI);
                List<Slowlog> slowLogs = redisClient.getSlowLog(limit);
                for (Slowlog slowLog : slowLogs) {
                    RedisSlowLog redisSlowLog = new RedisSlowLog(hostAndPort, slowLog);
                    redisSlowLogList.add(redisSlowLog);
                }
            } catch (Exception e) {
                logger.error("Get " + hostAndPort + " slow log failed, cluster name: " + cluster.getClusterName(), e);
            }
        }
        return redisSlowLogList;
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
    public AutoCommandResult scan(Cluster cluster, AutoCommandParam autoCommandParam) {
        AutoCommandResult scanResult = null;
        try {
            IDatabaseCommand client = buildDatabaseCommandClient(cluster);
            if (client != null) {
                scanResult = client.scan(autoCommandParam);
            }
        } catch (Exception e) {
            logger.error("Scan redis failed, cluster name: " + cluster.getClusterName(), e);
        }
        return scanResult;
    }

    @Override
    public AutoCommandResult query(Cluster cluster, AutoCommandParam autoCommandParam) {
        // standalone or cluster
        AutoCommandResult result = null;
        try {
            IDatabaseCommand client = buildDatabaseCommandClient(cluster);
            result = client.query(autoCommandParam);
        } catch (Exception e) {
            logger.error("Auto query failed, " + cluster.getClusterName(), e);
        }
        return result;
    }

    @Override
    public Object console(Cluster cluster, DataCommandsParam dataCommandsParam) {
        Object result = null;
        try {
            IDatabaseCommand client = buildDatabaseCommandClient(cluster);
            String command = dataCommandsParam.getCommand();
            // TODO: 判断
            result = client.string(dataCommandsParam);
        } catch (Exception e) {
            logger.error("Redis operation failed, cluster name: " + cluster.getClusterName(), e);
        }
        return result;
    }


    @Override
    public boolean forget() {
        return false;
    }

    private IDatabaseCommand buildDatabaseCommandClient(Cluster cluster) {
        String nodes = cluster.getNodes();
        IDatabaseCommand client = null;
        String redisMode = cluster.getRedisMode();
        RedisURI redisURI = new RedisURI(RedisUtil.nodesToHostAndPortSet(nodes), cluster.getRedisPassword());
        if (STANDALONE.equalsIgnoreCase(redisMode)) {
            client = RedisClientFactory.buildRedisClient(redisURI);
        } else if (CLUSTER.equalsIgnoreCase(redisMode)) {
            client = RedisClientFactory.buildRedisClusterClient(redisURI);
        }
        return client;
    }

}
