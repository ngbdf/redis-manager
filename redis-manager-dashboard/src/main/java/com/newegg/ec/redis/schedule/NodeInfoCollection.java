package com.newegg.ec.redis.schedule;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.CaseFormat;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.newegg.ec.redis.entity.*;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.INodeInfoService;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import redis.clients.jedis.HostAndPort;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.newegg.ec.redis.util.RedisNodeInfoUtil.*;
import static com.newegg.ec.redis.util.RedisUtil.CLUSTER;
import static com.newegg.ec.redis.util.RedisUtil.STANDALONE;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public class NodeInfoCollection implements IDataCollection, IDataCalculate, IDataCleanup, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(NodeInfoCollection.class);

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private IRedisService redisService;

    @Autowired
    private INodeInfoService nodeInfoService;

    private int coreSize;

    private static ExecutorService threadPool;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        coreSize = Runtime.getRuntime().availableProcessors();
        threadPool = new ThreadPoolExecutor(coreSize, coreSize * 4, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("collect-node-info-pool-thread-%d").build(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 一分钟收集一次RedisNode数据，并计算以 MINUTE 为单位的 avg, max, min
     */
    @Async
    @Scheduled(cron = "0 0/1 * * * ? ")
    @Override
    public void collectData() {
        try {
            List<Cluster> allClusterList = clusterService.getAllClusterList();
            if (allClusterList == null || allClusterList.isEmpty()) {
                return;
            }
            for (Cluster cluster : allClusterList) {
                threadPool.submit(new CollectMinuteNodeInfoTask(cluster, NodeInfoType.TimeType.MINUTE));
            }
        } catch (Exception e) {
            logger.error("Collect node info data failed.", e);
        }
    }

    /**
     * 一个小时跑一次，获取DB数据，计算所有节点以 HOUR 为单位的 avg, max, min
     */
    @Async
    @Scheduled(cron = "0 0 0/1 * * ? ")
    @Override
    public void calculate() {
        try {
            List<Cluster> allClusterList = clusterService.getAllClusterList();
            if (allClusterList == null || allClusterList.isEmpty()) {
                return;
            }
            for (Cluster cluster : allClusterList) {
                threadPool.submit(new CollectMinuteNodeInfoTask(cluster, NodeInfoType.TimeType.HOUR));
            }
        } catch (Exception e) {
            logger.error("Collect node info data failed.", e);
        }
    }

    /**
     * 每周星期天凌晨1点实行一次，清理数据
     */
    @Async
    @Scheduled(cron = "0 0 1 ? * L")
    @Override
    public void cleanup() {
        try {
            List<Cluster> allClusterList = clusterService.getAllClusterList();
            if (allClusterList == null || allClusterList.isEmpty()) {
                return;
            }
            for (Cluster cluster : allClusterList) {
                nodeInfoService.cleanupNodeInfo(cluster.getClusterId());
            }
        } catch (Exception e) {
            logger.error("Cleanup node info schedule failed.", e);
        }
    }

    private class CollectMinuteNodeInfoTask implements Runnable {

        private Cluster cluster;

        private NodeInfoType.TimeType timeType;

        public CollectMinuteNodeInfoTask(Cluster cluster, NodeInfoType.TimeType timeType) {
            this.cluster = cluster;
            this.timeType = timeType;
        }

        @Override
        public void run() {
            try {
                int clusterId = cluster.getClusterId();
                String redisMode = cluster.getRedisMode();
                List<NodeInfo> nodeInfoList = redisService.getNodeInfoList(cluster, timeType);
                // 计算 AVG、MAX、MIN
                Map<String, List<BigDecimal>> nodeInfoDataMap = nodeInfoDataToMap(nodeInfoList);
                List<NodeInfo> specialNodeInfo = buildNodeInfoForSpecialDataType(nodeInfoDataMap);
                nodeInfoList.addAll(specialNodeInfo);
                // clean last time data and save new data to db
                NodeInfoParam nodeInfoParam = new NodeInfoParam(clusterId, timeType);
                nodeInfoService.addNodeInfo(nodeInfoParam, nodeInfoList);
                // 计算 total keys 和 total expires, 这里不适用 dbsize 命令
                long totalKeys = 0;
                long totalExpires = 0;
                if (STANDALONE.equalsIgnoreCase(redisMode)) {
                    for (NodeInfo nodeInfo : nodeInfoList) {
                        if (NodeRole.MASTER.equals(nodeInfo.getRole())) {
                            totalKeys = nodeInfo.getKeys();
                            totalExpires = nodeInfo.getExpires();
                        }
                    }
                } else if (CLUSTER.equalsIgnoreCase(redisMode)) {
                    for (NodeInfo nodeInfo : nodeInfoList) {
                        totalKeys += nodeInfo.getKeys();
                        totalExpires += nodeInfo.getExpires();
                    }
                }
                cluster.setTotalKeys(totalKeys);
                cluster.setTotalExpires(totalExpires);
                clusterService.updateClusterKeys(cluster);
            } catch (Exception e) {
                logger.error("Collect " + timeType + " data for " + cluster.getClusterName() + " failed.", e);
            }

        }
    }

    /**
     * Convert objects to a map
     *
     * @param nodeInfoList
     * @return
     */
    private Map<String, List<BigDecimal>> nodeInfoDataToMap(List<NodeInfo> nodeInfoList) {
        int size = nodeInfoList.size();
        Map<String, List<BigDecimal>> dataMap = new HashMap<>();
        List<BigDecimal> responseTime = new ArrayList<>(size);
        List<BigDecimal> connectedClients = new ArrayList<>(size);
        List<BigDecimal> clientLongestOutputList = new ArrayList<>(size);
        List<BigDecimal> clientBiggestInputBuf = new ArrayList<>(size);
        List<BigDecimal> blockedClients = new ArrayList<>(size);
        List<BigDecimal> usedMemory = new ArrayList<>(size);
        List<BigDecimal> usedMemoryRss = new ArrayList<>(size);
        List<BigDecimal> usedMemoryOverhead = new ArrayList<>(size);
        List<BigDecimal> usedMemoryDataset = new ArrayList<>(size);
        List<BigDecimal> usedMemoryDatasetPerc = new ArrayList<>(size);
        List<BigDecimal> memFragmentationRatio = new ArrayList<>(size);
        List<BigDecimal> totalConnectionsReceived = new ArrayList<>(size);
        List<BigDecimal> connectionsReceived = new ArrayList<>(size);
        List<BigDecimal> totalCommandsProcessed = new ArrayList<>(size);
        List<BigDecimal> commandsProcessed = new ArrayList<>(size);
        List<BigDecimal> totalNetInputBytes = new ArrayList<>(size);
        List<BigDecimal> netInputBytes = new ArrayList<>(size);
        List<BigDecimal> totalNetOutputBytes = new ArrayList<>(size);
        List<BigDecimal> netOutputBytes = new ArrayList<>(size);
        List<BigDecimal> keyspaceHits = new ArrayList<>(size);
        List<BigDecimal> keyspaceMisses = new ArrayList<>(size);
        List<BigDecimal> keyspaceHitsRatio = new ArrayList<>(size);
        List<BigDecimal> usedCpuSys = new ArrayList<>(size);
        List<BigDecimal> usedCpuUser = new ArrayList<>(size);
        List<BigDecimal> keys = new ArrayList<>(size);
        List<BigDecimal> expires = new ArrayList<>(size);
        for (NodeInfo nodeInfo : nodeInfoList) {
            responseTime.add(new BigDecimal(nodeInfo.getResponseTime()));
            connectedClients.add(new BigDecimal(nodeInfo.getConnectedClients()));
            clientLongestOutputList.add(new BigDecimal(nodeInfo.getClientLongestOutputList()));
            clientBiggestInputBuf.add(new BigDecimal(nodeInfo.getClientBiggestInputBuf()));
            blockedClients.add(new BigDecimal(nodeInfo.getBlockedClients()));
            usedMemory.add(new BigDecimal(nodeInfo.getUsedMemory()));
            usedMemoryRss.add(new BigDecimal(nodeInfo.getUsedMemoryRss()));
            usedMemoryOverhead.add(new BigDecimal(nodeInfo.getUsedMemoryOverhead()));
            usedMemoryDataset.add(new BigDecimal(nodeInfo.getUsedMemoryDataset()));
            usedMemoryDatasetPerc.add(new BigDecimal(nodeInfo.getUsedMemoryDatasetPerc()));
            memFragmentationRatio.add(new BigDecimal(nodeInfo.getMemFragmentationRatio()));
            totalConnectionsReceived.add(new BigDecimal(nodeInfo.getTotalConnectionsReceived()));
            connectionsReceived.add(new BigDecimal(nodeInfo.getConnectionsReceived()));
            totalCommandsProcessed.add(new BigDecimal(nodeInfo.getTotalCommandsProcessed()));
            commandsProcessed.add(new BigDecimal(nodeInfo.getCommandsProcessed()));
            totalNetInputBytes.add(new BigDecimal(nodeInfo.getTotalNetInputBytes()));
            netInputBytes.add(new BigDecimal(nodeInfo.getNetInputBytes()));
            totalNetOutputBytes.add(new BigDecimal(nodeInfo.getTotalNetOutputBytes()));
            netOutputBytes.add(new BigDecimal(nodeInfo.getNetOutputBytes()));
            keyspaceHits.add(new BigDecimal(nodeInfo.getKeyspaceHits()));
            keyspaceMisses.add(new BigDecimal(nodeInfo.getKeyspaceMisses()));
            keyspaceHitsRatio.add(new BigDecimal(nodeInfo.getKeyspaceHitsRatio()));
            usedCpuSys.add(new BigDecimal(nodeInfo.getUsedCpuSys()));
            usedCpuUser.add(new BigDecimal(nodeInfo.getUsedCpuUser()));
            keys.add(new BigDecimal(nodeInfo.getKeys()));
            expires.add(new BigDecimal(nodeInfo.getExpires()));
        }
        dataMap.put(RESPONSE_TIME, responseTime);
        dataMap.put(CONNECTED_CLIENTS, connectedClients);
        dataMap.put(CLIENT_LONGEST_PUTPUT_LIST, clientLongestOutputList);
        dataMap.put(CLIENT_BIGGEST_INPUT_BUF, clientBiggestInputBuf);
        dataMap.put(BLOCKED_CLIENTS, blockedClients);
        dataMap.put(USED_MEMORY, usedMemory);
        dataMap.put(USED_MEMORY_RSS, usedMemoryRss);
        dataMap.put(USED_MEMORY_OVERHEAD, usedMemoryOverhead);
        dataMap.put(USED_MEMORY_DATASET, usedMemoryDataset);
        dataMap.put(USED_MEMORY_DATASET_PERC, usedMemoryDatasetPerc);
        dataMap.put(MEM_FRAGMENTATION_RATIO, memFragmentationRatio);
        dataMap.put(TOTAL_CONNECTIONS_RECEIVED, totalConnectionsReceived);
        dataMap.put(CONNECTED_CLIENTS, connectionsReceived);
        dataMap.put(TOTAL_COMMANDS_PROCESSED, totalCommandsProcessed);
        dataMap.put(COMMANDS_PROCESSED, commandsProcessed);
        dataMap.put(TOTAL_NET_INPUT_BYTES, totalNetInputBytes);
        dataMap.put(NET_INPUT_BYTES, netInputBytes);
        dataMap.put(TOTAL_NET_OUTPUT_BYTES, totalNetOutputBytes);
        dataMap.put(NET_OUTPUT_BYTES, netOutputBytes);
        dataMap.put(KEYSPACE_HITS, keyspaceHits);
        dataMap.put(KEYSPACE_MISSES, keyspaceMisses);
        dataMap.put(KEYSPACE_HITS_RATIO, keyspaceHitsRatio);
        dataMap.put(USED_CPU_SYS, usedCpuSys);
        dataMap.put(USED_CPU_USER, usedCpuUser);
        dataMap.put(KEYS, keys);
        dataMap.put(EXPIRES, expires);
        return dataMap;
    }

    private List<NodeInfo> buildNodeInfoForSpecialDataType(Map<String, List<BigDecimal>> dataMap) {
        List<NodeInfo> nodeInfoList = new ArrayList<>(3);
        JSONObject avgObject = new JSONObject();
        JSONObject maxObject = new JSONObject();
        JSONObject minObject = new JSONObject();
        dataMap.forEach((key, list) -> {
            String nodeInfoField = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, key);
            BigDecimal avg = RedisUtil.avg(list);
            BigDecimal max = RedisUtil.max(list);
            BigDecimal min = RedisUtil.min(list);
            avgObject.put(nodeInfoField, avg.doubleValue());
            maxObject.put(nodeInfoField, max.doubleValue());
            minObject.put(nodeInfoField, min.doubleValue());
        });
        NodeInfo avgNodeInfo = avgObject.toJavaObject(NodeInfo.class);
        avgNodeInfo.setDataType(NodeInfoType.DataType.AVG);
        NodeInfo maxNodeInfo = maxObject.toJavaObject(NodeInfo.class);
        maxNodeInfo.setDataType(NodeInfoType.DataType.MAX);
        NodeInfo minNodeInfo = minObject.toJavaObject(NodeInfo.class);
        minNodeInfo.setDataType(NodeInfoType.DataType.MIN);
        nodeInfoList.add(avgNodeInfo);
        nodeInfoList.add(maxNodeInfo);
        nodeInfoList.add(minNodeInfo);
        return nodeInfoList;
    }

    @Deprecated
    private NodeInfo buildNodeINfoHour(Map<String, List<BigDecimal>> historyDataMap, NodeInfoType.DataType dataType) {
        JSONObject jsonObject = new JSONObject();
        historyDataMap.forEach((key, list) -> {
            String nodeInfoField = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, key);
            BigDecimal bigDecimal = null;
            if (Objects.equals(NodeInfoType.DataType.AVG, dataType)) {
                bigDecimal = RedisUtil.avg(list);
            } else if (Objects.equals(NodeInfoType.DataType.MAX, dataType)) {
                bigDecimal = RedisUtil.max(list);
            } else if (Objects.equals(NodeInfoType.DataType.MIN, dataType)) {
                bigDecimal = RedisUtil.min(list);
            }
            if (bigDecimal != null) {
                jsonObject.put(nodeInfoField, bigDecimal.doubleValue());
            }
        });
        NodeInfo nodeInfo = jsonObject.toJavaObject(NodeInfo.class);
        nodeInfo.setDataType(dataType);
        return nodeInfo;

    }

}
