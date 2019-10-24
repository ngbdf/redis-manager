package com.newegg.ec.redis.util;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.NodeInfo;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Redis info util
 * TODO: move to NodeInfoService
 *
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public class RedisNodeInfoUtil {

    private static final Map<String, Map<String, NodeInfo>> LIMIT_VALUE_MAP = new ConcurrentHashMap<>();

    private static final Map<String, String> INFO_DESC_MAP = new ConcurrentHashMap<>();

    public static final String RESPONSE_TIME = "response_time";
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
    public static final String CLIENT_LONGEST_OUTPUT_LIST = "client_longest_output_list";
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
    // 服务器已经接受的连接请求数量
    public static final String TOTAL_CONNECTIONS_RECEIVED = "total_connections_received";
    public static final String TOTAL_COMMANDS_PROCESSED = "total_commands_processed";
    public static final String INSTANTANEOUS_OPS_PER_SEC = "instantaneous_ops_per_sec";
    public static final String TOTAL_NET_INPUT_BYTES = "total_net_input_bytes";
    public static final String TOTAL_NET_OUTPUT_BYTES = "total_net_output_bytes";
    public static final String INSTANTANEOUS_INPUT_KBPS = "instantaneous_input_kbps";
    public static final String INSTANTANEOUS_OUTPUT_KBPS = "instantaneous_output_kbps";
    public static final String REJECTED_CONNECTIONS = "rejected_connections";
    public static final String SYNC_FULL = "sync_full";
    public static final String SYNC_PARTIAL_OK = "sync_partial_ok";
    public static final String SYNC_PARTIAL_ERR = "sync_partial_err";
    public static final String EXPIRED_KEYS = "expired_keys";
    public static final String KEYSPACE_HITS = "keyspace_hits";
    public static final String KEYSPACE_MISSES = "keyspace_misses";

    public static final String CONNECTIONS_RECEIVED = "connections_received";
    public static final String COMMANDS_PROCESSED = "commands_processed";
    public static final String NET_INPUT_BYTES = "net_input_bytes";
    public static final String NET_OUTPUT_BYTES = "net_output_bytes";
    public static final String KEYSPACE_HITS_RATIO = "keyspace_hits_ratio";

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
    public static final String CPU_SYS = "cpu_sys";
    public static final String USED_CPU_USER = "used_cpu_user";
    public static final String CPU_USER = "cpu_user";

    public static final String CLUSTER_ENABLED = "cluster_enabled";
    public static final String DB_PREFIX = "db";
    public static final String KEYS = "keys";
    public static final String EXPIRES = "expires";

    private RedisNodeInfoUtil() {
    }

    private static final BigDecimal BIG_DECIMAL_1024 = new BigDecimal(1024);

    public static final NodeInfo parseInfoToObject(Map<String, String> infoMap, NodeInfo lastTimeNodeInfo) throws IOException {
        JSONObject infoJSONObject = new JSONObject();
        long keys = 0;
        long expires = 0;
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
                String[] subContents = SignUtil.splitByCommas(value);
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
                    if (Objects.equals(subContentKey, KEYS)) {
                        keys += Long.parseLong(subContentVal);
                    } else if (Objects.equals(subContentKey, EXPIRES)) {
                        expires += Long.parseLong(subContentVal);
                    }
                }

            } else if (isByteToMBKeyField(key)) {
                // 转换单位
                long newValue = byteToMB(value);
                infoJSONObject.put(nodeInfoField, newValue);
            } else if (isSubtractionPercentSignField(key)) {
                // 去掉 %
                double newValue = truncatedPercentSign(value);
                infoJSONObject.put(nodeInfoField, newValue);
            } else {
                // 正常写入
                infoJSONObject.put(nodeInfoField, value);
            }
        }
        NodeInfo nodeInfo = infoJSONObject.toJavaObject(NodeInfo.class);
        nodeInfo.setKeys(keys);
        nodeInfo.setExpires(expires);
        // 计算相减
        calculateCumulativeData(nodeInfo, lastTimeNodeInfo);
        return nodeInfo;
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
        BigDecimal divide = bigDecimal.divide(BIG_DECIMAL_1024).divide(BIG_DECIMAL_1024, 3, BigDecimal.ROUND_HALF_UP);
        return divide.longValue();
    }

    public static double truncatedPercentSign(String originalData) {
        double percent = 0;
        if (!Strings.isNullOrEmpty(originalData) && originalData.contains("%")) {
            String replace = originalData.replace("%", "");
            percent = Double.parseDouble(replace);
        }
        return percent;
    }

    /**
     * 累计值计算
     * keyspace_hits
     * command_processed
     * used_cpu_sys
     * <p>
     * PS:以下信息虽然是累积值，但是不进行差值计算
     * rejected_connections: 拒绝的连接个数，redis连接个数达到 maxclients 限制，拒绝新连接的个数
     * sync_full: 主从完全同步成功次数
     * sync_partial_ok: 主从部分同步成功次数
     * sync_partial_err: 主从部分同步失败次数
     *
     * @param lastTimeNodeInfo
     * @param nodeInfo
     * @return
     */
    public static final NodeInfo calculateCumulativeData(NodeInfo nodeInfo, NodeInfo lastTimeNodeInfo) {
        if (lastTimeNodeInfo != null) {
            double keyspaceHitRatio = calculateKeyspaceHitRatio(lastTimeNodeInfo, nodeInfo);
            nodeInfo.setKeyspaceHitsRatio(keyspaceHitRatio);
            nodeInfo.setCommandsProcessed(nodeInfo.getTotalCommandsProcessed() - lastTimeNodeInfo.getTotalCommandsProcessed());
            nodeInfo.setConnectionsReceived(nodeInfo.getTotalConnectionsReceived() - lastTimeNodeInfo.getTotalConnectionsReceived());
            nodeInfo.setNetInputBytes(nodeInfo.getTotalNetInputBytes() - lastTimeNodeInfo.getTotalNetInputBytes());
            nodeInfo.setNetOutputBytes(nodeInfo.getTotalNetOutputBytes() - lastTimeNodeInfo.getTotalNetOutputBytes());
            nodeInfo.setCpuSys(nodeInfo.getUsedCpuSys() - lastTimeNodeInfo.getUsedCpuSys());
            nodeInfo.setCpuUser(nodeInfo.getUsedCpuUser() - lastTimeNodeInfo.getUsedCpuUser());
        } else {
            /*nodeInfo.setUsedCpuSys(0);
            nodeInfo.setTotalConnectionsReceived(0);
            nodeInfo.setTotalCommandsProcessed(0);
            nodeInfo.setTotalNetInputBytes(0);
            nodeInfo.setTotalNetOutputBytes(0);*/
        }
        return nodeInfo;
    }

    public static final double calculateKeyspaceHitRatio(NodeInfo nodeInfo, NodeInfo lastTimeNodeInfo) {
        long keyspaceHit = nodeInfo.getKeyspaceHits() - lastTimeNodeInfo.getKeyspaceHits();
        long keyspaceMisses = nodeInfo.getKeyspaceMisses() - lastTimeNodeInfo.getKeyspaceMisses();
        BigDecimal hitAndMiss = BigDecimal.valueOf(keyspaceHit + keyspaceMisses);
        if (hitAndMiss.longValue() == 0) {
            return 0;
        }
        BigDecimal divide = BigDecimal.valueOf(keyspaceHit).divide(hitAndMiss, 2, BigDecimal.ROUND_HALF_UP);
        return divide.doubleValue();
    }

    public static void updateLimitValueMap(String clusterId, List<NodeInfo> nodeInfoList) {
        boolean start = false;
        NodeInfo maxResponseTime = null;
        NodeInfo maxConnectedClients = null;
        NodeInfo maxKeys = null;
        NodeInfo minKeys = null;
        NodeInfo maxUsedMemory = null;
        NodeInfo minUsedMemory = null;
        for (NodeInfo nodeInfo : nodeInfoList) {
            if (!start) {
                maxResponseTime = nodeInfo;
                maxConnectedClients = nodeInfo;
                maxKeys = nodeInfo;
                minKeys = nodeInfo;
                maxUsedMemory = nodeInfo;
                minUsedMemory = nodeInfo;
                start = true;
                continue;
            }
            if (nodeInfo.getResponseTime() > maxResponseTime.getResponseTime()) {
                maxResponseTime = nodeInfo;
            }
            if (nodeInfo.getConnectedClients() > maxConnectedClients.getConnectedClients()) {
                maxConnectedClients = nodeInfo;
            }
            if (nodeInfo.getKeys() > maxKeys.getKeys()) {
                maxKeys = nodeInfo;
            } else {
                minKeys = nodeInfo;
            }
            if (nodeInfo.getUsedMemory() > maxUsedMemory.getUsedMemory()) {
                maxUsedMemory = nodeInfo;
            } else {
                minUsedMemory = nodeInfo;
            }
        }
        Map<String, NodeInfo> nodeInfoMap = new HashMap<>();
        nodeInfoMap.put("maxResponseTime", maxResponseTime);
        nodeInfoMap.put("maxConnectedClients", maxConnectedClients);
        nodeInfoMap.put("maxKeys", maxKeys);
        nodeInfoMap.put("minKeys", minKeys);
        nodeInfoMap.put("maxUsedMemory", maxUsedMemory);
        nodeInfoMap.put("minUsedMemory", minUsedMemory);
        LIMIT_VALUE_MAP.put(clusterId, nodeInfoMap);
    }

    public static Map<String, NodeInfo> getLimitValue(String clusterId) {
        return LIMIT_VALUE_MAP.get(clusterId);
    }

    public static String getNodeInfoItemDesc(String infoKey) {
        return INFO_DESC_MAP.get(infoKey);
    }

    static {
        INFO_DESC_MAP.put(REDIS_VERSION, "Version of the Redis server.");
        INFO_DESC_MAP.put("redis_git_sha1", "Git SHA1.");
        INFO_DESC_MAP.put("redis_git_dirty", "Git dirty flag.");
        INFO_DESC_MAP.put("redis_build_id", "The build id.");
        INFO_DESC_MAP.put(REDIS_MODE, "The server's mode ('standalone', 'sentinel' or 'cluster').");
        INFO_DESC_MAP.put(OS, "Operating system hosting the Redis server.");
        INFO_DESC_MAP.put("arch_bits", "Architecture (32 or 64 bits).");
        INFO_DESC_MAP.put("multiplexing_api", "Event loop mechanism used by Redis.");
        INFO_DESC_MAP.put("atomicvar_api", "Atomicvar API used by Redis.");
        INFO_DESC_MAP.put("gcc_version", "Version of the GCC compiler used to compile the Redis server.");
        INFO_DESC_MAP.put("process_id", "PID of the server process.");
        INFO_DESC_MAP.put("run_id", "Random value identifying the Redis server (to be used by Sentinel and Cluster).");
        INFO_DESC_MAP.put(TCP_PORT, "TCP/IP listen port.");
        INFO_DESC_MAP.put("uptime_in_seconds", "Number of seconds since Redis server start.");
        INFO_DESC_MAP.put("uptime_in_days", "Same value expressed in days.");
        INFO_DESC_MAP.put(HZ, "The server's frequency setting.");
        INFO_DESC_MAP.put("lru_clock", "Clock incrementing every minute, for LRU management.");
        INFO_DESC_MAP.put("executable", "The path to the server's executable.");
        INFO_DESC_MAP.put("config_file", "The path to the config file.");
        // client
        INFO_DESC_MAP.put(CONNECTED_CLIENTS, "Number of client connections (excluding connections from replicas).");
        INFO_DESC_MAP.put(CLIENT_LONGEST_OUTPUT_LIST, "longest output list among current client connections.");
        INFO_DESC_MAP.put(CLIENT_BIGGEST_INPUT_BUF, "biggest input buffer among current client connections.");
        INFO_DESC_MAP.put(BLOCKED_CLIENTS, "Number of clients pending on a blocking call (BLPOP, BRPOP, BRPOPLPUSH).");
        // memory
        INFO_DESC_MAP.put(USED_MEMORY, "Total number of bytes allocated by Redis using its allocator (either standard libc, jemalloc, or an alternative allocator such as tcmalloc).");
        INFO_DESC_MAP.put("used_memory_human", "Human readable representation of previous value.");
        INFO_DESC_MAP.put(USED_MEMORY_RSS, "Number of bytes that Redis allocated as seen by the operating system (a.k.a resident set size). This is the number reported by tools such as top(1) and ps(1).");
        INFO_DESC_MAP.put("used_memory_rss_human", "Human readable representation of previous value.");
        INFO_DESC_MAP.put(USED_MEMORY_PEAK, "Peak memory consumed by Redis (in bytes).");
        INFO_DESC_MAP.put("used_memory_peak_human", "Human readable representation of previous value.");
        INFO_DESC_MAP.put(USED_MEMORY_PEAK_PERC, "The percentage of used_memory_peak out of used_memory.");
        INFO_DESC_MAP.put(USED_MEMORY_OVERHEAD, "The sum in bytes of all overheads that the server allocated for managing its internal data structures.");
        INFO_DESC_MAP.put("used_memory_startup", "Initial amount of memory consumed by Redis at startup in bytes.");
        INFO_DESC_MAP.put(USED_MEMORY_DATASET, "The size in bytes of the dataset (used_memory_overhead subtracted from used_memory).");
        INFO_DESC_MAP.put(USED_MEMORY_DATASET_PERC, "The percentage of used_memory_dataset out of the net memory usage (used_memory minus used_memory_startup).");
        INFO_DESC_MAP.put("total_system_memory", "The total amount of memory that the Redis host has.");
        INFO_DESC_MAP.put("total_system_memory_human", "Human readable representation of previous value.");
        INFO_DESC_MAP.put("used_memory_lua", "Number of bytes used by the Lua engine.");
        INFO_DESC_MAP.put("used_memory_lua_human", "Human readable representation of previous value.");
        INFO_DESC_MAP.put("maxmemory", "The value of the maxmemory configuration directive.");
        INFO_DESC_MAP.put("maxmemory_human", "Human readable representation of previous value.");
        INFO_DESC_MAP.put("maxmemory_policy", "The value of the maxmemory-policy configuration directive.");
        INFO_DESC_MAP.put(MEM_FRAGMENTATION_RATIO, "Ratio between used_memory_rss and used_memory.");
        INFO_DESC_MAP.put("mem_allocator", "Memory allocator, chosen at compile time.");
        INFO_DESC_MAP.put("active_defrag_running", "Flag indicating if active defragmentation is active.");
        INFO_DESC_MAP.put("lazyfree_pending_objects", "The number of objects waiting to be freed (as a result of calling UNLINK, or FLUSHDB and FLUSHALL with the ASYNC option).");
        // persistence
        INFO_DESC_MAP.put("loading", "Flag indicating if the load of a dump file is on-going.");
        INFO_DESC_MAP.put("rdb_changes_since_last_save", "Number of changes since the last dump.");
        INFO_DESC_MAP.put("rdb_bgsave_in_progress", "Flag indicating a RDB save is on-going.");
        INFO_DESC_MAP.put("rdb_last_save_time", "Epoch-based timestamp of last successful RDB save.");
        INFO_DESC_MAP.put("rdb_last_bgsave_status", "Status of the last RDB save operation.");
        INFO_DESC_MAP.put("rdb_last_bgsave_time_sec", "Duration of the last RDB save operation in seconds.");
        INFO_DESC_MAP.put("rdb_current_bgsave_time_sec", "Duration of the on-going RDB save operation if any.");
        INFO_DESC_MAP.put("rdb_last_cow_size", "The size in bytes of copy-on-write allocations during the last RBD save operation.");
        INFO_DESC_MAP.put("aof_enabled", "Flag indicating AOF logging is activated.");
        INFO_DESC_MAP.put("aof_rewrite_in_progress", "Flag indicating a AOF rewrite operation is on-going.");
        INFO_DESC_MAP.put("aof_rewrite_scheduled", "Flag indicating an AOF rewrite operation will be scheduled once the on-going RDB save is complete.");
        INFO_DESC_MAP.put("aof_last_rewrite_time_sec", "Duration of the last AOF rewrite operation in seconds.");
        INFO_DESC_MAP.put("aof_current_rewrite_time_sec", "Duration of the on-going AOF rewrite operation if any.");
        INFO_DESC_MAP.put("aof_last_bgrewrite_status", "Status of the last AOF rewrite operation.");
        INFO_DESC_MAP.put("aof_last_write_status", "Status of the last write operation to the AOF.");
        INFO_DESC_MAP.put("aof_last_cow_size", "The size in bytes of copy-on-write allocations during the last AOF rewrite operation.");
        INFO_DESC_MAP.put("aof_current_size", "AOF current file size.");
        INFO_DESC_MAP.put("aof_base_size", "AOF file size on latest startup or rewrite.");
        INFO_DESC_MAP.put("aof_pending_rewrite", "Flag indicating an AOF rewrite operation will be scheduled once the on-going RDB save is complete.");
        INFO_DESC_MAP.put("aof_buffer_length", "Size of the AOF buffer.");
        INFO_DESC_MAP.put("aof_rewrite_buffer_length", "Size of the AOF rewrite buffer.");
        INFO_DESC_MAP.put("aof_pending_bio_fsync", "Number of fsync pending jobs in background I/O queue.");
        INFO_DESC_MAP.put("aof_delayed_fsync", "Delayed fsync counter.");
        INFO_DESC_MAP.put("loading_start_time", "Epoch-based timestamp of the start of the load operation.");
        INFO_DESC_MAP.put("loading_total_bytes", "Total file size.");
        INFO_DESC_MAP.put("loading_loaded_bytes", "Number of bytes already loaded.");
        INFO_DESC_MAP.put("loading_loaded_perc", "Same value expressed as a percentage.");
        INFO_DESC_MAP.put("loading_eta_seconds", "ETA in seconds for the load to be complete.");
        // stats
        INFO_DESC_MAP.put(TOTAL_CONNECTIONS_RECEIVED, "Total number of connections accepted by the server.");
        INFO_DESC_MAP.put(TOTAL_COMMANDS_PROCESSED, "Total number of commands processed by the server.");
        INFO_DESC_MAP.put(INSTANTANEOUS_OPS_PER_SEC, "Number of commands processed per second.");
        INFO_DESC_MAP.put(TOTAL_NET_INPUT_BYTES, "The total number of bytes read from the network.");
        INFO_DESC_MAP.put(TOTAL_NET_OUTPUT_BYTES, "The total number of bytes written to the network.");
        INFO_DESC_MAP.put(INSTANTANEOUS_INPUT_KBPS, "The network's read rate per second in KB/sec.");
        INFO_DESC_MAP.put(INSTANTANEOUS_OUTPUT_KBPS, "The network's write rate per second in KB/sec.");
        INFO_DESC_MAP.put(REJECTED_CONNECTIONS, "Number of connections rejected because of maxclients limit.");
        INFO_DESC_MAP.put(SYNC_FULL, "The number of full resyncs with replicas.");
        INFO_DESC_MAP.put(SYNC_PARTIAL_OK, "The number of accepted partial resync requests.");
        INFO_DESC_MAP.put(SYNC_PARTIAL_ERR, "The number of denied partial resync requests.");
        INFO_DESC_MAP.put(EXPIRED_KEYS, "Total number of key expiration events.");
        INFO_DESC_MAP.put("evicted_keys", "Number of evicted keys due to maxmemory limit.");
        INFO_DESC_MAP.put(KEYSPACE_HITS, "Number of successful lookup of keys in the main dictionary.");
        INFO_DESC_MAP.put(KEYSPACE_MISSES, "Number of failed lookup of keys in the main dictionary.");
        INFO_DESC_MAP.put("pubsub_channels", "Global number of pub/sub channels with client subscriptions.");
        INFO_DESC_MAP.put("pubsub_patterns", "Global number of pub/sub pattern with client subscriptions.");
        INFO_DESC_MAP.put("latest_fork_usec", "Duration of the latest fork operation in microseconds.");
        INFO_DESC_MAP.put("migrate_cached_sockets", "The number of sockets open for MIGRATE purposes.");
        INFO_DESC_MAP.put("slave_expires_tracked_keys", "The number of keys tracked for expiry purposes (applicable only to writable replicas).");
        INFO_DESC_MAP.put("active_defrag_hits", "Number of value reallocations performed by active the defragmentation process.");
        INFO_DESC_MAP.put("active_defrag_misses", "Number of aborted value reallocations started by the active defragmentation process.");
        INFO_DESC_MAP.put("active_defrag_key_hits", "Number of keys that were actively defragmented.");
        INFO_DESC_MAP.put("active_defrag_key_misses", "Number of keys that were skipped by the active defragmentation process.");
        // replication
        INFO_DESC_MAP.put(ROLE, "Value is 'master' if the instance is replica of no one, or 'slave' if the instance is a replica of some master instance. Note that a replica can be master of another replica (chained replication).");
        INFO_DESC_MAP.put("master_replid", "The replication ID of the Redis server.");
        INFO_DESC_MAP.put("master_replid2", "The secondary replication ID, used for PSYNC after a failover.");
        INFO_DESC_MAP.put("master_repl_offset", "The server's current replication offset.");
        INFO_DESC_MAP.put("second_repl_offset", "The offset up to which replication IDs are accepted.");
        INFO_DESC_MAP.put("repl_backlog_active", "Flag indicating replication backlog is active.");
        INFO_DESC_MAP.put("repl_backlog_size", "Total size in bytes of the replication backlog buffer.");
        INFO_DESC_MAP.put("repl_backlog_first_byte_offset", "The master offset of the replication backlog buffer.");
        INFO_DESC_MAP.put("repl_backlog_histlen", "Size in bytes of the data in the replication backlog buffer.");
        INFO_DESC_MAP.put("master_host", "Host or IP address of the master.");
        INFO_DESC_MAP.put("master_port", "Master listening TCP port.");
        INFO_DESC_MAP.put("master_link_status", "Status of the link (up/down).");
        INFO_DESC_MAP.put("master_last_io_seconds_ago", "Number of seconds since the last interaction with master.");
        INFO_DESC_MAP.put("master_sync_in_progress", "Indicate the master is syncing to the replica.");
        INFO_DESC_MAP.put("slave_repl_offset", "The replication offset of the replica instance.");
        INFO_DESC_MAP.put("slave_priority", "The priority of the instance as a candidate for failover.");
        INFO_DESC_MAP.put("slave_read_only", "Flag indicating if the replica is read-only.");
        INFO_DESC_MAP.put("master_sync_left_bytes", "Number of bytes left before syncing is complete.");
        INFO_DESC_MAP.put("master_sync_last_io_seconds_ago", "Number of seconds since last transfer I/O during a SYNC operation.");
        INFO_DESC_MAP.put("master_link_down_since_seconds", "Number of seconds since the link is down.");
        INFO_DESC_MAP.put("connected_slaves", "Number of connected replicas.");
        INFO_DESC_MAP.put("min_slaves_good_slaves", "Number of replicas currently considered good.");
        // cpu
        INFO_DESC_MAP.put(USED_CPU_SYS, "System CPU consumed by the Redis server.");
        INFO_DESC_MAP.put(USED_CPU_USER,"User CPU consumed by the Redis server.");
        INFO_DESC_MAP.put("used_cpu_sys_children", "System CPU consumed by the background processes.");
        INFO_DESC_MAP.put("used_cpu_user_children", "User CPU consumed by the background processes.");
        // cluster
        INFO_DESC_MAP.put("cluster_enabled", "Indicate Redis cluster is enabled.");
    }

}
