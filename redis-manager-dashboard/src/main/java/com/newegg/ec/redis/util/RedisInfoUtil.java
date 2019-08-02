package com.newegg.ec.redis.util;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.NodeInfo;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


/**
 *  Redis info util
 *
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public class RedisInfoUtil {

    /**
     * Desc: 数据存放上次获取的集群 NodeInfo
     * Cause: 为了通过一些累加的数据计算出一分钟的变化量
     * Data: <clusterName, <host, nodeInfo>>
     */
    private static final Map<String, Map<String, NodeInfo>> LAST_TIME_NODE_INFO = new ConcurrentHashMap<>();

    /**
     * Server
     */
    public static final String REDIS_VERSION = "redis_version";
    public static final String REDIS_MODE = "redis_mode";
    public static final String OS = "os";
    public static final String TCP_PORT = "tcp_port";
    public static final String UPDATE_IN_SECONDES = "uptime_in_seconds";
    public static final String UPDATE_IN_DAYS = "uptime_in_days";
    public static final String HZ = "hz";
    public static final String EXECUTABLE = "executable";
    public static final String CONFIG_FILE = "config_file";

    /**
     * Clients
     */
    public static final String CONNECTED_CLIENTS = "connected_clients";
    public static final String CLIENT_LONGEST_PUTPUT_LIST = "client_longest_output_list";
    public static final String CLIENT_BIGGEST_INPUT_BUF = "client_biggest_input_buf";
    public static final String BLOCKED_CLIENTS = "blocked_clients";

    /**
     * Memory
     */
    public static final String USED_MEMORY = "used_memory";
    public static final String USED_MEMORY_RSS = "used_memory_rss";
    public static final String USED_MEMORY_PEAK = "used_memory_peak";
    public static final String USED_MEMORY_PEAK_PERC = "used_memory_peak_perc";
    public static final String USED_MEMORY_OVERHEAD = "used_memory_overhead";
    public static final String USED_MEMORY_DATASET = "used_memory_dataset";
    public static final String USED_MEMORY_DATASET_PERC = "used_memory_dataset_perc";
    public static final String MEM_FRAGMENTATION_RATIO = "mem_fragmentation_ratio";

    /** Persistence: Scalable */

    /**
     * Stats
     */
    public static final String TOTAL_CONNECTIONS_RECEIVED = "total_connections_received";
    public static final String TOTAL_COMMANDS_PROCESSED = "total_commands_processed";
    public static final String REJECTED_CONNECTIONS = "rejected_connections";
    public static final String SYNC_FULL = "sync_full";
    public static final String SYNC_PARTIAL_OK = "sync_partial_ok";
    public static final String SYNC_PARTIAL_ERR = "sync_partial_err";
    public static final String EXPIRED_KEYS = "expired_keys";
    public static final String KEYSPACE_HITS = "keyspace_hits";
    public static final String KEYSPACE_MISSES = "keyspace_misses";

    /**
     * Replication
     */
    public static final String ROLE = "role";
    public static final String CONNECTED_SLAVES = "connected_slaves";
    public static final String MASTER_REPL_OFFSET = "master_repl_offset";

    /**
     * CPU
     */
    public static final String USED_CPU_SYS = "used_cpu_sys";
    public static final String CLUSTER_ENABLED = "cluster_enabled";
    public static final String DB_PREFIX = "db";
    public static final String KEYS = "keys";
    public static final String EXPIRES = "expires";


    private RedisInfoUtil() {
    }

    private static final BigDecimal BIG_DECIMAL_1024 = new BigDecimal(1024);

    public static final NodeInfo parseInfoToObject(String clusterId, String host, String info) throws IOException {
        JSONObject infoJSONObject = new JSONObject();
        long keys = 0;
        long expires = 0;
        NodeInfo lastTimeNodeInfo = null;
        Map<String, NodeInfo> nodeInfoMap = LAST_TIME_NODE_INFO.get(clusterId);
        if (nodeInfoMap != null && !nodeInfoMap.isEmpty()) {
            lastTimeNodeInfo = nodeInfoMap.get(host);
        }
        Map<String, String> infoMap = RedisUtil.parseInfoToMap(info);
        for (Map.Entry<String, String> entry : infoMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (Strings.isNullOrEmpty(key) || Strings.isNullOrEmpty(value)) {
                continue;
            }
            // eg: used_memory -> usedMemory
            String nodeInfoField = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, key);
            // for keys and expires
            if (key.startsWith(DB_PREFIX)) {
                String[] subContents = value.split(",");
                for (String subContent : subContents) {
                    String[] split = subContent.split("=");
                    if (split.length != 2) {
                        continue;
                    }
                    String subContentKey = split[0];
                    String subContentVal = split[1];
                    if (Strings.isNullOrEmpty(subContentVal)) {
                        continue;
                    }
                    if (Objects.equals(subContentKey, KEYS)) {
                        keys += Long.valueOf(subContentVal);
                    } else if (Objects.equals(subContentKey, EXPIRES)) {
                        expires += Long.valueOf(subContentVal);
                    }
                }
            } else if (isByteToMBKeyField(key)) {
                long newValue = byteToMB(value);
                infoJSONObject.put(nodeInfoField, newValue);
            } else if (isSubtractionPercentSignField(key)) {
                double newValue = truncatedPercentSign(value);
                infoJSONObject.put(nodeInfoField, newValue);
            } else {
                infoJSONObject.put(nodeInfoField, value);
            }
        }
        NodeInfo nodeInfo = infoJSONObject.toJavaObject(NodeInfo.class);
        nodeInfo.setKeys(keys);
        nodeInfo.setExpires(expires);
        NodeInfo nodeInfoWithCalculation = finalCalculation(lastTimeNodeInfo, nodeInfo);
        return nodeInfoWithCalculation;
    }

    public static final void refreshLastTimeNodeInfoMap(String clusterId, Map<String, NodeInfo> infoMap) {
        LAST_TIME_NODE_INFO.put(clusterId, infoMap);
    }

    /**
     * 判断是否需要进行单位转换
     *
     * @param key
     * @return
     */
    private static final boolean isByteToMBKeyField(String key) {
        return Objects.equals(USED_MEMORY, key)
                || Objects.equals(USED_MEMORY_RSS, key)
                || Objects.equals(USED_MEMORY_OVERHEAD, key)
                || Objects.equals(USED_MEMORY_DATASET, key);
    }

    /**
     * 判断是否需要截断 %
     *
     * @param key
     * @return
     */
    private static final boolean isSubtractionPercentSignField(String key) {
        return Objects.equals(USED_MEMORY_PEAK_PERC, key)
                || Objects.equals(USED_MEMORY_DATASET_PERC, key);
    }

    /**
     * 单位转换
     *
     * @param originalData
     * @return
     */
    private static long byteToMB(String originalData) {
        if (Strings.isNullOrEmpty(originalData)) {
            return 0;
        }
        BigDecimal bigDecimal = BigDecimal.valueOf(Long.parseLong(originalData));
        BigDecimal divide = bigDecimal.divide(BIG_DECIMAL_1024).divide(BIG_DECIMAL_1024);
        return divide.longValue();
    }

    private static double truncatedPercentSign(String originalData) {
        double percent = 0;
        if (!Strings.isNullOrEmpty(originalData) && originalData.contains("%")) {
            String replace = originalData.replace("%", "");
            percent = Double.parseDouble(replace);
        }
        return percent;
    }

    /**
     * 计算一些字段的一分钟变化量
     * keyspace_hits
     * command_processed
     * @param lastTimeNodeInfo
     * @param nodeInfo
     * @return
     */
    private static final NodeInfo finalCalculation(NodeInfo lastTimeNodeInfo, NodeInfo nodeInfo) {
        if (lastTimeNodeInfo == null) {
            nodeInfo.setUsedCpuSys(0);
            nodeInfo.setTotalCommandsProcessed(0);
            return nodeInfo;
        } else {
            double keyspaceHitRatio = calculateKeyspaceHitRatio(lastTimeNodeInfo, nodeInfo);
            nodeInfo.setKeyspaceHitsRatio(keyspaceHitRatio);
            nodeInfo.setCommandsProcessed(nodeInfo.getTotalCommandsProcessed() - lastTimeNodeInfo.getTotalCommandsProcessed());
            nodeInfo.setUsedCpuSys(nodeInfo.getUsedCpuSys() - lastTimeNodeInfo.getUsedCpuSys());
        }
        return nodeInfo;
    }

    private static final double calculateKeyspaceHitRatio(NodeInfo lastTimeNodeInfo, NodeInfo nodeInfo) {
        long minuteKeyspaceHit = nodeInfo.getKeyspaceHits() - lastTimeNodeInfo.getKeyspaceHits();
        long minuteKeyspaceMisses = nodeInfo.getKeyspaceMisses() - lastTimeNodeInfo.getKeyspaceMisses();
        BigDecimal divide = BigDecimal.valueOf(minuteKeyspaceHit).divide(BigDecimal.valueOf(minuteKeyspaceHit + minuteKeyspaceMisses));
        return divide.doubleValue();
    }

}
