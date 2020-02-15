package com.newegg.ec.redis.entity;

import java.sql.Timestamp;

/**
 * 1)  1) "name"
 * 2) "master"
 * 3) "ip"
 * 4) "xxxxxxxx"
 * 5) "port"
 * 6) "16379"
 * 7) "runid"
 * 8) "3e3095a707d111e7467d8830e52e04d04ad53f92"
 * 9) "flags"
 * 10) "s_down,o_down,master"
 * 11) "link-pending-commands"
 * 12) "0"
 * 13) "link-refcount"
 * 14) "1"
 * 15) "last-ping-sent"
 * 16) "3706617"
 * 17) "last-ok-ping-reply"
 * 18) "3706617"
 * 19) "last-ping-reply"
 * 20) "631"
 * 21) "s-down-time"
 * 22) "3701566"
 * 23) "o-down-time"
 * 24) "3701476"
 * 25) "down-after-milliseconds"
 * 26) "5000"
 * 27) "info-refresh"
 * 28) "2238"
 * 29) "role-reported"
 * 30) "master"
 * 31) "role-reported-time"
 * 32) "3706617"
 * 33) "config-epoch"
 * 34) "22"
 * 35) "num-slaves"
 * 36) "3"
 * 37) "num-other-sentinels"
 * 38) "5"
 * 39) "quorum"
 * 40) "4"
 * 41) "failover-timeout"
 * 42) "180000"
 * 43) "parallel-syncs"
 * 44) "1"
 *
 * @author Everly.J.Ju
 * @date 2020/01/22
 */
public class SentinelMaster {

    private Integer sentinelMasterId;

    private Integer clusterId;

    private Integer groupId;

    private String name;

    private String host;

    private Integer port;

    private String flags;

    private String lastMasterNode;

    private String status;

    private Long linkPendingCommands;

    private Long linkRefcount;

    private Long lastPingSent;

    private Long lastOkPingReply;

    private Long lastPingReply;

    private Long sDownTime;

    private Long oDownTime;

    private Long downAfterMilliseconds;

    private Long infoRefresh;

    private String roleReported;

    private Long roleReportedTime;

    private Long configEpoch;

    private Integer numSlaves;

    private Integer sentinels;

    private Integer quorum;

    private Long failoverTimeout;

    private Integer parallelSyncs;

    private String authPass;

    private boolean monitor;

    private boolean masterChanged;

    private Timestamp updateTime;

    public Integer getSentinelMasterId() {
        return sentinelMasterId;
    }

    public void setSentinelMasterId(Integer sentinelMasterId) {
        this.sentinelMasterId = sentinelMasterId;
    }

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public String getLastMasterNode() {
        return lastMasterNode;
    }

    public void setLastMasterNode(String lastMasterNode) {
        this.lastMasterNode = lastMasterNode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getLinkPendingCommands() {
        return linkPendingCommands;
    }

    public void setLinkPendingCommands(Long linkPendingCommands) {
        this.linkPendingCommands = linkPendingCommands;
    }

    public Long getLinkRefcount() {
        return linkRefcount;
    }

    public void setLinkRefcount(Long linkRefcount) {
        this.linkRefcount = linkRefcount;
    }

    public Long getLastPingSent() {
        return lastPingSent;
    }

    public void setLastPingSent(Long lastPingSent) {
        this.lastPingSent = lastPingSent;
    }

    public Long getLastOkPingReply() {
        return lastOkPingReply;
    }

    public void setLastOkPingReply(Long lastOkPingReply) {
        this.lastOkPingReply = lastOkPingReply;
    }

    public Long getLastPingReply() {
        return lastPingReply;
    }

    public void setLastPingReply(Long lastPingReply) {
        this.lastPingReply = lastPingReply;
    }

    public Long getsDownTime() {
        return sDownTime;
    }

    public void setsDownTime(Long sDownTime) {
        this.sDownTime = sDownTime;
    }

    public Long getoDownTime() {
        return oDownTime;
    }

    public void setoDownTime(Long oDownTime) {
        this.oDownTime = oDownTime;
    }

    public Long getDownAfterMilliseconds() {
        return downAfterMilliseconds;
    }

    public void setDownAfterMilliseconds(Long downAfterMilliseconds) {
        this.downAfterMilliseconds = downAfterMilliseconds;
    }

    public Long getInfoRefresh() {
        return infoRefresh;
    }

    public void setInfoRefresh(Long infoRefresh) {
        this.infoRefresh = infoRefresh;
    }

    public String getRoleReported() {
        return roleReported;
    }

    public void setRoleReported(String roleReported) {
        this.roleReported = roleReported;
    }

    public Long getRoleReportedTime() {
        return roleReportedTime;
    }

    public void setRoleReportedTime(Long roleReportedTime) {
        this.roleReportedTime = roleReportedTime;
    }

    public Long getConfigEpoch() {
        return configEpoch;
    }

    public void setConfigEpoch(Long configEpoch) {
        this.configEpoch = configEpoch;
    }

    public Integer getNumSlaves() {
        return numSlaves;
    }

    public void setNumSlaves(Integer numSlaves) {
        this.numSlaves = numSlaves;
    }

    public Integer getSentinels() {
        return sentinels;
    }

    public void setSentinels(Integer sentinels) {
        this.sentinels = sentinels;
    }

    public Integer getQuorum() {
        return quorum;
    }

    public void setQuorum(Integer quorum) {
        this.quorum = quorum;
    }

    public Long getFailoverTimeout() {
        return failoverTimeout;
    }

    public void setFailoverTimeout(Long failoverTimeout) {
        this.failoverTimeout = failoverTimeout;
    }

    public Integer getParallelSyncs() {
        return parallelSyncs;
    }

    public void setParallelSyncs(Integer parallelSyncs) {
        this.parallelSyncs = parallelSyncs;
    }

    public String getAuthPass() {
        return authPass;
    }

    public void setAuthPass(String authPass) {
        this.authPass = authPass;
    }

    public boolean getMonitor() {
        return monitor;
    }

    public void setMonitor(boolean monitor) {
        this.monitor = monitor;
    }

    public boolean getMasterChanged() {
        return masterChanged;
    }

    public void setMasterChanged(boolean masterChanged) {
        this.masterChanged = masterChanged;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
