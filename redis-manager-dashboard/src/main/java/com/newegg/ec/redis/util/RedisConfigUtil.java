package com.newegg.ec.redis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * redis.conf
 * <p>
 * 维护 redis 4, 5 配置文件
 *
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public class RedisConfigUtil {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfigUtil.class);

    private static final List<RedisConfig> REDIS_CONFIG_LIST = new ArrayList<>();

    public static final String REDIS_CONF = "redis.conf";

    private static final String REQUIRE_PASS = "requirepass";

    public static final int NORMAL = 0;

    public static final int CLUSTER = 1;

    public RedisConfigUtil() {
    }

    public static void generateRedis4ClusterConfig(String path, String requirePass) throws IOException {
        generateRedisConfig(path, 4, CLUSTER, requirePass);
    }

    public static void generateRedis4NormalConfig(String path, String requirePass) throws IOException {
        generateRedisConfig(path, 4, NORMAL, requirePass);
    }

    public static void generateRedis5ClusterConfig(String path, String requirePass) throws IOException {
        generateRedisConfig(path, 5, CLUSTER, requirePass);
    }

    public static void generateRedis5NormalConfig(String path, String requirePass) throws IOException {
        generateRedisConfig(path, 5, NORMAL, requirePass);
    }

    public static void generateRedisConfig(String path, int version, int mode, String requirePass) throws IOException {
        File file = new File(path + REDIS_CONF);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file, true), "UTF-8"));
        for (RedisConfig redisConfig : REDIS_CONFIG_LIST) {
            String configKey = redisConfig.getConfigKey();
            String configValue = redisConfig.getConfigValue();
            if (Objects.equals(REQUIRE_PASS, configKey)) {
                configValue = requirePass;
            }
            if (version >= redisConfig.getVersion() && redisConfig.getMode() == mode) {
                String itemConfig = configKey + " " + configValue;
                try {
                    bufferedWriter.write(itemConfig);
                    bufferedWriter.newLine();
                } catch (IOException e) {
                    logger.error("Writer config failed, " + itemConfig, e);
                }
            }
        }
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    /**
     * init all config
     */
    static {
        // RDB文件默认名称,默认dump.rdb
        REDIS_CONFIG_LIST.add(new RedisConfig("dbfilename", "dump.rdb", 4, 0));
        // redis 密码
        REDIS_CONFIG_LIST.add(new RedisConfig("requirepass", "", 4, 0));
        // master 密码, 与 requirepass 一致
        REDIS_CONFIG_LIST.add(new RedisConfig("masterauth", "", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("cluster-announce-ip", "", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("unixsocket", "", 4, 0));
        // 日志文件名
        REDIS_CONFIG_LIST.add(new RedisConfig("logfile", "log.out", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("pidfile", "./redis_{port}.pid", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("slave-announce-ip", "", 4, 0));
        // 当前实例最大可用内存
        REDIS_CONFIG_LIST.add(new RedisConfig("maxmemory", "5gb", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("proto-max-bulk-len", "536870912", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("client-query-buffer-limit", "1073741824", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("maxmemory-samples", "5", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("lfu-log-factor", "10", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("lfu-decay-time", "1", 4, 0));
        // 客户端闲置多少秒后关闭连接,默认为0(永不关闭),redis-manager 默认 3600
        REDIS_CONFIG_LIST.add(new RedisConfig("timeout", "3600", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("active-defrag-threshold-lower", "10", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("active-defrag-threshold-upper", "100", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("active-defrag-ignore-bytes", "104857600", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("active-defrag-cycle-min", "25", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("active-defrag-cycle-max", "75", 4, 0));
        // Redis重写aof文件的比例条件,默认从100开始,统一机器下不同实例按4%递减
        REDIS_CONFIG_LIST.add(new RedisConfig("auto-aof-rewrite-percentage", "100", 4, 0));
        // 触发rewrite的aof文件最小阀值,默认64mb
        REDIS_CONFIG_LIST.add(new RedisConfig("auto-aof-rewrite-min-size", "64mb", 4, 0));
        // hash数据结构优化参数
        REDIS_CONFIG_LIST.add(new RedisConfig("hash-max-ziplist-entries", "512", 4, 0));
        // hash数据结构优化参数
        REDIS_CONFIG_LIST.add(new RedisConfig("hash-max-ziplist-value", "64", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("list-max-ziplist-size", "-2", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("list-compress-depth", "0", 4, 0));
        // set数据结构优化参数
        REDIS_CONFIG_LIST.add(new RedisConfig("set-max-intset-entries", "512", 4, 0));
        // zset数据结构优化参数
        REDIS_CONFIG_LIST.add(new RedisConfig("zset-max-ziplist-entries", "128", 4, 0));
        // zset数据结构优化参数
        REDIS_CONFIG_LIST.add(new RedisConfig("zset-max-ziplist-value", "64", 4, 0));
        // HyperLogLog稀疏表示限制设置
        REDIS_CONFIG_LIST.add(new RedisConfig("hll-sparse-max-bytes", "3000", 4, 0));
        // Lua脚本最长的执行时间，单位为毫秒
        REDIS_CONFIG_LIST.add(new RedisConfig("lua-time-limit", "5000", 4, 0));
        // 慢查询被记录的阀值,默认10毫秒
        REDIS_CONFIG_LIST.add(new RedisConfig("slowlog-log-slower-than", "10000", 4, 0));
        // 最多记录慢查询的条数
        REDIS_CONFIG_LIST.add(new RedisConfig("slowlog-max-len", "500", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("latency-monitor-threshold", "0", 4, 0));
        // 端口
        REDIS_CONFIG_LIST.add(new RedisConfig("port", "{port}", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("cluster-announce-port", "0", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("cluster-announce-bus-port", "0", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("tcp-backlog", "511", 4, 0));
        // 可用的数据库数，默认值为16个,默认数据库为0
        REDIS_CONFIG_LIST.add(new RedisConfig("databases", "16", 4, 0));
        // 指定slave定期ping master的周期,默认:10秒
        REDIS_CONFIG_LIST.add(new RedisConfig("repl-ping-slave-period", "10", 4, 0));
        // master批量数据传输时间或者ping回复时间间隔,默认:60秒
        REDIS_CONFIG_LIST.add(new RedisConfig("repl-timeout", "60", 4, 0));
        // 复制缓存区,默认:1mb,配置为:10mb
        REDIS_CONFIG_LIST.add(new RedisConfig("repl-backlog-size", "10mb", 4, 0));
        // master在没有slave的情况下释放BACKLOG的时间多久:默认:3600,配置为:7200
        REDIS_CONFIG_LIST.add(new RedisConfig("repl-backlog-ttl", "3600", 4, 0));
        // 客户端最大连接数
        REDIS_CONFIG_LIST.add(new RedisConfig("maxclients", "10000", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("watchdog-period", "0", 4, 0));
        // slave的优先级,影响sentinel/cluster晋升master操作,0永远不晋升
        REDIS_CONFIG_LIST.add(new RedisConfig("slave-priority", "100", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("slave-announce-port", "0", 4, 0));
        // 当slave数量小于min-slaves-to-write，且延迟小于等于min-slaves-max-lag时， master停止写入操作
        REDIS_CONFIG_LIST.add(new RedisConfig("min-slaves-to-write", "0", 4, 0));
        // 当slave服务器和master服务器失去连接后，或者当数据正在复制传输的时候，如果此参数值设置yes，slave服务器可以继续接受客户端的请求
        REDIS_CONFIG_LIST.add(new RedisConfig("min-slaves-max-lag", "10", 4, 0));
        // 执行后台task数量,默认:10
        REDIS_CONFIG_LIST.add(new RedisConfig("hz", "10", 4, 0));
        // 集群节点超时时间,默认15秒
        REDIS_CONFIG_LIST.add(new RedisConfig("cluster-node-timeout", "15000", 4, 0));
        // 主从迁移至少需要的从节点数,默认1个
        REDIS_CONFIG_LIST.add(new RedisConfig("cluster-migration-barrier", "1", 4, 0));
        // 从节点延迟有效性判断因子,默认10秒
        REDIS_CONFIG_LIST.add(new RedisConfig("cluster-slave-validity-factor", "10", 4, 0));
        // 无盘复制延时
        REDIS_CONFIG_LIST.add(new RedisConfig("repl-diskless-sync-delay", "5", 4, 0));
        // 检测客户端是否健康周期,默认关闭
        REDIS_CONFIG_LIST.add(new RedisConfig("tcp-keepalive", "300", 4, 0));
        // 节点部分失败期间,其他节点是否继续工作
        REDIS_CONFIG_LIST.add(new RedisConfig("cluster-require-full-coverage", "yes", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("cluster-slave-no-failover", "no", 4, 0));
        // 是否在后台aof文件rewrite期间调用fsync,默认调用,修改为yes,防止可能fsync阻塞,但可能丢失rewrite期间的数据
        REDIS_CONFIG_LIST.add(new RedisConfig("no-appendfsync-on-rewrite", "no", 4, 0));
        // 当slave服务器和master服务器失去连接后，或者当数据正在复制传输的时候，如果此参数值设置
        REDIS_CONFIG_LIST.add(new RedisConfig("slave-serve-stale-data", "yes", 4, 0));
        // slave服务器节点是否只读,cluster的slave节点默认读写都不可用,需要调用readonly开启可读模式
        REDIS_CONFIG_LIST.add(new RedisConfig("slave-read-only", "yes", 4, 0));
        // 当bgsave快照操作出错时停止写数据到磁盘
        REDIS_CONFIG_LIST.add(new RedisConfig("stop-writes-on-bgsave-error", "", 4, 0));
        // 是否守护进程, 默认no
        REDIS_CONFIG_LIST.add(new RedisConfig("daemonize", "no", 4, 0));
        // rdb是否压缩
        REDIS_CONFIG_LIST.add(new RedisConfig("rdbcompression", "yes", 4, 0));
        // rdb校验和
        REDIS_CONFIG_LIST.add(new RedisConfig("rdbchecksum", "yes", 4, 0));
        // 是否激活重置哈希,默认:yes
        REDIS_CONFIG_LIST.add(new RedisConfig("activerehashing", "yes", 4, 0));
        // 自动内存碎片整理, 默认no
        REDIS_CONFIG_LIST.add(new RedisConfig("activedefrag", "no", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("protected-mode", "yes", 4, 0));
        // 是否禁用socket的NO_DELAY,默认关闭，影响主从延迟
        REDIS_CONFIG_LIST.add(new RedisConfig("repl-disable-tcp-nodelay", "no", 4, 0));
        // 开启无盘复制
        REDIS_CONFIG_LIST.add(new RedisConfig("repl-diskless-sync", "no", 4, 0));
        // aof rewrite过程中,是否采取增量文件同步策略,默认:yes
        REDIS_CONFIG_LIST.add(new RedisConfig("aof-rewrite-incremental-fsync", "yes", 4, 0));
        // 加载aof文件时，是否忽略aof文件不完整的情况，是否Redis正常启动
        REDIS_CONFIG_LIST.add(new RedisConfig("aof-load-truncated", "yes", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("aof-use-rdb-preamble", "no", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("lazyfree-lazy-eviction", "no", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("lazyfree-lazy-expire", "no", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("lazyfree-lazy-server-del", "no", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("slave-lazy-flush", "no", 4, 0));
        // 内存不够时,淘汰策略,默认:volatile-lru
        REDIS_CONFIG_LIST.add(new RedisConfig("maxmemory-policy", "noeviction", 4, 0));
        // 日志级别
        REDIS_CONFIG_LIST.add(new RedisConfig("loglevel", "notice", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("supervised", "no", 4, 0));
        // 默认:aof每秒同步一次(everysec), redis-manager 默认 no
        REDIS_CONFIG_LIST.add(new RedisConfig("appendfsync", "no", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("syslog-facility", "local0", 4, 0));
        // 开启append only持久化模式
        REDIS_CONFIG_LIST.add(new RedisConfig("appendonly", "no", 4, 0));
        // 工作目录
        REDIS_CONFIG_LIST.add(new RedisConfig("dir", "{dir}", 4, 0));
        // 900秒有一次修改做bgsave, 300秒有10次修改做bgsave, 60秒有10000次修改做bgsave
        REDIS_CONFIG_LIST.add(new RedisConfig("save", "900 1 300 10 60 10000", 4, 0));
        // 客户端输出缓冲区限制(客户端)
        REDIS_CONFIG_LIST.add(new RedisConfig("client-output-buffer-limit", "normal", 4, 0));
        // 客户端输出缓冲区限制(复制)
        REDIS_CONFIG_LIST.add(new RedisConfig("client-output-buffer-limit", "slave 268435456 67108864 60", 4, 0));
        // 客户端输出缓冲区限制(发布订阅)
        REDIS_CONFIG_LIST.add(new RedisConfig("client-output-buffer-limit", "pubsub 33554432 8388608 60", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("unixsocketperm", "0", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("slaveof", "", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("notify-keyspace-events", "", 4, 0));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("bind", "", 4, 0));
    }


    /**
     * key
     * value
     * version: 4, 5
     * mode: 0=普通节点; 1=cluster
     */
    public static class RedisConfig {

        private String configKey;

        private String configValue;

        private int version;

        private int mode;

        public RedisConfig() {
        }

        public RedisConfig(String configKey, String configValue, int version, int mode) {
            this.configKey = configKey;
            this.configValue = configValue;
            this.version = version;
            this.mode = mode;
        }

        public String getConfigKey() {
            return configKey;
        }

        public void setConfigKey(String configKey) {
            this.configKey = configKey;
        }

        public String getConfigValue() {
            return configValue;
        }

        public void setConfigValue(String configValue) {
            this.configValue = configValue;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public int getMode() {
            return mode;
        }

        public void setMode(int mode) {
            this.mode = mode;
        }
    }


}
