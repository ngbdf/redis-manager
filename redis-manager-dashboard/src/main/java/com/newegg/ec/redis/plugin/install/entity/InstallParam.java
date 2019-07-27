package com.newegg.ec.redis.plugin.install.entity;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.RedisNode;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/26
 */
public class InstallParam {

    private Cluster cluster;

    private String machineGroup;

    private boolean autoBuild;

    private boolean autoInit;

    private int startPort;

    private int endPort;

    List<RedisNode> redisNodeList;

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public String getMachineGroup() {
        return machineGroup;
    }

    public void setMachineGroup(String machineGroup) {
        this.machineGroup = machineGroup;
    }

    public boolean isAutoBuild() {
        return autoBuild;
    }

    public void setAutoBuild(boolean autoBuild) {
        this.autoBuild = autoBuild;
    }

    public boolean isAutoInit() {
        return autoInit;
    }

    public void setAutoInit(boolean autoInit) {
        this.autoInit = autoInit;
    }

    public int getStartPort() {
        return startPort;
    }

    public void setStartPort(int startPort) {
        this.startPort = startPort;
    }

    public int getEndPort() {
        return endPort;
    }

    public void setEndPort(int endPort) {
        this.endPort = endPort;
    }

    public List<RedisNode> getRedisNodeList() {
        return redisNodeList;
    }

    public void setRedisNodeList(List<RedisNode> redisNodeList) {
        this.redisNodeList = redisNodeList;
    }
}
