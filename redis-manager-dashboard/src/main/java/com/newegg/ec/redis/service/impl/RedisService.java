package com.newegg.ec.redis.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.client.*;
import com.newegg.ec.redis.entity.*;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.INodeInfoService;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.util.RedisUtil;
import com.newegg.ec.redis.util.SignUtil;
import com.newegg.ec.redis.util.SlotBalanceUtil;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import redis.clients.jedis.ClusterReset;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.params.MigrateParams;
import redis.clients.jedis.util.Slowlog;

import java.util.*;

import static com.newegg.ec.redis.client.IDatabaseCommand.*;
import static com.newegg.ec.redis.util.RedisClusterInfoUtil.OK;
import static com.newegg.ec.redis.util.RedisConfigUtil.MASTER_AUTH;
import static com.newegg.ec.redis.util.RedisConfigUtil.REQUIRE_PASS;
import static com.newegg.ec.redis.util.RedisNodeInfoUtil.EXPIRES;
import static com.newegg.ec.redis.util.RedisNodeInfoUtil.KEYS;
import static com.newegg.ec.redis.util.RedisUtil.*;

/**
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
@Service
public class RedisService implements IRedisService, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    private INodeInfoService nodeInfoService;

    @Autowired
    private IClusterService clusterService;

    @Value("${redis-manager.monitor.slow-log-limit:100}")
    private int slowLogLimit;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

    }

    /**
     * db0:keys=31,expires=1,avg_ttl=1
     *
     * @param cluster
     * @return
     */
    @Override
    public Map<String, Map<String, Long>> getKeyspaceInfo(Cluster cluster) {
        List<RedisNode> redisMasterNodeList = getRedisMasterNodeList(cluster);
        Map<String, Map<String, Long>> keyspaceInfoMap = new LinkedHashMap<>();
        redisMasterNodeList.forEach(redisNode -> {
            RedisClient redisClient = RedisClientFactory.buildRedisClient(redisNode, cluster.getRedisPassword());
            try {
                Map<String, String> keyspaceInfo = redisClient.getInfo(RedisClient.KEYSPACE);
                if (keyspaceInfo.isEmpty()) {
                    return;
                }
                // key: db0, val: keys=31,expires=1,avg_ttl=1
                keyspaceInfo.forEach((key, val) -> {
                    Map<String, Long> nodeKeyspaceInfoMap = keyspaceInfoMap.get(key);
                    if (nodeKeyspaceInfoMap == null) {
                        nodeKeyspaceInfoMap = new LinkedHashMap<>();
                    }
                    String[] subContents = SignUtil.splitByCommas(val);
                    for (String subContent : subContents) {
                        String[] split = SignUtil.splitByEqualSign(subContent);
                        if (split.length != 2) {
                            continue;
                        }
                        String subContentKey = split[0];
                        String subContentVal = split[1];
                        if (Strings.isNullOrEmpty(subContentVal)) {
                            continue;
                        }
                        long nodeCount = Long.parseLong(subContentVal);
                        if (Objects.equals(subContentKey, KEYS)) {
                            Long keys = nodeKeyspaceInfoMap.get(KEYS);
                            keys = keys == null ? nodeCount : keys + nodeCount;
                            nodeKeyspaceInfoMap.put(KEYS, keys);
                        } else if (Objects.equals(subContentKey, EXPIRES)) {
                            Long expires = nodeKeyspaceInfoMap.get(EXPIRES);
                            expires = expires == null ? nodeCount : expires + nodeCount;
                            nodeKeyspaceInfoMap.put(EXPIRES, expires);
                        }
                    }
                    keyspaceInfoMap.put(key, nodeKeyspaceInfoMap);
                });
            } catch (Exception e) {
                logger.error("Get keyspace info failed, redis node = " + redisNode.getHost() + ":" + redisNode.getPort(), e);
            }
        });
        return keyspaceInfoMap;
    }

    @Override
    public Map<String, Long> getDatabase(Cluster cluster) {
        Map<String, Long> database = new LinkedHashMap<>();
        Map<String, Map<String, Long>> keyspaceInfo = getKeyspaceInfo(cluster);
        keyspaceInfo.forEach((key, val) -> {
            database.put(key, val.get(KEYS));
        });
        return database;
    }

    @Override
    public List<RedisNode> getRedisNodeList(Cluster cluster) {
        RedisURI redisURI = new RedisURI(cluster.getNodes(), cluster.getRedisPassword());
        String redisMode = cluster.getRedisMode();
        List<RedisNode> nodeList = new ArrayList<>();
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
    public List<RedisNode> getRedisMasterNodeList(Cluster cluster) {
        List<RedisNode> masterNodeList = new ArrayList<>();
        List<RedisNode> redisNodeList = getRedisNodeList(cluster);
        if (redisNodeList == null) {
            return masterNodeList;
        }
        redisNodeList.forEach(redisNode -> {
            if (Objects.equals(NodeRole.MASTER, redisNode.getNodeRole())) {
                masterNodeList.add(redisNode);
            }
        });
        return masterNodeList;
    }

    @Override
    public Map<String, String> getClusterInfo(Cluster cluster) {
        try {
            String redisPassword = cluster.getRedisPassword();
            Set<HostAndPort> hostAndPortSet = getHostAndPortSet(cluster);
            RedisURI redisURI = new RedisURI(hostAndPortSet, redisPassword);
            RedisClient redisClient = RedisClientFactory.buildRedisClient(redisURI);
            Map<String, String> clusterInfo = redisClient.getClusterInfo();
            return clusterInfo;
        } catch (Exception e) {
            logger.error("Get cluster info failed, " + cluster, e);
            return null;
        }
    }

    @Override
    public List<RedisSlowLog> getRedisSlowLog(Cluster cluster, SlowLogParam slowLogParam) {

        List<RedisNode> nodeList;
        String node = slowLogParam.getNode();
        if (Strings.isNullOrEmpty(node)) {
            nodeList = getRedisNodeList(cluster);
        } else {
            nodeList = new ArrayList<>();
            HostAndPort hostAndPort = nodesToHostAndPort(node);
            RedisNode redisNode = new RedisNode(hostAndPort.getHost(), hostAndPort.getPort());
            nodeList.add(redisNode);
        }
        List<RedisSlowLog> redisSlowLogList = new ArrayList<>();
        for (RedisNode redisNode : nodeList) {
            HostAndPort hostAndPort = null;
            try {
                hostAndPort = new HostAndPort(redisNode.getHost(), redisNode.getPort());
                RedisURI redisURI = new RedisURI(hostAndPort, cluster.getRedisPassword());
                RedisClient redisClient = RedisClientFactory.buildRedisClient(redisURI);
                List<Slowlog> slowLogs = redisClient.getSlowLog(slowLogLimit);
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
        List<RedisNode> redisNodeList = getRedisNodeList(cluster);
        Set<HostAndPort> hostAndPortSet = new HashSet<>();
        for (RedisNode redisNode : redisNodeList) {
            hostAndPortSet.add(new HostAndPort(redisNode.getHost(), redisNode.getPort()));
        }
        return hostAndPortSet;
    }

    @Override
    public Set<String> scan(Cluster cluster, AutoCommandParam autoCommandParam) {
        List<RedisNode> redisMasterNodeList = getRedisMasterNodeList(cluster);
        int masterSize = redisMasterNodeList.size();
        int count = masterSize < 10 ? 100 / masterSize : 10;
        autoCommandParam.setCount(count);
        Set<String> result = new LinkedHashSet<>();
        redisMasterNodeList.forEach(masterNode -> {
            try {
                RedisClient redisClient = RedisClientFactory.buildRedisClient(masterNode, cluster.getRedisPassword());
                Set<String> scanResult = redisClient.scan(autoCommandParam);
                result.addAll(scanResult);
            } catch (Exception e) {
                logger.error("Scan redis failed, node = " + masterNode.getHost() + ":" + masterNode.getPort(), e);
            }
        });
        return result;
    }

    @Override
    public AutoCommandResult query(Cluster cluster, AutoCommandParam autoCommandParam) {
        // standalone or cluster
        try {
            IDatabaseCommand client = buildDatabaseCommandClient(cluster);
            return client.query(autoCommandParam);
        } catch (Exception e) {
            logger.error("Auto query failed, cluster name = " + cluster.getClusterName(), e);
            return null;
        }
    }

    @Override
    public Object console(Cluster cluster, DataCommandsParam dataCommandsParam) {
        Object result = null;
        try {
            IDatabaseCommand client = buildDatabaseCommandClient(cluster);
            String command = dataCommandsParam.getCommand().toUpperCase();
            if (command.startsWith(GET) || command.startsWith(SET)) {
                result = client.string(dataCommandsParam);
            } else if (command.startsWith(HGET) || command.startsWith(HMGET)
                    || command.startsWith(HGETALL) || command.startsWith(HKEYS)
                    || command.startsWith(HSET)) {
                result = client.hash(dataCommandsParam);
            } else if (command.startsWith(LINDEX) || command.startsWith(LLEN)
                    || command.startsWith(LRANGE) || command.startsWith(LPUSH)
                    || command.startsWith(RPUSH)) {
                result = client.list(dataCommandsParam);
            } else if (command.startsWith(SCARD) || command.startsWith(SADD)
                    || command.startsWith(SMEMBERS) || command.startsWith(SRANDMEMBER)) {
                result = client.set(dataCommandsParam);
            } else if (command.startsWith(ZCARD) || command.startsWith(ZSCORE)
                    || command.startsWith(ZCOUNT) || command.startsWith(ZRANGE)
                    || command.startsWith(ZADD)) {
                result = client.zset(dataCommandsParam);
            }
        } catch (Exception e) {
            logger.error("Redis operation failed, cluster name: " + cluster.getClusterName() + ", command: " + dataCommandsParam.getCommand(), e);
        }
        return result;
    }

    @Override
    public boolean clusterForget(Cluster cluster, RedisNode forgetNode) {
        List<RedisNode> nodeList = getRedisNodeList(cluster);
        String clusterName = cluster.getClusterName();
        if (nodeList == null || nodeList.isEmpty()) {
            return false;
        }
        String redisPassword = cluster.getRedisPassword();
        String forgetNodeId = forgetNode.getNodeId();
        for (RedisNode redisNode : nodeList) {
            String nodeId = redisNode.getNodeId();
            if (Objects.equals(nodeId, forgetNodeId)) {
                continue;
            }
            try {
                RedisURI redisURI = new RedisURI(redisNode.getHost(), redisNode.getPort(), redisPassword);
                RedisClient redisClient = RedisClientFactory.buildRedisClient(redisURI);
                redisClient.clusterForget(forgetNodeId);
                redisClient.close();
            } catch (Exception e) {
                logger.error("Forget cluster node failed, cluster name: " + clusterName + ", bad node: " + redisNode.getHost() + ":" + redisNode.getPort(), e);
            }
        }
        try {
            RedisURI redisURI = new RedisURI(forgetNode.getHost(), forgetNode.getPort(), redisPassword);
            RedisClient redisClient = RedisClientFactory.buildRedisClient(redisURI);
            // Forget itself
            redisClient.clusterReset(ClusterReset.HARD);
            redisClient.close();
            return true;
        } catch (Exception e) {
            logger.error("Forget itself failed, cluster name: " + clusterName + ", " + forgetNode.getHost() + ":" + forgetNode.getPort(), e);
            return false;
        }
    }

    @Override
    public String clusterReplicate(Cluster cluster, RedisNode masterNode, RedisNode slaveNode) {
        String newMasterId = masterNode.getMasterId();
        String redisPassword = cluster.getRedisPassword();
        if (Strings.isNullOrEmpty(newMasterId)) {
            return "master node id empty!";
        }
        try {
            RedisClient redisClient = RedisClientFactory.buildRedisClient(slaveNode, redisPassword);
            redisClient.clusterReplicate(newMasterId);
            redisClient.close();
            return null;
        } catch (Exception e) {
            String message = slaveNode.getHost() + ":" + slaveNode.getPort() + " replicate " + newMasterId + " failed.";
            logger.error(message, e);
            return message + e.getMessage();
        }
    }

    @Override
    public boolean clusterFailOver(Cluster cluster, RedisNode newMasterNode) {
        String redisPassword = cluster.getRedisPassword();
        try {
            RedisURI redisURI = new RedisURI(newMasterNode.getHost(), newMasterNode.getPort(), redisPassword);
            RedisClient redisClient = RedisClientFactory.buildRedisClient(redisURI);
            redisClient.clusterFailOver();
            redisClient.close();
            return true;
        } catch (Exception e) {
            logger.error(newMasterNode.getHost() + ":" + newMasterNode.getPort() + " fail over failed", e);
            return false;
        }
    }

    @Override
    public String clusterMeet(Cluster cluster, RedisNode firstNode, List<RedisNode> redisNodeList) {
        StringBuffer result = new StringBuffer();
        RedisClient redisClient;
        try {
            redisClient = RedisClientFactory.buildRedisClient(firstNode);
        } catch (Exception e) {
            String message = "Create redis client failed.";
            logger.error(message, e);
            result.append(message).append(e.getMessage());
            return result.toString();
        }
        for (RedisNode redisNode : redisNodeList) {
            String host = redisNode.getHost();
            int port = redisNode.getPort();
            try {
                redisClient.clusterMeet(host, port);
                redisClient.close();
            } catch (Exception e) {
                String message = "Cluster meet " + host + ":" + port + " failed.";
                logger.error(message, e);
                result.append(message).append(e.getMessage());
            }
        }
        return result.toString();
    }

    @Override
    public String clusterAddSlots(Cluster cluster, RedisNode masterNode, SlotBalanceUtil.Shade shade) {
        int start = shade.getStartSlot();
        int end = shade.getEndSlot();
        int[] slots = new int[end - start];
        for (int i = start, j = 0; i <= end; i++, j++) {
            slots[j] = i;
        }
        try {
            RedisClient redisClient = RedisClientFactory.buildRedisClient(masterNode, cluster.getRedisPassword());
            String result = redisClient.clusterAddSlots(slots);
            return Objects.equals(result, OK) ? result : null;
        } catch (Exception e) {
            logger.error(cluster.getClusterName() + " add slots error, " + shade, e);
            return e.getMessage();
        }
    }

    @Override
    public String clusterAddSlotsBatch(Cluster cluster, Map<RedisNode, SlotBalanceUtil.Shade> masterNodeAndShade) {
        StringBuffer results = new StringBuffer();
        masterNodeAndShade.forEach((masterNode, shade) -> {
            String result = clusterAddSlots(cluster, masterNode, shade);
            if (!Strings.isNullOrEmpty(result)) {
                results.append(result).append("\n");
            }
        });
        return results.toString();
    }

    @Override
    public void clusterMoveSlots(Cluster cluster, RedisNode targetNode, SlotBalanceUtil.Shade shade) {
        String clusterName = cluster.getClusterName();
        String redisPassword = cluster.getRedisPassword();
        Map<RedisNode, List<SlotBalanceUtil.Shade>> masterNodeAndShadeMap = getSlotSpread(cluster);
        String targetNodeId = targetNode.getNodeId();
        // 目标节点
        RedisURI targetRedisURI = new RedisURI(targetNode.getHost(), targetNode.getPort(), cluster.getRedisPassword());

        for (int slot = shade.getStartSlot(); slot <= shade.getEndSlot(); slot++) {
            RedisClient targetRedisClient = RedisClientFactory.buildRedisClient(targetRedisURI);
            // 改槽位已被分配
            RedisNode masterNodeAssigned = getMasterNodeAssigned(masterNodeAndShadeMap, slot);
            if (masterNodeAssigned == null) {
                // 直接分配此 slot
                targetRedisClient.clusterAddSlots(slot);
                continue;
            } else {
                RedisClient sourceRedisClient = RedisClientFactory.buildRedisClient(masterNodeAssigned, redisPassword);
                try {
                    // 源节点导出槽道
                    sourceRedisClient.clusterSetSlotImporting(slot, targetNodeId);
                    // 目标节点导入槽道
                    targetRedisClient.clusterSetSlotMigrating(slot, masterNodeAssigned.getNodeId());
                    // 数据迁移
                    List<String> keyList;
                    do {
                        keyList = sourceRedisClient.clusterGetKeysInSlot(slot, 50);
                        String[] keys = keyList.toArray(new String[0]);
                        sourceRedisClient.migrate(masterNodeAssigned.getHost(), masterNodeAssigned.getPort(),
                                0, 100000, MigrateParams.migrateParams().replace(), keys);
                    } while (!keyList.isEmpty());
                } catch (Exception e) {
                    logger.error(clusterName + " move slot error.", e);
                    // 出现异常则恢复 slot
                    targetRedisClient.clusterSetSlotStable(slot);
                    sourceRedisClient.clusterSetSlotStable(slot);
                } finally {
                    // set slot to target node
                    targetRedisClient.clusterSetSlotNode(slot, targetNodeId);
                    //？ 这个很奇怪如果没有设置 stable 它的迁移状态会一直在的
                    //sourceRedisClient.clusterSetSlotStable(slot);
                    disseminate(masterNodeAndShadeMap.keySet(), redisPassword, slot);
                    sourceRedisClient.close();
                    targetRedisClient.close();
                }
            }
        }
    }

    /**
     * 所有主节点通知更新数据(槽道迁移)
     *
     * @param masterNodeList
     * @param requirePass
     * @param slot
     */
    private void disseminate(Set<RedisNode> masterNodeList, String requirePass, int slot) {
        for (RedisNode redisNode : masterNodeList) {
            RedisClient redisClient = RedisClientFactory.buildRedisClient(redisNode, requirePass);
            redisClient.clusterSetSlotNode(slot, redisNode.getNodeId());
            redisClient.close();
        }
    }

    /**
     * 获取所有节点的shade分布情况
     *
     * @param cluster
     * @return
     */
    private Map<RedisNode, List<SlotBalanceUtil.Shade>> getSlotSpread(Cluster cluster) {
        List<RedisNode> redisMasterNodeList = getRedisMasterNodeList(cluster);
        Map<RedisNode, List<SlotBalanceUtil.Shade>> nodeIdAndShadeMap = new HashMap<>(redisMasterNodeList.size() - 1);
        redisMasterNodeList.forEach(masterNode -> {
            String slotRanges = masterNode.getSlotRange();
            String[] slotRangeArr = SignUtil.splitByCommas(slotRanges);
            int length = slotRangeArr.length;
            List<SlotBalanceUtil.Shade> shadeList = new ArrayList<>(length);
            if (length == 1) {
                shadeList.add(new SlotBalanceUtil.Shade(slotRanges));
                nodeIdAndShadeMap.put(masterNode, shadeList);
            } else if (length > 1) {
                for (String slotRange : slotRangeArr) {
                    shadeList.add(new SlotBalanceUtil.Shade(slotRange));
                }
                nodeIdAndShadeMap.put(masterNode, shadeList);
            } else {
                nodeIdAndShadeMap.put(masterNode, null);
            }
        });
        return nodeIdAndShadeMap;
    }

    private RedisNode getMasterNodeAssigned(Map<RedisNode, List<SlotBalanceUtil.Shade>> nodeIdAndShadeMap, int slot) {
        for (Map.Entry<RedisNode, List<SlotBalanceUtil.Shade>> nodeIdAndShade : nodeIdAndShadeMap.entrySet()) {
            RedisNode redisNode = nodeIdAndShade.getKey();
            List<SlotBalanceUtil.Shade> shadeList = nodeIdAndShade.getValue();
            // 判断 i 是否在在此master上
            for (SlotBalanceUtil.Shade startEnd : shadeList) {
                if (startEnd == null) {
                    continue;
                }
                int slotCount = startEnd.getSlotCount();
                int start = startEnd.getStartSlot();
                int end = startEnd.getEndSlot();
                if (slotCount > 0 && (slot >= start || slot <= end)) {
                    return redisNode;
                }
            }
        }
        return null;
    }

    @Override
    public boolean standaloneReplicaOf(Cluster cluster, RedisNode masterNode, RedisNode redisNode) {
        String clusterName = cluster.getClusterName();
        String masterHost = masterNode.getHost();
        int masterPort = masterNode.getPort();
        try {
            RedisClient redisClient = RedisClientFactory.buildRedisClient(redisNode, cluster.getRedisPassword());
            redisClient.replicaOf(masterHost, masterPort);
            redisClient.close();
            redisClient.replicaOf(masterHost, masterPort);
            return true;
        } catch (Exception e) {
            logger.error(redisNode.getHost() + ":" + redisNode.getPort() + " replica of " + masterHost + ":" + masterPort + " failed, cluster name: " + clusterName, e);
            return false;
        }
    }

    @Override
    public boolean standaloneReplicaNoOne(Cluster cluster, RedisNode redisNode) {
        String clusterName = cluster.getClusterName();
        try {
            RedisClient redisClient = RedisClientFactory.buildRedisClient(redisNode, cluster.getRedisPassword());
            redisClient.replicaNoOne();
            return true;
        } catch (Exception e) {
            logger.error(redisNode.getHost() + ":" + redisNode.getPort() + " replica no one failed, cluster name: " + clusterName, e);
            return false;
        }
    }

    @Override
    public Map<String, String> getConfig(Cluster cluster, RedisNode redisNode) {
        String clusterName = cluster.getClusterName();
        String redisPassword = cluster.getRedisPassword();
        RedisClient redisClient = null;
        try {
            redisClient = RedisClientFactory.buildRedisClient(redisNode, redisPassword);
            return redisClient.getConfig();
        } catch (Exception e) {
            logger.error(clusterName + " get config failed.", e);
            return null;
        } finally {
            close(redisClient);
        }
    }

    @Override
    public boolean setConfigBatch(Cluster cluster, Pair<String, String> config) {
        List<RedisNode> redisNodeList = getRedisNodeList(cluster);
        redisNodeList.forEach(redisNode -> setConfig(cluster, redisNode, config));
        return true;
    }

    @Override

    public boolean setConfig(Cluster cluster, RedisNode redisNode, Pair<String, String> config) {
        String clusterName = cluster.getClusterName();
        String redisPassword = cluster.getRedisPassword();
        RedisClient redisClient = null;
        try {
            // TODO: special config, like `save`
            redisClient = RedisClientFactory.buildRedisClient(redisNode, redisPassword);
            redisClient.rewriteConfig();
            return redisClient.setConfig(config);
        } catch (Exception e) {
            logger.error(clusterName + " change config failed, config: " + config, e);
            return false;
        } finally {
            close(redisClient);
        }
    }

    @Override
    public void autoGenerateConfigFile(Cluster cluster) {

    }

    @Override
    public String updateRedisPassword(Cluster cluster) {
        String redisPassword = cluster.getRedisPassword();
        StringBuffer result = new StringBuffer();
        if (!Strings.isNullOrEmpty(redisPassword)) {
            List<RedisNode> redisNodeList = getRedisNodeList(cluster);

            redisNodeList.forEach(redisNode -> {
                try {
                    RedisClient redisClient = RedisClientFactory.buildRedisClient(redisNode);
                    redisClient.setConfig(new Pair<>(MASTER_AUTH, redisPassword));
                    redisClient.setConfig(new Pair<>(REQUIRE_PASS, redisPassword));
                    redisClient.rewriteConfig();
                    redisClient.close();
                } catch (Exception e) {
                    String message = "Update redis password failed, host=" + redisNode.getHost() + ", port=" + redisNode.getPort() + ".";
                    logger.error(message, e);
                    result.append(message + e.getMessage());
                }
            });
        }
        return result.toString();
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

    private void close(IRedisClient redisClient) {
        if (redisClient != null) {
            redisClient.close();
        }
    }


}
