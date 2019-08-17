package com.newegg.ec.redis.util;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * redis.conf
 * <p>
 * 维护 redis 3, 4, 5 配置文件
 * <p>
 *
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public class RedisConfigUtil {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfigUtil.class);

    private static final List<RedisConfig> REDIS_CONFIG_LIST = new ArrayList<>();

    public static final String REDIS_CONF = "redis.conf";

    private static final String REQUIRE_PASS = "requirepass";

    public static final int NORMAL_TYPE = 0;

    public static final int STANDALONE_TYPE = 1;

    public static final int CLUSTER_TYPE = 2;

    public RedisConfigUtil() {
    }

    public static void generateRedis4ClusterConfig(String path, String requirePass) throws IOException {
        generateRedisConfig(path, 4, CLUSTER_TYPE, requirePass);
    }

    public static void generateRedis4NormalConfig(String path, String requirePass) throws IOException {
        generateRedisConfig(path, 4, NORMAL_TYPE, requirePass);
    }

    public static void generateRedis5ClusterConfig(String path, String requirePass) throws IOException {
        generateRedisConfig(path, 5, CLUSTER_TYPE, requirePass);
    }

    public static void generateRedis5NormalConfig(String path, String requirePass) throws IOException {
        generateRedisConfig(path, 5, NORMAL_TYPE, requirePass);
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
                if (Strings.isNullOrEmpty(configValue)) {
                    configValue = "\"\"";
                }
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
        /** 网络 */
        // 如果没有设置 bind 和 密码,只允许本地访问(默认开启)
        REDIS_CONFIG_LIST.add(new RedisConfig("protected-mode", "yes", 3, 0));
        // TCP 连接中已完成队列的长度(不能大于 Linux 系统定义 /proc/sys/net/core/somaxconn 中的值)
        REDIS_CONFIG_LIST.add(new RedisConfig("tcp-backlog", "511", 3, 0));
        // 指定 Unix 套接字的位置,没有设置不监听 Unix 套接字
        /*REDIS_CONFIG_LIST.add(new RedisConfig("unixsocket", "/tmp/redis.sock", 3, 0));*/
        // 客户端闲置多少秒后关闭连接,默认为0(永不关闭),redis-manager 默认 3600
        REDIS_CONFIG_LIST.add(new RedisConfig("timeout", "3600", 3, 0));
        // 检测客户端是否健康周期,默认关闭
        REDIS_CONFIG_LIST.add(new RedisConfig("tcp-keepalive", "300", 3, 0));

        /** 通用 */
        // 是否守护进程, 默认 no
        REDIS_CONFIG_LIST.add(new RedisConfig("daemonize", "no", 3, 0));
        // 是否通过 upstart 或 systemd 管理守护进程,默认no没有服务监控
        REDIS_CONFIG_LIST.add(new RedisConfig("supervised", "no", 3, 0));
        // pid 文件路径(redis 会在启动时创建,退出时删除)
        REDIS_CONFIG_LIST.add(new RedisConfig("pidfile", "./redis_{port}.pid", 3, 0));
        // 日志级别
        REDIS_CONFIG_LIST.add(new RedisConfig("loglevel", "notice", 3, 0));
        // 日志文件名
        REDIS_CONFIG_LIST.add(new RedisConfig("logfile", "log.out", 3, 0));
        // 是否启动系统日志记录,默认 no
        REDIS_CONFIG_LIST.add(new RedisConfig("syslog-enabled", "no", 3, 0));
        // 指定运行用户名
        /*REDIS_CONFIG_LIST.add(new RedisConfig("syslog-ident", "redis", 3, 0));*/
        // 指明syslog的设备。必须是一个用户或者是 LOCAL0 ~ LOCAL7 之一
        /*REDIS_CONFIG_LIST.add(new RedisConfig("syslog-facility", "local0", 0, 0));*/
        // 可用的数据库数,默认值为16个,默认数据库为0
        REDIS_CONFIG_LIST.add(new RedisConfig("databases", "16", 3, 0));
        // 启动时是否显示 LOGO
        /*REDIS_CONFIG_LIST.add(new RedisConfig("always-show-logo", "yes", 3, 0));*/

        /** 快照 */
        // 900秒有一次修改做bgsave, 300秒有10次修改做bgsave, 60秒有10000次修改做bgsave
        REDIS_CONFIG_LIST.add(new RedisConfig("save", "900 1 300 10 60 10000", 3, 0));
        // 当 BGSAVE 快照操作出错时停止写数据到磁盘
        REDIS_CONFIG_LIST.add(new RedisConfig("stop-writes-on-bgsave-error", "yes", 3, 0));
        // RDB 是否压缩
        REDIS_CONFIG_LIST.add(new RedisConfig("rdbcompression", "yes", 3, 0));
        // 是否检验 RDB 文件校验和
        REDIS_CONFIG_LIST.add(new RedisConfig("rdbchecksum", "yes", 3, 0));
        // RDB文件默认名称,默认dump.rdb
        REDIS_CONFIG_LIST.add(new RedisConfig("dbfilename", "dump.rdb", 3, 0));

        /** 同步 */
        // 主从同步配置 TODO: cluster 模式是否需要？
        REDIS_CONFIG_LIST.add(new RedisConfig("slaveof", "", 3, 0));
        // master 密码, 与 requirepass 一致
        REDIS_CONFIG_LIST.add(new RedisConfig("masterauth", "", 3, 0));
        // slave 丢失连接时是否继续处理请求
        REDIS_CONFIG_LIST.add(new RedisConfig("slave-serve-stale-data", "no", 3, 0));
        // slave服务器节点是否只读,cluster的slave节点默认读写都不可用,需要调用 readonly 开启可读模式
        REDIS_CONFIG_LIST.add(new RedisConfig("slave-read-only", "yes", 3, 0));
        // 同步策略,磁盘或 socket,默认磁盘
        REDIS_CONFIG_LIST.add(new RedisConfig("repl-diskless-sync", "no", 3, 0));
        // 非磁盘同步时,同步延时(单位:秒)
        REDIS_CONFIG_LIST.add(new RedisConfig("repl-diskless-sync-delay", "5", 3, 0));
        // slave 根据指定时间给 master 发送 ping 请求,默认 10 秒
        REDIS_CONFIG_LIST.add(new RedisConfig("repl-ping-slave-period", "10", 3, 0));
        // 同步超时时间,默认 60 秒
        REDIS_CONFIG_LIST.add(new RedisConfig("repl-timeout", "60", 3, 0));
        // 是否在 slave 套接字发送 SYNC 之后禁用 TCP_NODELAY
        REDIS_CONFIG_LIST.add(new RedisConfig("repl-disable-tcp-nodelay", "no", 3, 0));
        // 复制缓存区,默认:1mb
        REDIS_CONFIG_LIST.add(new RedisConfig("repl-backlog-size", "1mb", 3, 0));
        // master 与所有 slave 断开时,多少秒后释放 backlog,默认 3600 秒
        REDIS_CONFIG_LIST.add(new RedisConfig("repl-backlog-ttl", "3600", 3, 0));
        // sentinel 选择 slave 升级为 master 的优先级(越小优先级越高)
        REDIS_CONFIG_LIST.add(new RedisConfig("slave-priority", "100", 3, 0));
        // 当 slave 数量小于 min-slaves-to-write,且延迟小于等于min-slaves-max-lag 时, master 停止写入操作
        REDIS_CONFIG_LIST.add(new RedisConfig("min-slaves-to-write", "0", 3, 0));
        // 当 slave 服务器和 master 服务器失去连接后,或者当数据正在复制传输的时候,如果此参数值设置 yes,slave 服务器可以继续接受客户端的请求
        REDIS_CONFIG_LIST.add(new RedisConfig("min-slaves-max-lag", "10", 3, 0));
        // slave 向 master 声明自己的 ip
        /*REDIS_CONFIG_LIST.add(new RedisConfig("slave-announce-ip", "", , 0));*/
        // slave 向 master 声明自己的端口
        /*REDIS_CONFIG_LIST.add(new RedisConfig("slave-announce-port", "0", , 0));*/

        /** 安全 */
        // redis 密码
        REDIS_CONFIG_LIST.add(new RedisConfig("requirepass", "", 3, 0));
        // 禁用 FLUSHALL
        REDIS_CONFIG_LIST.add(new RedisConfig("rename-command", "FLUSHALL \"\"", 3, 0));
        // 禁用 FLUSHDB
        REDIS_CONFIG_LIST.add(new RedisConfig("rename-command", "FLUSHDB \"\"", 3, 0));

        /** 客户端 */
        // 客户端最大连接数
        REDIS_CONFIG_LIST.add(new RedisConfig("maxclients", "10000", 3, 0));

        /** 内存管理 */
        // 当前实例最大可用内存
        REDIS_CONFIG_LIST.add(new RedisConfig("maxmemory", "10gb", 3, 0));
        // 内存淘汰策略,默认:volatile-lru
        REDIS_CONFIG_LIST.add(new RedisConfig("maxmemory-policy", "noeviction", 3, 0));
        // 淘汰样本配置,越大越精确,但是更消耗 CPU
        REDIS_CONFIG_LIST.add(new RedisConfig("maxmemory-samples", "5", 3, 0));

        /** 惰性清理 */
        // 对移出执行异步删除
        REDIS_CONFIG_LIST.add(new RedisConfig("lazyfree-lazy-eviction", "no", 4, 0));
        // 对已过期密钥执行异步删除
        REDIS_CONFIG_LIST.add(new RedisConfig("lazyfree-lazy-expire", "no", 4, 0));
        // 对更新值的命令执行异步删除
        REDIS_CONFIG_LIST.add(new RedisConfig("lazyfree-lazy-server-del", "no", 4, 0));
        // 在从属同步期间执行异步 flushDB
        REDIS_CONFIG_LIST.add(new RedisConfig("slave-lazy-flush", "no", 4, 0));

        /** AOF 配置 */
        // 是否开启 AOF
        REDIS_CONFIG_LIST.add(new RedisConfig("appendonly", "no", 3, 0));
        // AOF 文件名
        REDIS_CONFIG_LIST.add(new RedisConfig("appendfilename", "appendonly.aof", 3, 0));
        // AOF 写磁盘频率,默认 everysec, redis-manager 默认 no
        REDIS_CONFIG_LIST.add(new RedisConfig("appendfsync", "no", 3, 0));
        // 是否在 BGSAVE 或 BGREWRITEAOF 处理时阻止fsync()
        REDIS_CONFIG_LIST.add(new RedisConfig("no-appendfsync-on-rewrite", "no", 3, 0));
        // 重写 AOF 文件的比例条件,默认从100开始,统一机器下不同实例按4%递减
        REDIS_CONFIG_LIST.add(new RedisConfig("auto-aof-rewrite-percentage", "100", 3, 0));
        // 触发 REWRITE 的 AOF 文件最小阀值,默认64mb
        REDIS_CONFIG_LIST.add(new RedisConfig("auto-aof-rewrite-min-size", "64mb", 3, 0));
        // 加载 AOF 文件时,是否忽略 AOF 文件不完整的情况,是否正常启动
        REDIS_CONFIG_LIST.add(new RedisConfig("aof-load-truncated", "yes", 3, 0));
        // redis4: RDB-AOF 混合持久化功能,默认是关闭 启用该功能需开启 AOF 持久化,并将 aof-use-rdb-preamble 置为 yes
        REDIS_CONFIG_LIST.add(new RedisConfig("aof-use-rdb-preamble", "no", 4, 0));

        /** LUA 脚本 */
        // Lua脚本最长的执行时间,单位:毫秒
        /*REDIS_CONFIG_LIST.add(new RedisConfig("lua-time-limit", "5000", 3, 0));*/

        /** 集群 */
        // 是否开启集群
        REDIS_CONFIG_LIST.add(new RedisConfig("cluster-enabled", "yes", 4, CLUSTER_TYPE));
        // 节点配置文件
        REDIS_CONFIG_LIST.add(new RedisConfig("cluster-config-file", "node.conf", 4, CLUSTER_TYPE));
        // 集群节点超时时间,默认15秒
        REDIS_CONFIG_LIST.add(new RedisConfig("cluster-node-timeout", "15000", 3, CLUSTER_TYPE));
        // 从节点延迟有效性判断因子,默认10秒
        REDIS_CONFIG_LIST.add(new RedisConfig("cluster-slave-validity-factor", "10", 3, CLUSTER_TYPE));
        // 至少有多少个 slave 时，slave 才能成为 master
        REDIS_CONFIG_LIST.add(new RedisConfig("cluster-migration-barrier", "1", 3, CLUSTER_TYPE));
        // 集群检测到至少有 1 个 hash slot 不可用，则停止查询服务
        REDIS_CONFIG_LIST.add(new RedisConfig("cluster-require-full-coverage", "yes", 3, CLUSTER_TYPE));
        //
        REDIS_CONFIG_LIST.add(new RedisConfig("cluster-slave-no-failover", "no", 3, 0));

        /** 集群 Docker/NAT 支持 */
        /*REDIS_CONFIG_LIST.add(new RedisConfig("cluster-announce-ip", "", 3, 0));*/
        /*REDIS_CONFIG_LIST.add(new RedisConfig("cluster-announce-port", "", 3, 0));*/
        /*REDIS_CONFIG_LIST.add(new RedisConfig("cluster-announce-bus-port", "", 3, 0));*/

        /** 慢查询日志 */
        // 慢查询被记录的阀值,默认10毫秒
        REDIS_CONFIG_LIST.add(new RedisConfig("slowlog-log-slower-than", "10000", 3, 0));
        // 最多记录慢查询的条数
        REDIS_CONFIG_LIST.add(new RedisConfig("slowlog-max-len", "500", 3, 0));

        /** 延时监控 */
        // 0 表示关闭,单位:微秒
        REDIS_CONFIG_LIST.add(new RedisConfig("latency-monitor-threshold", "0", 3, 0));

        /** 键空间通知 */
        // 默认处于关闭状态
        /*REDIS_CONFIG_LIST.add(new RedisConfig("notify-keyspace-events", "\"\"", 3, 0));*/

        /** 高级配置 */
        // ziplist 到 hash 转换的阈值
        REDIS_CONFIG_LIST.add(new RedisConfig("hash-max-ziplist-entries", "512", 3, 0));
        // ziplist 到 hash 转换的阈值
        REDIS_CONFIG_LIST.add(new RedisConfig("hash-max-ziplist-value", "64", 3, 0));
        REDIS_CONFIG_LIST.add(new RedisConfig("list-max-ziplist-size", "-2", 3, 0));
        REDIS_CONFIG_LIST.add(new RedisConfig("list-compress-depth", "0", 3, 0));
        // set 数据结构优化参数
        REDIS_CONFIG_LIST.add(new RedisConfig("set-max-intset-entries", "512", 3, 0));
        // zset 数据结构优化参数
        REDIS_CONFIG_LIST.add(new RedisConfig("zset-max-ziplist-entries", "128", 3, 0));
        // zset 数据结构优化参数
        REDIS_CONFIG_LIST.add(new RedisConfig("zset-max-ziplist-value", "64", 3, 0));
        // HyperLogLog稀疏表示限制设置
        REDIS_CONFIG_LIST.add(new RedisConfig("hll-sparse-max-bytes", "3000", 3, 0));
        // 是否激活重置哈希,默认:yes
        REDIS_CONFIG_LIST.add(new RedisConfig("activerehashing", "yes", 3, 0));
        // 客户端输出缓冲区限制(客户端)
        REDIS_CONFIG_LIST.add(new RedisConfig("client-output-buffer-limit", "normal", 3, 0));
        // 客户端输出缓冲区限制(复制)
        REDIS_CONFIG_LIST.add(new RedisConfig("client-output-buffer-limit", "slave 268435456 67108864 60", 3, 0));
        // 客户端输出缓冲区限制(发布订阅)
        REDIS_CONFIG_LIST.add(new RedisConfig("client-output-buffer-limit", "pubsub 33554432 8388608 60", 3, 0));
        // 单个客户端查询缓冲区的最大大小,默认 1gb
        REDIS_CONFIG_LIST.add(new RedisConfig("client-query-buffer-limit", "1gb", 4, 0));
        // 批量请求的单个 string 的长度限制在512MB
        REDIS_CONFIG_LIST.add(new RedisConfig("proto-max-bulk-len", "215mb", 4, 0));
        // 执行后台task数量,默认:10
        REDIS_CONFIG_LIST.add(new RedisConfig("hz", "10", 3, 0));
        // AOF REWRITE 过程中,是否采取增量文件同步策略,默认:yes
        REDIS_CONFIG_LIST.add(new RedisConfig("aof-rewrite-incremental-fsync", "yes", 3, 0));
        // 设置日志因素，这确定键命中数以使键计数器饱和
        REDIS_CONFIG_LIST.add(new RedisConfig("lfu-log-factor", "10", 4, 0));
        // 减少键计数器的时间，单位:分钟
        REDIS_CONFIG_LIST.add(new RedisConfig("lfu-decay-time", "1", 4, 0));
        /*REDIS_CONFIG_LIST.add(new RedisConfig("watchdog-period", "0", 3, 0));*/
        // unixsocket /tmp/redis.sock
        REDIS_CONFIG_LIST.add(new RedisConfig("unixsocketperm", "755", 3, 0));

        /** 内存碎片整理 */
        // 内存碎片整理, 默认no
        REDIS_CONFIG_LIST.add(new RedisConfig("activedefrag", "no", 4, 0));
        // 内存碎片达到多少的时候开启整理
        REDIS_CONFIG_LIST.add(new RedisConfig("active-defrag-ignore-bytes", "104857600", 4, 0));
        // 启动碎片整理的碎片最低百分比
        REDIS_CONFIG_LIST.add(new RedisConfig("active-defrag-threshold-lower", "10", 4, 0));
        // 启动碎片整理的碎片最低百分比
        REDIS_CONFIG_LIST.add(new RedisConfig("active-defrag-threshold-upper", "100", 4, 0));
        // 碎片整理的最少精力,以 CPU 百分比为单位
        REDIS_CONFIG_LIST.add(new RedisConfig("active-defrag-cycle-min", "25", 4, 0));
        // 碎片整理的最多精力,以 CPU 百分比为单位
        REDIS_CONFIG_LIST.add(new RedisConfig("active-defrag-cycle-max", "75", 4, 0));

        /** redis version 5 */
        // 流数据结构是节点的基数树,这些节点对内部的多个项进行编码,使用此配置指定基数树中单个节点的最大大小(以字节为单位),如果设置为 0，则树节点的大小是不受限制的
        REDIS_CONFIG_LIST.add(new RedisConfig("stream-node-max-bytes", "4096", 5, 0));
        // 流数据结构是节点的基数树，这些节点对内部的多个项进行编码。使用此配置指定在追加新的流条目时切换到新节点之前单个节点可包含的项的最大数目。如果设置为 0，则树节点中的项数是不受限制的
        REDIS_CONFIG_LIST.add(new RedisConfig("stream-node-max-bytes", "100", 5, 0));
        // 将从主字典扫描中处理的最大 set/hash/zset/list 字段数
        REDIS_CONFIG_LIST.add(new RedisConfig("active-defrag-max-scan-fields", "1000", 5, 0));
        // 是否始终在 Lua 脚本中启用 Lua 效果复制
        REDIS_CONFIG_LIST.add(new RedisConfig("lua-replicate-commands", "yes", 5, 0));
        // 确定副本是否通过不移出独立于主实例的项来忽略 maxmemory 设置
        REDIS_CONFIG_LIST.add(new RedisConfig("replica-ignore-maxmemory", "yes", 5, 0));
        // 重命名

        // 在副本同步期间执行异步 flushDB
        REDIS_CONFIG_LIST.add(new RedisConfig("replica-lazy-flush", "no", 5, 0));
        // 对于 Redis 只读副本：如果客户端的输出缓冲区达到指定字节数，则客户端将断开连接
        REDIS_CONFIG_LIST.add(new RedisConfig("client-output-buffer-limit-replica-hard-limit", "1024000", 5, 0));
        // 对于 Redis 只读副本：如果客户端的输出缓冲区达到指定字节数，仅当此条件保持 client-output-buffer-limit-replica-soft-seconds 时间时，客户端将断开连接
        REDIS_CONFIG_LIST.add(new RedisConfig("client-output-buffer-limit-replica-soft-limit", "1024000", 5, 0));
        // 对于 Redis 只读副本：如果客户端的输出缓冲区保持 client-output-buffer-limit-replica-soft-limit 字节的时间长于此秒数，则客户端将断开连接
        REDIS_CONFIG_LIST.add(new RedisConfig("client-output-buffer-limit-replica-soft-seconds", "60", 5, 0));
        // 确定 Redis 中的只读副本是否可以具有自己的只读副本
        REDIS_CONFIG_LIST.add(new RedisConfig("replica-allow-chaining", "no", 5, 0));
        // 使主节点可以从客户端接受写入所必需的可用只读副本的最小数目。如果可用副本数下降到低于此数字，则主节点不再接受写入请求。
        //如果此参数或 min-replicas-max-lag 是 0，则主节点始终接受写入请求（即使无副本可用）
        REDIS_CONFIG_LIST.add(new RedisConfig("min-replicas-to-write", "0", 5, 0));
        // 主节点必须从只读副本收到 Ping 请求的秒数。如果此时间量已过，但主节点未收到 Ping，则不再将副本视为可用。如果可用副本数下降到低于 min-replicas-to-write，则主节点在此时停止接受写入。
        //如果此参数或 min-replicas-to-write 是 0，则主节点始终接受写入请求（即使无副本可用）
        REDIS_CONFIG_LIST.add(new RedisConfig("min-replicas-max-lag", "10", 5, 0));
        // 如果启用，尝试写入只读副本的客户端将会断开连接
        REDIS_CONFIG_LIST.add(new RedisConfig("close-on-replica-write", "yes", 5, 0));

        /** 自定义配置 */
        // ip
        REDIS_CONFIG_LIST.add(new RedisConfig("bind", "{ip}", 3, 0));
        // 工作目录
        REDIS_CONFIG_LIST.add(new RedisConfig("dir", "{dir}", 3, 0));
        // 端口
        REDIS_CONFIG_LIST.add(new RedisConfig("port", "{port}", 3, 0));
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
