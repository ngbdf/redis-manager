package com.newegg.ec.redis.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.client.*;
import com.newegg.ec.redis.controller.websocket.InstallationWebSocketHandler;
import com.newegg.ec.redis.entity.*;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.INodeInfoService;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.ClusterReset;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.params.MigrateParams;
import redis.clients.jedis.util.Slowlog;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static com.newegg.ec.redis.client.IDatabaseCommand.*;
import static com.newegg.ec.redis.util.RedisClusterInfoUtil.OK;
import static com.newegg.ec.redis.util.RedisConfigUtil.PORT;
import static com.newegg.ec.redis.util.RedisConfigUtil.*;
import static com.newegg.ec.redis.util.RedisNodeInfoUtil.*;
import static com.newegg.ec.redis.util.RedisUtil.*;
import static javax.management.timer.Timer.ONE_SECOND;

/**
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
@Service
public class RedisService implements IRedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    private INodeInfoService nodeInfoService;

    @Autowired
    private IClusterService clusterService;

    @Value("${redis-manager.monitor.slow-log-limit:100}")
    private int slowLogLimit;

    @Override
    public Map<String, String> getNodeInfo(HostAndPort hostAndPort, String redisPassword) {
        RedisClient redisClient = null;
        try {
            redisClient = RedisClientFactory.buildRedisClient(hostAndPort, redisPassword);
            return redisClient.getInfo();
        } catch (Exception e) {
            logger.error("Get redis node info failed, " + hostAndPort, e);
            return null;
        } finally {
            close(redisClient);
        }
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
            RedisClient redisClient = null;
            try {
                redisClient = RedisClientFactory.buildRedisClient(redisNode, cluster.getRedisPassword());
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
            } finally {
                close(redisClient);
            }
        });
        return keyspaceInfoMap;
    }

    @Override
    public Map<String, Long> getTotalMemoryInfo(Cluster cluster) {
        List<RedisNode> redisMasterNodeList = getRedisMasterNodeList(cluster);
        Map<String, Long> totalMemoryInfo = new HashMap<>();
        AtomicLong totalUsedMemory = new AtomicLong(0);
        redisMasterNodeList.forEach(redisNode -> {
            RedisClient redisClient = null;
            try {
                redisClient = RedisClientFactory.buildRedisClient(redisNode, cluster.getRedisPassword());
                Map<String, String> memoryInfo = redisClient.getInfo(RedisClient.MEMORY);
                if (memoryInfo.isEmpty()) {
                    return;
                }
                memoryInfo.forEach((key, val) -> {
                    if (Objects.equals(USED_MEMORY, key)) {
                        totalUsedMemory.addAndGet(byteToMB(val));
                    }
                });
            } catch (Exception e) {
                logger.error("Get keyspace info failed, redis node = " + redisNode.getHost() + ":" + redisNode.getPort(), e);
            } finally {
                close(redisClient);
            }
        });
        totalMemoryInfo.put(USED_MEMORY, totalUsedMemory.get());
        return totalMemoryInfo;
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
        RedisClient redisClient = null;
        try {
            redisClient = RedisClientFactory.buildRedisClient(redisURI);
            if (STANDALONE.equalsIgnoreCase(redisMode)) {
                nodeList = redisClient.nodes();
            } else if (CLUSTER.equalsIgnoreCase(redisMode)) {
                nodeList = redisClient.clusterNodes();
            } else if (SENTINEL.equalsIgnoreCase(redisMode)) {
                nodeList = redisClient.sentinelNodes(nodesToHostAndPortSet(cluster.getNodes()));
            }
        } catch (Exception e) {
            logger.error("Get redis node list failed, " + cluster.getClusterName(), e);
        } finally {
            close(redisClient);
        }
        nodeList.forEach(redisNode -> redisNode.setClusterId(cluster.getClusterId()));
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
        RedisClient redisClient = null;
        try {
            String redisPassword = cluster.getRedisPassword();
            Set<HostAndPort> hostAndPortSet = nodesToHostAndPortSet(cluster.getNodes());
            RedisURI redisURI = new RedisURI(hostAndPortSet, redisPassword);
            redisClient = RedisClientFactory.buildRedisClient(redisURI);
            return redisClient.getClusterInfo();
        } catch (Exception e) {
            logger.error("Get cluster info failed, cluster name = " + cluster.getClusterName(), e);
            return null;
        } finally {
            close(redisClient);
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
            HostAndPort hostAndPort = new HostAndPort(redisNode.getHost(), redisNode.getPort());
            RedisClient redisClient = null;
            try {
                redisClient = RedisClientFactory.buildRedisClient(redisNode, cluster.getRedisPassword());
                List<Slowlog> slowLogs = redisClient.getSlowLog(slowLogLimit);
                for (Slowlog slowLog : slowLogs) {
                    RedisSlowLog redisSlowLog = new RedisSlowLog(hostAndPort, slowLog);
                    redisSlowLogList.add(redisSlowLog);
                }
            } catch (Exception e) {
                logger.error("Get " + hostAndPort + " slow log failed, cluster name: " + cluster.getClusterName(), e);
            } finally {
                close(redisClient);
            }
        }
        return redisSlowLogList;
    }

    /*private Set<HostAndPort> getHostAndPortSet(Cluster cluster) {
        List<RedisNode> redisNodeList = getRedisNodeList(cluster);
        Set<HostAndPort> hostAndPortSet = new HashSet<>();
        for (RedisNode redisNode : redisNodeList) {
            hostAndPortSet.add(new HostAndPort(redisNode.getHost(), redisNode.getPort()));
        }
        return hostAndPortSet;
    }*/

    @Override
    public Set<String> scan(Cluster cluster, AutoCommandParam autoCommandParam) {
        List<RedisNode> redisMasterNodeList = getRedisMasterNodeList(cluster);
        int masterSize = redisMasterNodeList.size();
        int count = masterSize < 10 ? 100 / masterSize : 10;
        autoCommandParam.setCount(count);
        Set<String> result = new LinkedHashSet<>();
        redisMasterNodeList.forEach(masterNode -> {
            RedisClient redisClient = null;
            try {
                redisClient = RedisClientFactory.buildRedisClient(masterNode, cluster.getRedisPassword());
                Set<String> scanResult = redisClient.scan(autoCommandParam);
                result.addAll(scanResult);
            } catch (Exception e) {
                logger.error("Scan redis failed, node = " + masterNode.getHost() + ":" + masterNode.getPort(), e);
            } finally {
                close(redisClient);
            }
        });
        return result;
    }

    @Override
    public AutoCommandResult query(Cluster cluster, AutoCommandParam autoCommandParam) {
        // standalone or cluster
        IDatabaseCommand client = null;
        try {
            client = buildDatabaseCommandClient(cluster);
            return client.query(autoCommandParam);
        } catch (Exception e) {
            logger.error("Auto query failed, cluster name = " + cluster.getClusterName(), e);
            return null;
        } finally {
            String redisMode = cluster.getRedisMode();
            if (Objects.equals(redisMode, CLUSTER)) {
                IRedisClusterClient redisClusterClient = (IRedisClusterClient) client;
                if (redisClusterClient != null) {
                    redisClusterClient.close();
                }
            } else {
                close((IRedisClient) client);
            }
        }
    }

    @Override
    public Object console(Cluster cluster, DataCommandsParam dataCommandsParam) {
        Object result = null;
        IDatabaseCommand client = null;
        try {
            client = buildDatabaseCommandClient(cluster);
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
            } else if (command.startsWith(TYPE)) {
                result = client.type(dataCommandsParam);
            } else if (command.startsWith(DEL)) {
                result = client.del(dataCommandsParam);
            }
        } catch (Exception e) {
            logger.error("Redis operation failed, cluster name: " + cluster.getClusterName() + ", command: " + dataCommandsParam.getCommand(), e);
        } finally {
            String redisMode = cluster.getRedisMode();
            if (Objects.equals(redisMode, CLUSTER)) {
                IRedisClusterClient redisClusterClient = (IRedisClusterClient) client;
                if (redisClusterClient != null) {
                    redisClusterClient.close();
                }
            } else {
                close((IRedisClient) client);
            }
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
            RedisClient redisClient = null;
            try {
                redisClient = RedisClientFactory.buildRedisClient(redisNode, redisPassword);
                redisClient.clusterForget(forgetNodeId);
            } catch (Exception e) {
                logger.error("Forget cluster node failed, cluster name: " + clusterName + ", bad node: " + redisNode.getHost() + ":" + redisNode.getPort(), e);
            } finally {
                close(redisClient);
            }
        }
        RedisClient redisClient = null;
        try {
            redisClient = RedisClientFactory.buildRedisClient(forgetNode, redisPassword);
            // Forget itself
            redisClient.clusterReset(ClusterReset.HARD);
            return true;
        } catch (Exception e) {
            logger.error("Forget itself failed, cluster name: " + clusterName + ", " + forgetNode.getHost() + ":" + forgetNode.getPort(), e);
            return false;
        } finally {
            close(redisClient);
        }
    }

    @Override
    public boolean clusterReplicate(Cluster cluster, String masterId, RedisNode slaveNode) {
        String redisPassword = cluster.getRedisPassword();
        if (Strings.isNullOrEmpty(masterId)) {
            return false;
        }
        RedisClient redisClient = null;
        try {
            redisClient = RedisClientFactory.buildRedisClient(slaveNode, redisPassword);
            return redisClient.clusterReplicate(masterId);
        } catch (Exception e) {
            logger.error(slaveNode.getHost() + ":" + slaveNode.getPort() + " replicate " + masterId + " failed.", e);
            return false;
        } finally {
            close(redisClient);
        }
    }

    @Override
    public boolean clusterFailOver(Cluster cluster, RedisNode newMasterNode) {
        String redisPassword = cluster.getRedisPassword();
        RedisClient redisClient = null;
        try {
            RedisURI redisURI = new RedisURI(newMasterNode.getHost(), newMasterNode.getPort(), redisPassword);
            redisClient = RedisClientFactory.buildRedisClient(redisURI);
            redisClient.clusterFailOver();
            return true;
        } catch (Exception e) {
            logger.error(newMasterNode.getHost() + ":" + newMasterNode.getPort() + " fail over failed", e);
            return false;
        } finally {
            close(redisClient);
        }
    }

    @Override
    public String clusterMeet(Cluster cluster, RedisNode seed, List<RedisNode> redisNodeList) {
        StringBuilder result = new StringBuilder();
        try {
            for (RedisNode redisNode : redisNodeList) {
                if (RedisUtil.equals(seed, redisNode)) {
                    continue;
                }
                RedisClient redisClient = null;
                String host = redisNode.getHost();
                int port = redisNode.getPort();
                try {
                    redisClient = RedisClientFactory.buildRedisClient(seed, cluster.getRedisPassword());
                    redisClient.clusterMeet(host, port);
                    Thread.sleep(ONE_SECOND);
                } catch (Exception e) {
                    String message = "Cluster meet " + host + ":" + port + " failed.";
                    logger.error(message, e);
                    result.append(message).append(e.getMessage()).append(SignUtil.COMMAS);
                } finally {
                    close(redisClient);
                }
            }
        } catch (Exception e) {
            String message = "Create redis client failed.";
            logger.error(message, e);
            result.append(message).append(e.getMessage());
        }
        return result.toString();
    }

    @Override
    public String clusterMeet(Cluster cluster, RedisNode seed, RedisNode redisNode) {
        List<RedisNode> redisNodeList = new ArrayList<>();
        redisNodeList.add(redisNode);
        return clusterMeet(cluster, seed, redisNodeList);
    }

    @Override
    public String clusterAddSlots(Cluster cluster, RedisNode masterNode, SlotBalanceUtil.Shade shade) {
        int start = shade.getStartSlot();
        int end = shade.getEndSlot();
        int[] slots = new int[end - start + 1];
        for (int i = start, j = 0; i <= end; i++, j++) {
            slots[j] = i;
        }
        RedisClient redisClient = null;
        try {
            redisClient = RedisClientFactory.buildRedisClient(masterNode, cluster.getRedisPassword());
            String result = redisClient.clusterAddSlots(slots);
            return Objects.equals(result, OK) ? result : null;
        } catch (Exception e) {
            logger.error(cluster.getClusterName() + " add slots error, " + shade, e);
            return e.getMessage();
        } finally {
            close(redisClient);
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
    public String initSlots(Cluster cluster) {
        List<RedisNode> masterNodeList = getRedisMasterNodeList(cluster);
        List<SlotBalanceUtil.Shade> balanceSlots = SlotBalanceUtil.balanceSlot(masterNodeList.size());
        Map<RedisNode, SlotBalanceUtil.Shade> masterNodeShadeMap = new LinkedHashMap<>();
        for (int i = 0; i < masterNodeList.size(); i++) {
            RedisNode redisNode = masterNodeList.get(i);
            SlotBalanceUtil.Shade shade = balanceSlots.get(i);
            masterNodeShadeMap.put(redisNode, shade);
        }
        return clusterAddSlotsBatch(cluster, masterNodeShadeMap);
    }

    @Override
    public boolean clusterMoveSlots(Cluster cluster, RedisNode targetNode, SlotBalanceUtil.Shade shade) {
        boolean result = true;
        String clusterName = cluster.getClusterName();
        String redisPassword = cluster.getRedisPassword();
        Map<RedisNode, List<SlotBalanceUtil.Shade>> masterNodeAndShadeMap = getSlotSpread(cluster);
        String targetNodeId = targetNode.getNodeId();
        // 目标节点
        RedisURI targetRedisURI = new RedisURI(targetNode, cluster.getRedisPassword());
        for (int slot = shade.getStartSlot(); slot <= shade.getEndSlot(); slot++) {
            RedisClient targetRedisClient = RedisClientFactory.buildRedisClient(targetRedisURI);

            RedisNode sourceNode = getMasterNodeAssigned(masterNodeAndShadeMap, slot);
            // 槽位没有被分配
            if (sourceNode == null) {
                // 直接分配此 slot
                try {
                    targetRedisClient.clusterAddSlots(slot);
                } catch (Exception e) {
                    logger.error("Assigned slot failed.", e);
                } finally {
                    close(targetRedisClient);
                }
                continue;
            }
            // 如果此 slot 就在它自己本身，则直接跳过
            if (RedisUtil.equals(sourceNode, targetNode)) {
                continue;
            }
            // 迁移槽
            RedisClient sourceRedisClient = RedisClientFactory.buildRedisClient(sourceNode, redisPassword);
            try {
                // 目标节点导入槽道
                targetRedisClient.clusterSetSlotImporting(slot, sourceNode.getNodeId());
                // 源节点导出槽道
                sourceRedisClient.clusterSetSlotMigrating(slot, targetNodeId);
                // 数据迁移
                List<String> keyList;
                do {
                    keyList = sourceRedisClient.clusterGetKeysInSlot(slot, 50);
                    String[] keys = keyList.toArray(new String[0]);
                    sourceRedisClient.migrate(sourceNode.getHost(), sourceNode.getPort(),
                            0, 100000, MigrateParams.migrateParams().replace(), keys);
                } while (!keyList.isEmpty());
            } catch (Exception e) {
                logger.error(clusterName + " move slot " + slot + "error.", e);
                result = false;
                // 出现异常则恢复 slot
                targetRedisClient.clusterSetSlotStable(slot);
                sourceRedisClient.clusterSetSlotStable(slot);
            } finally {
                // 所有主节点通知更新数据(槽道迁移)
                disseminate(masterNodeAndShadeMap.keySet(), redisPassword, slot, targetNodeId);
                close(sourceRedisClient);
                close(targetRedisClient);
            }
        }
        return result;
    }

    /**
     * 所有主节点通知更新数据(槽道迁移)
     *
     * @param masterNodeList
     * @param requirePass
     * @param slot
     */
    private void disseminate(Set<RedisNode> masterNodeList, String requirePass, int slot, String targetNodeId) {
        for (RedisNode redisNode : masterNodeList) {
            RedisClient redisClient = null;
            try {
                redisClient = RedisClientFactory.buildRedisClient(redisNode, requirePass);
                redisClient.clusterSetSlotNode(slot, targetNodeId);
            } catch (Exception e) {
                logger.error("Redis disseminate failed, node = " + RedisUtil.getNodeString(redisNode), e);
            } finally {
                close(redisClient);
            }
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
            if (Strings.isNullOrEmpty(slotRanges)) {
                nodeIdAndShadeMap.put(masterNode, null);
            } else {
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
            }
        });
        return nodeIdAndShadeMap;
    }

    /**
     * 获取所要迁移的 slot 在哪个 master 上
     *
     * @param nodeIdAndShadeMap
     * @param slot
     * @return
     */
    private RedisNode getMasterNodeAssigned(Map<RedisNode, List<SlotBalanceUtil.Shade>> nodeIdAndShadeMap, int slot) {
        for (Map.Entry<RedisNode, List<SlotBalanceUtil.Shade>> nodeIdAndShade : nodeIdAndShadeMap.entrySet()) {
            RedisNode redisNode = nodeIdAndShade.getKey();
            List<SlotBalanceUtil.Shade> shadeList = nodeIdAndShade.getValue();
            // master no slot
            if (shadeList == null || shadeList.isEmpty()) {
                return null;
            }
            // 判断 i 是否在在此master上
            for (SlotBalanceUtil.Shade startEnd : shadeList) {
                if (startEnd == null) {
                    continue;
                }
                int slotCount = startEnd.getSlotCount();
                int start = startEnd.getStartSlot();
                int end = startEnd.getEndSlot();
                if (slotCount > 0 && (slot >= start && slot <= end)) {
                    return redisNode;
                }
            }
        }
        return null;
    }

    @Override
    public String standaloneReplicaOf(Cluster cluster, RedisNode masterNode, RedisNode redisNode) {
        String clusterName = cluster.getClusterName();
        String masterHost = masterNode.getHost();
        int masterPort = masterNode.getPort();
        StringBuilder result = new StringBuilder();
        RedisClient redisClient = null;
        try {
            redisClient = RedisClientFactory.buildRedisClient(redisNode, cluster.getRedisPassword());
            redisClient.replicaOf(masterHost, masterPort);
            return result.toString();
        } catch (Exception e) {
            String template = "%s:%d replica of %s:%d failed, cluster name: %s";
            result.append(String.format(template, redisNode.getHost(), redisNode.getPort(), masterHost, masterPort, clusterName));
            InstallationWebSocketHandler.appendLog(clusterName, result.toString());
            InstallationWebSocketHandler.appendLog(clusterName, e.getMessage());
            logger.error(result.toString(), e);
            return result.toString();
        } finally {
            close(redisClient);
        }
    }

    @Override
    public boolean standaloneReplicaNoOne(Cluster cluster, RedisNode redisNode) {
        String clusterName = cluster.getClusterName();
        RedisClient redisClient = null;
        try {
            redisClient = RedisClientFactory.buildRedisClient(redisNode, cluster.getRedisPassword());
            redisClient.replicaNoOne();
            return true;
        } catch (Exception e) {
            logger.error(redisNode.getHost() + ":" + redisNode.getPort() + " replica no one failed, cluster name: " + clusterName, e);
            return false;
        } finally {
            close(redisClient);
        }
    }

    @Override
    public Map<String, String> getConfig(RedisNode redisNode, String redisPassword, String pattern) {
        RedisClient redisClient = null;
        try {
            if (Strings.isNullOrEmpty(pattern)) {
                pattern = "*";
            }
            redisClient = RedisClientFactory.buildRedisClient(redisNode, redisPassword);
            return redisClient.getConfig(pattern);
        } catch (Exception e) {
            logger.error(RedisUtil.getNodeString(redisNode) + " get config failed.", e);
            return null;
        } finally {
            close(redisClient);
        }
    }

    @Override
    public boolean setConfigBatch(Cluster cluster, RedisConfigUtil.RedisConfig redisConfig) {
        List<RedisNode> redisNodeList = getRedisNodeList(cluster);
        AtomicBoolean result = new AtomicBoolean(true);
        for (RedisNode redisNode : redisNodeList) {
            result.set(setConfig(cluster, redisNode, redisConfig));
        }
        return result.get();
    }

    @Override
    public boolean setConfig(Cluster cluster, RedisNode redisNode, RedisConfigUtil.RedisConfig redisConfig) {
        String redisPassword = cluster.getRedisPassword();
        RedisClient redisClient = null;
        try {
            String configKey = redisConfig.getConfigKey();
            // 不允许更新以下配置
            if (Objects.equals(configKey, REQUIRE_PASS)
                    || Objects.equals(configKey, MASTER_AUTH)
                    || Objects.equals(configKey, BIND)
                    || Objects.equals(configKey, PORT)
                    || Objects.equals(configKey, DIR)
                    || Objects.equals(configKey, DAEMONIZE)) {
                return true;
            }
            String configValue = redisConfig.getConfigValue();
            // for special config, like `save`
            if (Strings.isNullOrEmpty(configValue)) {
                configValue = "";
            }
            redisClient = RedisClientFactory.buildRedisClient(redisNode, redisPassword);
            redisClient.setConfig(configKey, configValue);
            if (Objects.equals(cluster.getRedisMode(), CLUSTER)) {
                redisClient.clusterSaveConfig();
            }
            redisClient.rewriteConfig();
            return true;
        } catch (Exception e) {
            logger.error(redisNode.getHost() + ":" + redisNode.getPort() + " change config failed, " + redisConfig, e);
            return false;
        } finally {
            close(redisClient);
        }
    }

    @Override
    public void autoGenerateConfigFile(Cluster cluster) {

    }

    @Override
    public List<SentinelMaster> getSentinelMasters(Cluster cluster) {
        List<SentinelMaster> sentinelMasterList = new LinkedList<>();
        try {
            Set<HostAndPort> hostAndPorts = nodesToHostAndPortSet(cluster.getNodes());
            RedisClient redisClient = RedisClientFactory.buildRedisClient(hostAndPorts);
            List<Map<String, String>> sentinelMasters = redisClient.getSentinelMasters();
            Map<String, String> info = redisClient.getInfo(SENTINEL);
            Map<String, String> nameAndStatus = new HashMap<>();
            for (String value : info.values()) {
                String[] keyValues = SignUtil.splitByCommas(value);
                if (keyValues.length == 1) {
                    continue;
                }
                String name = SignUtil.splitByEqualSign(keyValues[0])[1];
                String status = SignUtil.splitByEqualSign(keyValues[1])[1];
                nameAndStatus.put(name, status);
            }

            for (Map<String, String> master : sentinelMasters) {
                SentinelMaster sentinelMaster = new SentinelMaster();
                sentinelMaster.setFlags(master.get("flags"));
                sentinelMaster.setNumSlaves(Integer.parseInt(master.get("num-slaves")));
                sentinelMaster.setMasterHost(master.get("ip"));
                sentinelMaster.setMasterPort(Integer.parseInt(master.get("port")));
                sentinelMaster.setMasterName(master.get("name"));
                sentinelMaster.setQuorum(Integer.parseInt(master.get("quorum")));
                sentinelMaster.setDownAfterMilliseconds(Long.parseLong(master.get("down-after-milliseconds")));
                sentinelMaster.setFailoverTimeout(Long.parseLong(master.get("failover-timeout")));
                sentinelMaster.setParallelSync(Integer.parseInt(master.get("parallel-syncs")));
                sentinelMaster.setSentinels(Integer.parseInt(master.get("num-other-sentinels")) + 1);
                String status = nameAndStatus.get(master.get("name"));
                sentinelMaster.setStatus(status);
                sentinelMasterList.add(sentinelMaster);
            }
        } catch (Exception e) {
            logger.error("Add sentinel master host and port failed, " + cluster.getClusterName(), e);
        }
        return sentinelMasterList;
    }

    private IDatabaseCommand buildDatabaseCommandClient(Cluster cluster) {
        IDatabaseCommand client = null;
        String redisMode = cluster.getRedisMode();
        String redisPassword = cluster.getRedisPassword();
        List<RedisNode> masterNodeList = getRedisMasterNodeList(cluster);
        RedisNode redisNode = masterNodeList.get(0);
        if (STANDALONE.equalsIgnoreCase(redisMode)) {
            RedisURI redisURI = new RedisURI(redisNode, redisPassword);
            client = RedisClientFactory.buildRedisClient(redisURI);
        } else if (CLUSTER.equalsIgnoreCase(redisMode)) {
            client = RedisClientFactory.buildRedisClusterClient(redisNode, redisPassword);
        }
        return client;
    }

    private void close(IRedisClient redisClient) {
        if (redisClient != null) {
            redisClient.close();
        }
    }

    //***********************sentinel
    @Override
    public boolean setSentinelConfig(SentinelMaster sentinelMaster, SentinelConfigUtil.SentinelConfig redisConfig) {
        RedisClient redisClient = null;
        try {
            String masterName = sentinelMaster.getMasterName();
            String configKey = redisConfig.getConfigKey();
            String configValue = redisConfig.getConfigValue();
            if (Strings.isNullOrEmpty(configValue)) {
                configValue = "";
            }
            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put(configKey, configValue);
            redisClient = RedisClientFactory.buildRedisClient(new HostAndPort(sentinelMaster.getMasterHost(), sentinelMaster.getMasterPort()));
            redisClient.setConfig(masterName, parameterMap);
            return true;
        } catch (Exception e) {
            logger.error(" change config failed, ", e);
            return false;
        } finally {
            close(redisClient);
        }

    }

    @Override
    public Map<String, String> getSentinelConfig(SentinelMaster sentinelMaster) {
        RedisClient redisClient = null;
        try {
            redisClient = RedisClientFactory.buildRedisClient(new HostAndPort(sentinelMaster.getMasterHost(), sentinelMaster.getMasterPort()));
            return redisClient.getInfo();
        } catch (Exception e) {
            logger.error(" get config failed.", e);
            return null;
        } finally {
            close(redisClient);
        }

    }

    @Override
    public List<String> getMasterAddrByName(SentinelMaster sentinelMaster) {
        RedisClient redisClient = null;
        try {
            String masterName = sentinelMaster.getMasterName();
            redisClient = RedisClientFactory.buildRedisClient(new HostAndPort(sentinelMaster.getMasterHost(), sentinelMaster.getMasterPort()));
            return redisClient.getMasterAddrByName(masterName);
        } catch (Exception e) {
            logger.error(" get config failed.", e);
            return null;
        } finally {
            close(redisClient);
        }

    }

    @Override
    public boolean failOverMaster(SentinelMaster sentinelMaster) {
        RedisClient redisClient = null;
        try {
            String masterName = sentinelMaster.getMasterName();
            redisClient = RedisClientFactory.buildRedisClient(new HostAndPort(sentinelMaster.getMasterHost(), sentinelMaster.getMasterPort()));
            redisClient.failoverMaster(masterName);
            return true;
        } catch (Exception e) {
            logger.error(" failover failed.", e);
            return false;
        } finally {
            close(redisClient);
        }
    }

    @Override
    public List<Map<String, String>> getMastersInfo(SentinelMaster sentinelMaster) {
        RedisClient redisClient = null;
        try {
            redisClient = RedisClientFactory.buildRedisClient(new HostAndPort(sentinelMaster.getMasterHost(), sentinelMaster.getMasterPort()));
            return redisClient.getSentinelMasters();
        } catch (Exception e) {
            logger.error(" get masters info failed.", e);
            return null;
        } finally {
            close(redisClient);
        }
    }

    @Override
    public boolean getMasterState(SentinelMaster sentinelMaster) {
        RedisClient redisClient = null;
        try {
            String masterName = sentinelMaster.getMasterName();
            String ip = sentinelMaster.getMasterHost();
            int port = sentinelMaster.getMasterPort();
            int quorum = sentinelMaster.getQuorum();
            redisClient = RedisClientFactory.buildRedisClient(new HostAndPort(sentinelMaster.getMasterHost(), sentinelMaster.getMasterPort()));
            redisClient.monitorMaster(masterName, ip, port, quorum);
        } catch (Exception e) {
            logger.error(" get masters info failed.", e);
        } finally {
            close(redisClient);
        }
        return false;
    }


    private void close(RedisClient redisClient) {
        if (redisClient != null) {
            redisClient.close();
        }
    }

}
