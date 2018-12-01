package com.newegg.ec.cache.app.model;

import com.newegg.ec.cache.core.mysql.MysqlField;
import com.newegg.ec.cache.core.mysql.MysqlTable;

/**
 * Created by gl49 on 2018/4/21.
 */
@MysqlTable(name = "", autoCreate = false)
public class NodeInfo {
    @MysqlField(isPrimaryKey = true, field = "id", type = "int")
    private int id;
    @MysqlField(field = "connected_clients", type = "bigint")
    private long connectedClients;
    @MysqlField(field = "blocked_clients", type = "bigint")
    private long blockedClients;
    @MysqlField(field = "used_memory", type = "bigint")
    private long usedMemory;
    @MysqlField(field = "used_memory_rss", type = "bigint")
    private long usedMemoryRss;
    @MysqlField(field = "used_memory_peak", type = "bigint")
    private long usedMemoryPeak;
    @MysqlField(field = "mem_fragmentation_ratio", type = "float")
    private float memFragmentationRatio;
    @MysqlField(field = "aof_enabled", type = "bigint")
    private long aofEnabled;
    @MysqlField(field = "total_connections_received", type = "bigint")
    private long totalConnectionsReceived;
    @MysqlField(field = "total_commands_processed", type = "bigint")
    private long totalCommandsProcessed;
    @MysqlField(field = "instantaneous_ops_per_sec", type = "bigint")
    private long instantaneousOpsPerSec;
    @MysqlField(field = "total_net_input_bytes", type = "bigint")
    private long totalNetInputBytes;
    @MysqlField(field = "total_net_output_bytes", type = "bigint")
    private long totalNetOutputBytes;
    @MysqlField(field = "instantaneous_input_kbps", type = "float")
    private float instantaneousInputKbps;
    @MysqlField(field = "instantaneous_output_kbps", type = "float")
    private float instantaneousOutputKbps;
    @MysqlField(field = "rejected_connections", type = "bigint")
    private long rejectedConnections;
    @MysqlField(field = "sync_full", type = "bigint")
    private long syncFull;
    @MysqlField(field = "sync_partial_ok", type = "bigint")
    private long syncPartialOk;
    @MysqlField(field = "sync_partial_err", type = "bigint")
    private long syncPartialErr;
    @MysqlField(field = "expired_keys", type = "bigint")
    private long expiredKeys;
    @MysqlField(field = "evicted_keys", type = "bigint")
    private long evictedKeys;
    @MysqlField(field = "keyspace_hits", type = "bigint")
    private long keyspaceHits;
    @MysqlField(field = "keyspace_misses", type = "bigint")
    private long keyspaceMisses;
    @MysqlField(field = "pubsub_channels", type = "bigint")
    private long pubsubChannels;
    @MysqlField(field = "pubsub_patterns", type = "bigint")
    private long pubsubPatterns;
    @MysqlField(field = "latest_fork_usec", type = "bigint")
    private long latestForkUsec;
    @MysqlField(field = "migrate_cached_sockets", type = "bigint")
    private long migrateCachedSockets;
    @MysqlField(field = "used_cpu_sys", type = "float")
    private float usedCpuSys;
    @MysqlField(field = "used_cpu_user", type = "float")
    private float usedCpuUser;
    @MysqlField(field = "used_cpu_sys_children", type = "float")
    private float usedCpuSysChildren;
    @MysqlField(field = "used_cpu_user_children", type = "float")
    private float usedCpuUserChildren;
    @MysqlField(field = "total_keys", type = "bigint")
    private long totalKeys;
    @MysqlField(field = "expires", type = "bigint")
    private long expires;
    @MysqlField(field = "avg_ttl", type = "bigint")
    private long avgTtl;
    @MysqlField(field = "response_time", type = "bigint")
    private long responseTime;
    @MysqlField(field = "host", type = "varchar(30)")
    private String host;
    @MysqlField(field = "ip", type = "varchar(25)")
    private String ip;
    @MysqlField(field = "port", type = "smallint")
    private int port;
    @MysqlField(field = "day", type = "int")
    private int day;
    @MysqlField(field = "hour", type = "tinyint")
    private int hour;
    @MysqlField(field = "minute", type = "tinyint")
    private int minute;
    @MysqlField(field = "add_time", type = "int")
    private int addTime;
    private String date;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getConnectedClients() {
        return connectedClients;
    }

