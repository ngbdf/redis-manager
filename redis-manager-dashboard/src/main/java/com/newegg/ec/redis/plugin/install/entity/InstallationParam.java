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

    private int startPort;

    List<RedisNode> allRedisNodes;

    Multimap<Machine, RedisNode> machineAndRedisNode;

    Multimap<RedisNode, RedisNode> topology;
    /**
     * master 个数
     */
    private int masterNumber;

    /**
     * 每个 master 下的副本个数
     */
    private int replicaNumber;

    Map<String, String> configMap;

    private String redisMode;

    private String policy;

    private InstallationEnvironment installationEnvironment;

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

    public int getStartPort() {
        return startPort;
    }

    public void setStartPort(int startPort) {
        this.startPort = startPort;
    }

    public List<RedisNode> getAllRedisNodes() {
        return allRedisNodes;
    }

    public void setAllRedisNodes(List<RedisNode> allRedisNodes) {
        this.allRedisNodes = allRedisNodes;
    }

    public Multimap<Machine, RedisNode> getMachineAndRedisNode() {
        return machineAndRedisNode;
    }

    public void setMachineAndRedisNode(Multimap<Machine, RedisNode> machineAndRedisNode) {
        this.machineAndRedisNode = machineAndRedisNode;
    }

    public Multimap<RedisNode, RedisNode> getTopology() {
        return topology;
    }

    public void setTopology(Multimap<RedisNode, RedisNode> topology) {
        this.topology = topology;
    }

    public int getMasterNumber() {
        return masterNumber;
    }

    public void setMasterNumber(int masterNumber) {
        this.masterNumber = masterNumber;
    }

    public int getReplicaNumber() {
        return replicaNumber;
    }

    public void setReplicaNumber(int replicaNumber) {
        this.replicaNumber = replicaNumber;
    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, String> configMap) {
        this.configMap = configMap;
    }

    public String getRedisMode() {
        return redisMode;
    }

    public void setRedisMode(String redisMode) {
        this.redisMode = redisMode;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public InstallationEnvironment getInstallationEnvironment() {
        return installationEnvironment;
    }

    public void setInstallationEnvironment(InstallationEnvironment installationEnvironment) {
        this.installationEnvironment = installationEnvironment;
    }
}
