package com.newegg.ec.redis.plugin.install.entity;

import com.google.common.collect.Multimap;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;

import java.util.List;
import java.util.Map;

/**
 * @author Jay.H.Zou
 * @date 2019/7/26
 */
public class InstallationParam {

    private Cluster cluster;

    private boolean create;

    private List<Integer> machineIdList;

    private List<Machine> machineList;

    private boolean autoBuild;

    private boolean autoInit;

    private boolean sudo;

    private Integer startPort;

    /**
     * all nodes
     */
    List<RedisNode> redisNodeList;

    Multimap<Machine, RedisNode> machineAndRedisNode;

    private String topologyList;

    Multimap<RedisNode, RedisNode> topology;
    /**
     * master 个数
     */
    private Integer masterNumber;

    /**
     * 每个 master 下的副本个数
     */
    private Integer replicaNumber;

    Map<String, String> configMap;

    private String policy;

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    public List<Integer> getMachineIdList() {
        return machineIdList;
    }

    public void setMachineIdList(List<Integer> machineIdList) {
        this.machineIdList = machineIdList;
    }

    public List<Machine> getMachineList() {
        return machineList;
    }

    public void setMachineList(List<Machine> machineList) {
        this.machineList = machineList;
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

    public boolean isSudo() {
        return sudo;
    }

    public void setSudo(boolean sudo) {
        this.sudo = sudo;
    }

    public Integer getStartPort() {
        return startPort;
    }

    public void setStartPort(Integer startPort) {
        this.startPort = startPort;
    }

    public List<RedisNode> getRedisNodeList() {
        return redisNodeList;
    }

    public void setRedisNodeList(List<RedisNode> redisNodeList) {
        this.redisNodeList = redisNodeList;
    }

    public Multimap<Machine, RedisNode> getMachineAndRedisNode() {
        return machineAndRedisNode;
    }

    public void setMachineAndRedisNode(Multimap<Machine, RedisNode> machineAndRedisNode) {
        this.machineAndRedisNode = machineAndRedisNode;
    }

    public String getTopologyList() {
        return topologyList;
    }

    public void setTopologyList(String topologyList) {
        this.topologyList = topologyList;
    }

    public Multimap<RedisNode, RedisNode> getTopology() {
        return topology;
    }

    public void setTopology(Multimap<RedisNode, RedisNode> topology) {
        this.topology = topology;
    }

    public Integer getMasterNumber() {
        return masterNumber;
    }

    public void setMasterNumber(Integer masterNumber) {
        this.masterNumber = masterNumber;
    }

    public Integer getReplicaNumber() {
        return replicaNumber;
    }

    public void setReplicaNumber(Integer replicaNumber) {
        this.replicaNumber = replicaNumber;
    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, String> configMap) {
        this.configMap = configMap;
    }

}
