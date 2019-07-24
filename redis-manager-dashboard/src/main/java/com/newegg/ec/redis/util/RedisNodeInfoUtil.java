package com.newegg.ec.redis.util;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.NodeInfo;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 计算槽位等
 *
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public class RedisNodeInfoUtil {

    /**
     * Desc: 数据存放上次获取的集群 NodeInfo
     * Cause: 为了通过一些累加的数据计算出一分钟的变化量
     * Data: <clusterName, <host, nodeInfo>>
     */
    private static final Map<String, Map<String, NodeInfo>> LAST_TIME_NODE_INFO = new ConcurrentHashMap<>();


    private RedisNodeInfoUtil() {
    }

    private static final BigDecimal BIG_DECIMAL_1024 = new BigDecimal(1024);

    /**
     * special fields
     */
    private static final String KEYS = "keys";

    private static final String EXPIRES = "expires";

    private static final String DB = "db";

    private static final String USED_MEMORY = "used_memory";

    private static final String USED_MEMORY_RSS = "used_memory_rss";

    private static final String USED_MEMORY_PEAK_PERC = "used_memory_peak_perc";

    private static final String USED_MEMORY_OVERHEAD = "used_memory_overhead";

    private static final String USED_MEMORY_DATASET = "used_memory_dataset";

    private static final String USED_MEMORY_DATASET_PERC = "used_memory_dataset_perc";

    public static final NodeInfo parseInfo(String info) throws IOException {
        JSONObject infoJSONObject = new JSONObject();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(info.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
        long keys = 0;
        long expires = 0;
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] keyValue = line.split(":");
            if (keyValue.length != 2) {
                continue;
            }
            String key = keyValue[0];
            String value = keyValue[1];
            if (Strings.isNullOrEmpty(key) || Strings.isNullOrEmpty(value)) {
                continue;
            }
            // eg: used_memory -> usedMemory
            String nodeInfoField = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, key);
            // for keys and expires
            if (key.startsWith(DB)) {
                String[] subContents = value.split(",");
                for (String subContent : subContents) {
                    String[] split = subContent.split("=");
                    if (split.length != 2) {
                        continue;
                    }
                    String subContentKey = split[0];
                    String subContentVal = split[1];
                    if (!Strings.isNullOrEmpty(subContentVal)) {
                        if (Objects.equals(subContentKey, KEYS)) {
                            keys += Long.valueOf(subContentVal);
                        } else if (Objects.equals(subContentKey, EXPIRES)) {
                            expires += Long.valueOf(subContentVal);
                        }
                    }
                }
            } else if (isByteToMBKey(key)) {
                long newValue = byteToMB(value);
                infoJSONObject.put(nodeInfoField, newValue);
            } else if (isRemovePercentSign(key)) {
                double newValue = removePercentSign(value);
                infoJSONObject.put(nodeInfoField, newValue);
            } else {
                infoJSONObject.put(nodeInfoField, value);
            }
        }
        NodeInfo nodeInfo = infoJSONObject.toJavaObject(NodeInfo.class);
        nodeInfo.setKeys(keys);
        nodeInfo.setExpires(expires);
        return nodeInfo;
    }

    private static final boolean isByteToMBKey(String key) {
        return Objects.equals(USED_MEMORY, key)
                || Objects.equals(USED_MEMORY_RSS, key)
                || Objects.equals(USED_MEMORY_OVERHEAD, key)
                || Objects.equals(USED_MEMORY_DATASET, key);
    }

    private static final boolean isRemovePercentSign(String key) {
        return Objects.equals(USED_MEMORY_PEAK_PERC, key)
                || Objects.equals(USED_MEMORY_DATASET_PERC, key);
    }

    public static long byteToMB(String originalData) {
        if (Strings.isNullOrEmpty(originalData)) {
            return 0;
        }
        BigDecimal bigDecimal = BigDecimal.valueOf(Long.parseLong(originalData));
        BigDecimal divide = bigDecimal.divide(BIG_DECIMAL_1024).divide(BIG_DECIMAL_1024);
        return divide.longValue();
    }

    private static double removePercentSign(String originalData) {
        double percent = 0;
        if (!Strings.isNullOrEmpty(originalData) && originalData.contains("%")) {
            String replace = originalData.replace("%", "");
            percent = Double.parseDouble(replace);
        }
        return percent;
    }

    public static void main(String[] args) throws IOException {
        NodeInfo nodeInfo = parseInfo(INFO);
        System.err.println(JSONObject.toJSONString(nodeInfo));

    }

    private static String INFO = "# Server\n" +
            "redis_version:4.0.10\n" +
            "redis_git_sha1:00000000\n" +
            "redis_git_dirty:0\n" +
            "redis_build_id:da6c26a0ca9ce3a9\n" +
            "redis_mode:cluster\n" +
            "os:Linux 3.10.0-327.el7.x86_64 x86_64\n" +
            "arch_bits:64\n" +
            "multiplexing_api:epoll\n" +
            "atomicvar_api:atomic-builtin\n" +
            "gcc_version:4.8.5\n" +
            "process_id:11\n" +
            "run_id:1c4b11d942510d05f5176931edb616c3655ffb98\n" +
            "tcp_port:8201\n" +
            "uptime_in_seconds:356040\n" +
            "uptime_in_days:4\n" +
            "hz:10\n" +
            "lru_clock:3667665\n" +
            "executable:/redis/redis-4.0.10/src/redis-server\n" +
            "config_file:/data/redis/8201/redis.conf\n" +
            "\n" +
            "# Clients\n" +
            "connected_clients:61\n" +
            "client_longest_output_list:0\n" +
            "client_biggest_input_buf:0\n" +
            "blocked_clients:0\n" +
            "\n" +
            "# Memory\n" +
            "used_memory:1171575984\n" +
            "used_memory_human:1.09G\n" +
            "used_memory_rss:1210011648\n" +
            "used_memory_rss_human:1.13G\n" +
            "used_memory_peak:1172448336\n" +
            "used_memory_peak_human:1.09G\n" +
            "used_memory_peak_perc:99.93%\n" +
            "used_memory_overhead:205349138\n" +
            "used_memory_startup:3066752\n" +
            "used_memory_dataset:966226846\n" +
            "used_memory_dataset_perc:82.69%\n" +
            "total_system_memory:270261809152\n" +
            "total_system_memory_human:251.70G\n" +
            "used_memory_lua:37888\n" +
            "used_memory_lua_human:37.00K\n" +
            "maxmemory:6000000000\n" +
            "maxmemory_human:5.59G\n" +
            "maxmemory_policy:volatile-lru\n" +
            "mem_fragmentation_ratio:1.03\n" +
            "mem_allocator:jemalloc-4.0.3\n" +
            "active_defrag_running:0\n" +
            "lazyfree_pending_objects:0\n" +
            "\n" +
            "# Persistence\n" +
            "loading:0\n" +
            "rdb_changes_since_last_save:44575168\n" +
            "rdb_bgsave_in_progress:0\n" +
            "rdb_last_save_time:1563592735\n" +
            "rdb_last_bgsave_status:ok\n" +
            "rdb_last_bgsave_time_sec:0\n" +
            "rdb_current_bgsave_time_sec:-1\n" +
            "rdb_last_cow_size:8548352\n" +
            "aof_enabled:0\n" +
            "aof_rewrite_in_progress:0\n" +
            "aof_rewrite_scheduled:0\n" +
            "aof_last_rewrite_time_sec:-1\n" +
            "aof_current_rewrite_time_sec:-1\n" +
            "aof_last_bgrewrite_status:ok\n" +
            "aof_last_write_status:ok\n" +
            "aof_last_cow_size:0\n" +
            "\n" +
            "# Stats\n" +
            "total_connections_received:110859\n" +
            "total_commands_processed:63755615\n" +
            "instantaneous_ops_per_sec:46\n" +
            "total_net_input_bytes:21424117282\n" +
            "total_net_output_bytes:44797196545\n" +
            "instantaneous_input_kbps:3.38\n" +
            "instantaneous_output_kbps:5.79\n" +
            "rejected_connections:0\n" +
            "sync_full:2\n" +
            "sync_partial_ok:0\n" +
            "sync_partial_err:2\n" +
            "expired_keys:0\n" +
            "expired_stale_perc:0.00\n" +
            "expired_time_cap_reached_count:0\n" +
            "evicted_keys:0\n" +
            "keyspace_hits:6002074\n" +
            "keyspace_misses:9567315\n" +
            "pubsub_channels:0\n" +
            "pubsub_patterns:0\n" +
            "latest_fork_usec:798\n" +
            "migrate_cached_sockets:0\n" +
            "slave_expires_tracked_keys:0\n" +
            "active_defrag_hits:0\n" +
            "active_defrag_misses:0\n" +
            "active_defrag_key_hits:0\n" +
            "active_defrag_key_misses:0\n" +
            "\n" +
            "# Replication\n" +
            "role:master\n" +
            "connected_slaves:2\n" +
            "slave0:ip=172.16.35.76,port=8203,state=online,offset=19957610024,lag=1\n" +
            "slave1:ip=172.16.35.77,port=8203,state=online,offset=19957610024,lag=2\n" +
            "master_replid:b3a354cf821796b301209e9bc66d952c0bbcd907\n" +
            "master_replid2:0000000000000000000000000000000000000000\n" +
            "master_repl_offset:19957610038\n" +
            "second_repl_offset:-1\n" +
            "repl_backlog_active:1\n" +
            "repl_backlog_size:67108864\n" +
            "repl_backlog_first_byte_offset:19890501175\n" +
            "repl_backlog_histlen:67108864\n" +
            "\n" +
            "# CPU\n" +
            "used_cpu_sys:1264.58\n" +
            "used_cpu_user:2275.10\n" +
            "used_cpu_sys_children:0.00\n" +
            "used_cpu_user_children:0.00\n" +
            "\n" +
            "# Cluster\n" +
            "cluster_enabled:1\n" +
            "\n" +
            "# Keyspace\n" +
            "db0:keys=2511467,expires=0,avg_ttl=0\n";
}