    public void setConnectedClients(long connectedClients) {
        this.connectedClients = connectedClients;
    }

    public long getBlockedClients() {
        return blockedClients;
    }

    public void setBlockedClients(long blockedClients) {
        this.blockedClients = blockedClients;
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public long getUsedMemoryRss() {
        return usedMemoryRss;
    }

    public void setUsedMemoryRss(long usedMemoryRss) {
        this.usedMemoryRss = usedMemoryRss;
    }

    public long getUsedMemoryPeak() {
        return usedMemoryPeak;
    }

    public void setUsedMemoryPeak(long usedMemoryPeak) {
        this.usedMemoryPeak = usedMemoryPeak;
    }

    public float getMemFragmentationRatio() {
        return memFragmentationRatio;
    }

    public void setMemFragmentationRatio(float memFragmentationRatio) {
        this.memFragmentationRatio = memFragmentationRatio;
    }

    public long getAofEnabled() {
        return aofEnabled;
    }

    public void setAofEnabled(long aofEnabled) {
        this.aofEnabled = aofEnabled;
    }

    public long getTotalConnectionsReceived() {
        return totalConnectionsReceived;
    }

    public void setTotalConnectionsReceived(long totalConnectionsReceived) {
        this.totalConnectionsReceived = totalConnectionsReceived;
    }

    public long getTotalCommandsProcessed() {
        return totalCommandsProcessed;
    }

    public void setTotalCommandsProcessed(long totalCommandsProcessed) {
        this.totalCommandsProcessed = totalCommandsProcessed;
    }

    public long getInstantaneousOpsPerSec() {
        return instantaneousOpsPerSec;
    }

    public void setInstantaneousOpsPerSec(long instantaneousOpsPerSec) {
        this.instantaneousOpsPerSec = instantaneousOpsPerSec;
    }

    public long getTotalNetInputBytes() {
        return totalNetInputBytes;
    }

    public void setTotalNetInputBytes(long totalNetInputBytes) {
        this.totalNetInputBytes = totalNetInputBytes;
    }

    public long getTotalNetOutputBytes() {
        return totalNetOutputBytes;
    }

    public void setTotalNetOutputBytes(long totalNetOutputBytes) {
        this.totalNetOutputBytes = totalNetOutputBytes;
    }

    public float getInstantaneousInputKbps() {
        return instantaneousInputKbps;
    }

    public void setInstantaneousInputKbps(float instantaneousInputKbps) {
        this.instantaneousInputKbps = instantaneousInputKbps;
    }

    public float getInstantaneousOutputKbps() {
        return instantaneousOutputKbps;
    }

    public void setInstantaneousOutputKbps(float instantaneousOutputKbps) {
        this.instantaneousOutputKbps = instantaneousOutputKbps;
    }

    public long getRejectedConnections() {
        return rejectedConnections;
    }

    public void setRejectedConnections(long rejectedConnections) {
        this.rejectedConnections = rejectedConnections;
    }

    public long getSyncFull() {
        return syncFull;
    }

    public void setSyncFull(long syncFull) {
        this.syncFull = syncFull;
    }

    public long getSyncPartialOk() {
        return syncPartialOk;
    }

    public void setSyncPartialOk(long syncPartialOk) {
        this.syncPartialOk = syncPartialOk;
    }

    public long getSyncPartialErr() {
        return syncPartialErr;
    }

    public void setSyncPartialErr(long syncPartialErr) {
        this.syncPartialErr = syncPartialErr;
    }

    public long getExpiredKeys() {
        return expiredKeys;
    }

    public void setExpiredKeys(long expiredKeys) {
        this.expiredKeys = expiredKeys;
    }

    public long getEvictedKeys() {
        return evictedKeys;
    }

    public void setEvictedKeys(long evictedKeys) {
        this.evictedKeys = evictedKeys;
    }

    public long getKeyspaceHits() {
        return keyspaceHits;
    }

    public void setKeyspaceHits(long keyspaceHits) {
        this.keyspaceHits = keyspaceHits;
    }

    public long getKeyspaceMisses() {
        return keyspaceMisses;
    }

    public void setKeyspaceMisses(long keyspaceMisses) {
        this.keyspaceMisses = keyspaceMisses;
    }

    public long getPubsubChannels() {
        return pubsubChannels;
    }

    public void setPubsubChannels(long pubsubChannels) {
        this.pubsubChannels = pubsubChannels;
    }

    public long getPubsubPatterns() {
        return pubsubPatterns;
    }

    public void setPubsubPatterns(long pubsubPatterns) {
        this.pubsubPatterns = pubsubPatterns;
    }

    public long getLatestForkUsec() {
        return latestForkUsec;
    }

    public void setLatestForkUsec(long latestForkUsec) {
        this.latestForkUsec = latestForkUsec;
    }

    public long getMigrateCachedSockets() {
        return migrateCachedSockets;
    }

    public void setMigrateCachedSockets(long migrateCachedSockets) {
        this.migrateCachedSockets = migrateCachedSockets;
    }

    public float getUsedCpuSys() {
        return usedCpuSys;
    }

    public void setUsedCpuSys(float usedCpuSys) {
        this.usedCpuSys = usedCpuSys;
    }

    public float getUsedCpuUser() {
        return usedCpuUser;
    }

    public void setUsedCpuUser(float usedCpuUser) {
        this.usedCpuUser = usedCpuUser;
    }

    public float getUsedCpuSysChildren() {
        return usedCpuSysChildren;
    }

    public void setUsedCpuSysChildren(float usedCpuSysChildren) {
        this.usedCpuSysChildren = usedCpuSysChildren;
    }

    public float getUsedCpuUserChildren() {
        return usedCpuUserChildren;
    }

    public void setUsedCpuUserChildren(float usedCpuUserChildren) {
        this.usedCpuUserChildren = usedCpuUserChildren;
    }

    public long getTotalKeys() {
        return totalKeys;
    }

    public void setTotalKeys(long totalKeys) {
        this.totalKeys = totalKeys;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public long getAvgTtl() {
        return avgTtl;
    }

    public void setAvgTtl(long avgTtl) {
        this.avgTtl = avgTtl;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getAddTime() {
        return addTime;
    }

    public void setAddTime(int addTime) {
        this.addTime = addTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "id=" + id +
                ", connectedClients=" + connectedClients +
                ", blockedClients=" + blockedClients +
                ", usedMemory=" + usedMemory +
                ", usedMemoryRss=" + usedMemoryRss +
                ", usedMemoryPeak=" + usedMemoryPeak +
                ", memFragmentationRatio=" + memFragmentationRatio +
                ", aofEnabled=" + aofEnabled +
                ", totalConnectionsReceived=" + totalConnectionsReceived +
                ", totalCommandsProcessed=" + totalCommandsProcessed +
                ", instantaneousOpsPerSec=" + instantaneousOpsPerSec +
                ", totalNetInputBytes=" + totalNetInputBytes +
                ", totalNetOutputBytes=" + totalNetOutputBytes +
                ", instantaneousInputKbps=" + instantaneousInputKbps +
                ", instantaneousOutputKbps=" + instantaneousOutputKbps +
                ", rejectedConnections=" + rejectedConnections +
                ", syncFull=" + syncFull +
                ", syncPartialOk=" + syncPartialOk +
                ", syncPartialErr=" + syncPartialErr +
                ", expiredKeys=" + expiredKeys +
                ", evictedKeys=" + evictedKeys +
                ", keyspaceHits=" + keyspaceHits +
                ", keyspaceMisses=" + keyspaceMisses +
                ", pubsubChannels=" + pubsubChannels +
                ", pubsubPatterns=" + pubsubPatterns +
                ", latestForkUsec=" + latestForkUsec +
                ", migrateCachedSockets=" + migrateCachedSockets +
                ", usedCpuSys=" + usedCpuSys +
                ", usedCpuUser=" + usedCpuUser +
                ", usedCpuSysChildren=" + usedCpuSysChildren +
                ", usedCpuUserChildren=" + usedCpuUserChildren +
                ", totalKeys=" + totalKeys +
                ", expires=" + expires +
                ", avgTtl=" + avgTtl +
                ", responseTime=" + responseTime +
                ", host='" + host + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                ", addTime=" + addTime +
                '}';
    }
}
