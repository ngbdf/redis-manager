package com.newegg.ec.redis.util;

import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Machine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.newegg.ec.redis.util.SignUtil.SLASH;
import static com.newegg.ec.redis.util.SignUtil.SPACE;

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

    private static final List<RedisConfig> REDIS_CONFIG_LIST = new ArrayList<>();

    private static final String UTF_8 = "UTF-8";

    public static final String REDIS_CONF = "redis.conf";

    public static final String REQUIRE_PASS = "requirepass";

    public static final String MASTER_AUTH = "masterauth";

    public static final String BIND = "bind";

    public static final String PORT = "port";

    public static final String DIR = "dir";

    public static final int NORMAL_TYPE = 0;

    public static final int STANDALONE_TYPE = 1;

    public static final int CLUSTER_TYPE = 2;

    public RedisConfigUtil() {
    }

    /**
     * dir, bind, port 等变量赋值
     *
     * @param machine
     * @param path
     * @return
     */
    public static void variableAssignment(Machine machine, String path, Map<String, String> configs, boolean sudo) throws Exception {
        StringBuffer commands = new StringBuffer();
        // 进入配置文件所在目录
        commands.append("cd " + path).append(";");
        String cd = SSH2Util.execute(machine, commands.toString());
        if (!Strings.isNullOrEmpty(cd)) {
            throw new RuntimeException(cd);
        }
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (sudo) {
                commands.append("sudo ");
            }
            commands.append("sed -i 's#{" + key + "}#" + value + "#g' " + REDIS_CONF + ";");
        }
        String result = SSH2Util.execute(machine, commands.toString());
        if (!Strings.isNullOrEmpty(result)) {
            throw new RuntimeException(result);
        }
    }

    public static void generateRedisConfig(String path, int mode) throws IOException {
        File tempPath = new File(path);
        if (!tempPath.mkdir()) {
            throw new RuntimeException(path + " mkdir failed.");
        }
        File file = new File(path + REDIS_CONF);
        if (file.exists()) {
            if (!file.delete()) {
                throw new RuntimeException(file.getName() + " delete failed.");
            }
        }
        if (!file.createNewFile()) {
            throw new RuntimeException(file.getName() + " create failed.");
        }
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file, true), UTF_8));
        for (RedisConfig redisConfig : getRedisConfig(mode)) {
            String configKey = redisConfig.getConfigKey();
            String configValue = redisConfig.getConfigValue();
            bufferedWriter.write(configKey + SPACE + configValue);
            bufferedWriter.newLine();
        }
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    public static List<RedisConfig> getRedisConfig(int mode) {
        List<RedisConfig> configs = new ArrayList<>();
        for (RedisConfig redisConfig : REDIS_CONFIG_LIST) {
            String configKey = redisConfig.getConfigKey();
            if (Objects.equals(REQUIRE_PASS, configKey) || Objects.equals(MASTER_AUTH, configKey)) {
                continue;
            }
            int item = redisConfig.getMode();
            boolean type = item == mode || item == NORMAL_TYPE;
            if (redisConfig.isEnable() && type) {
                configs.add(redisConfig);
            }
        }
        return configs;
    }

    public static List<RedisConfig> getAllRedisConfig() {
        return REDIS_CONFIG_LIST;
    }

    /**
     * init all config
     */
    static {
        /** 网络 */
        // 如果没有设置 bind 和 密码,只允许本地访问(默认开启)
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "protected-mode", "yes", NORMAL_TYPE, "如果没有设置 bind 和 密码,只允许本地访问(默认开启)"));
        // TCP 连接中已完成队列的长度(不能大于 Linux 系统定义 /proc/sys/net/core/somaxconn 中的值)
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "tcp-backlog", "511", NORMAL_TYPE, "TCP 连接中已完成队列的长度(不能大于 Linux 系统定义 /proc/sys/net/core/somaxconn 中的值)"));
        // 指定 Unix 套接字的位置,没有设置不监听 Unix 套接字
        /*REDIS_CONFIG_LIST.add(new RedisConfig(true, false, "unixsocket", "/tmp/redis.sock", 3, NORMAL_TYPE));*/
        // 客户端闲置多少秒后关闭连接,默认为0(永不关闭),redis-manager 默认 3600
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "timeout", "3600", NORMAL_TYPE, "客户端闲置多少秒后关闭连接,默认为0(永不关闭),redis-manager 默认 3600"));
        // 检测客户端是否健康周期,默认关闭
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "tcp-keepalive", "300", NORMAL_TYPE, "检测客户端是否健康周期,默认关闭"));

        /** 通用 */
        // 是否守护进程, 默认 no
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "daemonize", "no", NORMAL_TYPE));
        // 是否通过 upstart 或 systemd 管理守护进程,默认no没有服务监控
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "supervised", "no", NORMAL_TYPE));
        // pid 文件路径(redis 会在启动时创建,退出时删除)
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "pidfile", "./redis_{port}.pid", NORMAL_TYPE));
        // 日志级别
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "loglevel", "notice", NORMAL_TYPE));
        // 日志文件名
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "logfile", "log.out", NORMAL_TYPE));
        // 是否启动系统日志记录,默认 no
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "syslog-enabled", "no", NORMAL_TYPE));
        // 指定运行用户名
        /*REDIS_CONFIG_LIST.add(new RedisConfig(true, false, , "syslog-ident", "redis", NORMAL_TYPE));*/
        // 指明syslog的设备。必须是一个用户或者是 LOCAL0 ~ LOCAL7 之一
        /*REDIS_CONFIG_LIST.add(new RedisConfig(true, false, "syslog-facility", "local0", NORMAL_TYPE, NORMAL_TYPE));*/
        // 可用的数据库数,默认值为16个,默认数据库为0
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "databases", "16", NORMAL_TYPE));
        // 启动时是否显示 LOGO
        /*REDIS_CONFIG_LIST.add(new RedisConfig(true, false, "always-show-logo", "yes", NORMAL_TYPE));*/

        /** 快照 */
        // 900秒有一次修改做bgsave, 300秒有10次修改做bgsave, 60秒有10000次修改做bgsave
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "save", "900 1 300 10 60 10000", NORMAL_TYPE));
        // 当 BGSAVE 快照操作出错时停止写数据到磁盘
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "stop-writes-on-bgsave-error", "yes", NORMAL_TYPE));
        // RDB 是否压缩
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "rdbcompression", "yes", NORMAL_TYPE));
        // 是否检验 RDB 文件校验和
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "rdbchecksum", "yes", NORMAL_TYPE));
        // RDB文件默认名称,默认dump.rdb
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "dbfilename", "dump.rdb", NORMAL_TYPE));

        /** 同步 */
        // 主从同步配置
        /*REDIS_CONFIG_LIST.add(new RedisConfig(true, "slaveof", "\"\"", NORMAL_TYPE));*/
        // master 密码, 与 requirepass 一致
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "masterauth", "", NORMAL_TYPE));
        // slave 丢失连接时是否继续处理请求
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "slave-serve-stale-data", "no", NORMAL_TYPE));
        // slave服务器节点是否只读,cluster的slave节点默认读写都不可用,需要调用 readonly 开启可读模式
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "slave-read-only", "yes", NORMAL_TYPE));
        // 同步策略,磁盘或 socket,默认磁盘
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "repl-diskless-sync", "no", NORMAL_TYPE));
        // 非磁盘同步时,同步延时(单位:秒)
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "repl-diskless-sync-delay", "5", NORMAL_TYPE));
        // slave 根据指定时间给 master 发送 ping 请求,默认 10 秒
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "repl-ping-slave-period", "10", NORMAL_TYPE));
        // 同步超时时间,默认 60 秒
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "repl-timeout", "60", NORMAL_TYPE));
        // 是否在 slave 套接字发送 SYNC 之后禁用 TCP_NODELAY
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "repl-disable-tcp-nodelay", "no", NORMAL_TYPE));
        // 复制缓存区,默认:1mb
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "repl-backlog-size", "1mb", NORMAL_TYPE));
        // master 与所有 slave 断开时,多少秒后释放 backlog,默认 3600 秒
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "repl-backlog-ttl", "3600", NORMAL_TYPE));
        // sentinel 选择 slave 升级为 master 的优先级(越小优先级越高)
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "slave-priority", "100", NORMAL_TYPE));
        // 当 slave 数量小于 min-slaves-to-write,且延迟小于等于min-slaves-max-lag 时, master 停止写入操作
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "min-slaves-to-write", "0", NORMAL_TYPE));
        // 当 slave 服务器和 master 服务器失去连接后,或者当数据正在复制传输的时候,如果此参数值设置 yes,slave 服务器可以继续接受客户端的请求
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "min-slaves-max-lag", "10", NORMAL_TYPE));
        // slave 向 master 声明自己的 ip
        /*REDIS_CONFIG_LIST.add(new RedisConfig(true, "slave-announce-ip", "", , NORMAL_TYPE));*/
        // slave 向 master 声明自己的端口
        /*REDIS_CONFIG_LIST.add(new RedisConfig(true, "slave-announce-port", "0", , NORMAL_TYPE));*/

        /** 安全 */
        // redis 密码
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "requirepass", "", NORMAL_TYPE));
        // 禁用 FLUSHALL
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "rename-command", "FLUSHALL \"\"", NORMAL_TYPE));
        // 禁用 FLUSHDB
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "rename-command", "FLUSHDB \"\"", NORMAL_TYPE));

        /** 客户端 */
        // 客户端最大连接数
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "maxclients", "10000", NORMAL_TYPE));

        /** 内存管理 */
        // 当前实例最大可用内存
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "maxmemory", "10gb", NORMAL_TYPE));
        // 内存淘汰策略,默认:volatile-lru
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "maxmemory-policy", "noeviction", NORMAL_TYPE));
        // 淘汰样本配置,越大越精确,但是更消耗 CPU
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "maxmemory-samples", "5", NORMAL_TYPE));

        /** 惰性清理 */
        // redis4,对移出执行异步删除
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "lazyfree-lazy-eviction", "no", NORMAL_TYPE));
        // redis4,对已过期密钥执行异步删除
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "lazyfree-lazy-expire", "no", NORMAL_TYPE));
        // redis4,对更新值的命令执行异步删除
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "lazyfree-lazy-server-del", "no", NORMAL_TYPE));
        // redis4,在从属同步期间执行异步 flushDB
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "slave-lazy-flush", "no", NORMAL_TYPE));

        /** AOF 配置 */
        // 是否开启 AOF
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "appendonly", "no", NORMAL_TYPE));
        // AOF 文件名
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "appendfilename", "appendonly.aof", NORMAL_TYPE));
        // AOF 写磁盘频率,默认 everysec, redis-manager 默认 no
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "appendfsync", "no", NORMAL_TYPE));
        // 是否在 BGSAVE 或 BGREWRITEAOF 处理时阻止fsync()
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "no-appendfsync-on-rewrite", "no", NORMAL_TYPE));
        // 重写 AOF 文件的比例条件,默认从100开始,统一机器下不同实例按4%递减
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "auto-aof-rewrite-percentage", "100", NORMAL_TYPE));
        // 触发 REWRITE 的 AOF 文件最小阀值,默认64mb
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "auto-aof-rewrite-min-size", "64mb", NORMAL_TYPE));
        // 加载 AOF 文件时,是否忽略 AOF 文件不完整的情况,是否正常启动
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "aof-load-truncated", "yes", NORMAL_TYPE));
        // redis4,RDB-AOF 混合持久化功能,默认是关闭 启用该功能需开启 AOF 持久化,并将 aof-use-rdb-preamble 置为 yes
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "aof-use-rdb-preamble", "no", NORMAL_TYPE));

        /** LUA 脚本 */
        // Lua脚本最长的执行时间,单位:毫秒
        /*REDIS_CONFIG_LIST.add(new RedisConfig(false, "lua-time-limit", "5000", NORMAL_TYPE));*/

        /** 集群 */
        // 是否开启集群
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "cluster-enabled", "yes", CLUSTER_TYPE));
        // 节点配置文件
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "cluster-config-file", "node.conf", CLUSTER_TYPE));
        // 集群节点超时时间,默认15秒
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "cluster-node-timeout", "15000", CLUSTER_TYPE));
        // 从节点延迟有效性判断因子,默认10秒
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "cluster-slave-validity-factor", "10", CLUSTER_TYPE));
        // 至少有多少个 slave 时，slave 才能成为 master
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "cluster-migration-barrier", "1", CLUSTER_TYPE));
        // 集群检测到至少有 1 个 hash slot 不可用，则停止查询服务
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "cluster-require-full-coverage", "yes", CLUSTER_TYPE));
        /*REDIS_CONFIG_LIST.add(new RedisConfig(false, "cluster-slave-no-failover", "no", CLUSTER_TYPE));*/

        /** 集群 Docker/NAT 支持 */
        /*REDIS_CONFIG_LIST.add(new RedisConfig(false, "cluster-announce-ip", "", NORMAL_TYPE));*/
        /*REDIS_CONFIG_LIST.add(new RedisConfig(false, "cluster-announce-port", "", NORMAL_TYPE));*/
        /*REDIS_CONFIG_LIST.add(new RedisConfig(false, "cluster-announce-bus-port", "", NORMAL_TYPE));*/

        /** 慢查询日志 */
        // 慢查询被记录的阀值,默认10毫秒
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "slowlog-log-slower-than", "10000", NORMAL_TYPE));
        // 最多记录慢查询的条数
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "slowlog-max-len", "500", NORMAL_TYPE));

        /** 延时监控 */
        // 0 表示关闭,单位:微秒
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "latency-monitor-threshold", "0", NORMAL_TYPE));

        /** 键空间通知 */
        // 默认处于关闭状态
        /*REDIS_CONFIG_LIST.add(new RedisConfig(false, "notify-keyspace-events", "\"\"", NORMAL_TYPE));*/

        /** 高级配置 */
        // ziplist 到 hash 转换的阈值
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "hash-max-ziplist-entries", "512", NORMAL_TYPE));
        // ziplist 到 hash 转换的阈值
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "hash-max-ziplist-value", "64", NORMAL_TYPE));
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "list-max-ziplist-size", "-2", NORMAL_TYPE));
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "list-compress-depth", "0", NORMAL_TYPE));
        // set 数据结构优化参数
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "set-max-intset-entries", "512", NORMAL_TYPE));
        // zset 数据结构优化参数
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "zset-max-ziplist-entries", "128", NORMAL_TYPE));
        // zset 数据结构优化参数
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "zset-max-ziplist-value", "64", NORMAL_TYPE));
        // HyperLogLog稀疏表示限制设置
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "hll-sparse-max-bytes", "3000", NORMAL_TYPE));
        // 是否激活重置哈希,默认:yes
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "activerehashing", "yes", NORMAL_TYPE));
        // 客户端输出缓冲区限制(客户端)
        /*REDIS_CONFIG_LIST.add(new RedisConfig(false, "client-output-buffer-limit", "normal", NORMAL_TYPE));*/
        // 客户端输出缓冲区限制(复制)
        /*REDIS_CONFIG_LIST.add(new RedisConfig(false, "client-output-buffer-limit", "slave 268435456 67108864 60", NORMAL_TYPE));*/
        // 客户端输出缓冲区限制(发布订阅)
        /*REDIS_CONFIG_LIST.add(new RedisConfig(false, "client-output-buffer-limit", "pubsub 33554432 8388608 60", NORMAL_TYPE));*/
        // redis4,单个客户端查询缓冲区的最大大小,默认 1gb
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "client-query-buffer-limit", "1gb", NORMAL_TYPE));
        // redis4,批量请求的单个 string 的长度限制在512MB
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "proto-max-bulk-len", "215mb", NORMAL_TYPE));
        // 执行后台task数量,默认:10
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "hz", "10", NORMAL_TYPE));
        // AOF REWRITE 过程中,是否采取增量文件同步策略,默认:yes
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "aof-rewrite-incremental-fsync", "yes", NORMAL_TYPE));
        // redis4,设置日志因素，这确定键命中数以使键计数器饱和
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "lfu-log-factor", "10", NORMAL_TYPE));
        // redis4,减少键计数器的时间，单位:分钟
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "lfu-decay-time", "1", NORMAL_TYPE));
        /*REDIS_CONFIG_LIST.add(new RedisConfig(true, "watchdog-period", "0", NORMAL_TYPE));*/
        // unixsocket /tmp/redis.sock
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "unixsocketperm", "755", NORMAL_TYPE));

        /** 内存碎片整理 */
        // redis4,内存碎片整理, 默认no
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "activedefrag", "no", NORMAL_TYPE));
        // redis4,内存碎片达到多少的时候开启整理
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "active-defrag-ignore-bytes", "104857600", NORMAL_TYPE));
        // redis4,启动碎片整理的碎片最低百分比
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "active-defrag-threshold-lower", "10", NORMAL_TYPE));
        // redis4,启动碎片整理的碎片最低百分比
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "active-defrag-threshold-upper", "100", NORMAL_TYPE));
        // redis4,碎片整理的最少精力,以 CPU 百分比为单位
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "active-defrag-cycle-min", "25", NORMAL_TYPE));
        // redis4,碎片整理的最多精力,以 CPU 百分比为单位
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "active-defrag-cycle-max", "75", NORMAL_TYPE));

        /** redis version 5 */
        // redis5,流数据结构是节点的基数树,这些节点对内部的多个项进行编码,使用此配置指定基数树中单个节点的最大大小(以字节为单位),如果设置为 0，则树节点的大小是不受限制的
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "stream-node-max-bytes", "4096", NORMAL_TYPE));
        // redis5,流数据结构是节点的基数树，这些节点对内部的多个项进行编码。使用此配置指定在追加新的流条目时切换到新节点之前单个节点可包含的项的最大数目。如果设置为 0，则树节点中的项数是不受限制的
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "stream-node-max-bytes", "100", NORMAL_TYPE));
        // redis5,将从主字典扫描中处理的最大 set/hash/zset/list 字段数
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "active-defrag-max-scan-fields", "1000", NORMAL_TYPE));
        /*// redis5,是否始终在 Lua 脚本中启用 Lua 效果复制
        REDIS_CONFIG_LIST.add(new RedisConfig(false, "lua-replicate-commands", "yes", NORMAL_TYPE));
        // redis5,确定副本是否通过不移出独立于主实例的项来忽略 maxmemory 设置
        REDIS_CONFIG_LIST.add(new RedisConfig(false, "replica-ignore-maxmemory", "yes", NORMAL_TYPE));*/
        // 重命名

        /*// redis5,在副本同步期间执行异步 flushDB
        REDIS_CONFIG_LIST.add(new RedisConfig(false, "replica-lazy-flush", "no", NORMAL_TYPE));
        // redis5,对于 Redis 只读副本：如果客户端的输出缓冲区达到指定字节数，则客户端将断开连接
        REDIS_CONFIG_LIST.add(new RedisConfig(false, "client-output-buffer-limit-replica-hard-limit", "1024000", NORMAL_TYPE));
        // redis5,对于 Redis 只读副本：如果客户端的输出缓冲区达到指定字节数，仅当此条件保持 client-output-buffer-limit-replica-soft-seconds 时间时，客户端将断开连接
        REDIS_CONFIG_LIST.add(new RedisConfig(false, "client-output-buffer-limit-replica-soft-limit", "1024000", NORMAL_TYPE));
        // redis5,对于 Redis 只读副本：如果客户端的输出缓冲区保持 client-output-buffer-limit-replica-soft-limit 字节的时间长于此秒数，则客户端将断开连接
        REDIS_CONFIG_LIST.add(new RedisConfig(false, "client-output-buffer-limit-replica-soft-seconds", "60", NORMAL_TYPE));
        // redis5,确定 Redis 中的只读副本是否可以具有自己的只读副本
        REDIS_CONFIG_LIST.add(new RedisConfig(false, "replica-allow-chaining", "no", NORMAL_TYPE));
        // redis5,使主节点可以从客户端接受写入所必需的可用只读副本的最小数目。如果可用副本数下降到低于此数字，则主节点不再接受写入请求。
        //如果此参数或 min-replicas-max-lag 是 0，则主节点始终接受写入请求（即使无副本可用）
        REDIS_CONFIG_LIST.add(new RedisConfig(false, "min-replicas-to-write", "0", NORMAL_TYPE));
        // redis5,主节点必须从只读副本收到 Ping 请求的秒数。如果此时间量已过，但主节点未收到 Ping，则不再将副本视为可用。如果可用副本数下降到低于 min-replicas-to-write，则主节点在此时停止接受写入。
        //如果此参数或 min-replicas-to-write 是 0，则主节点始终接受写入请求（即使无副本可用）
        REDIS_CONFIG_LIST.add(new RedisConfig(false, "min-replicas-max-lag", "10", NORMAL_TYPE));
        // redis5,如果启用，尝试写入只读副本的客户端将会断开连接
        REDIS_CONFIG_LIST.add(new RedisConfig(false, "close-on-replica-write", "yes", NORMAL_TYPE));*/

        /** 自定义配置 */
        // ip
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "bind", "{bind}", NORMAL_TYPE));
        // 工作目录
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "dir", "{dir}", NORMAL_TYPE));
        // 端口
        REDIS_CONFIG_LIST.add(new RedisConfig(true, "port", "{port}", NORMAL_TYPE, "端口"));
    }


    /**
     * key
     * value
     * version: 5
     * mode: 0=普通节点; 1=cluster
     */
    public static class RedisConfig {

        /**
         *
         */
        private boolean enable;

        private String configKey;

        private String configValue;

        private int mode;

        private String desc;

        public RedisConfig() {
        }

        public RedisConfig(String configKey, String configValue, int mode) {
            this(true, configKey, configValue, mode, null);
        }

        public RedisConfig(boolean enable, String configKey, String configValue, int mode) {
            this(enable, configKey, configValue, mode, null);
        }

        public RedisConfig(boolean enable, String configKey, String configValue, int mode, String desc) {
            this.enable = enable;
            this.configKey = configKey;
            this.configValue = configValue;
            this.mode = mode;
            this.desc = desc;
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
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

        public int getMode() {
            return mode;
        }

        public void setMode(int mode) {
            this.mode = mode;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }


}
