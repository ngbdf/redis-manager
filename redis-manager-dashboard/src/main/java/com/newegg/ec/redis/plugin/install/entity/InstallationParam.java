package com.newegg.ec.redis.plugin.install.entity;

import com.google.common.collect.Multimap;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import org.jvnet.hk2.component.MultiMap;

import java.util.List;
import java.util.Map;

/**
 * @author Jay.H.Zou
 * @date 2019/7/26
 */
public class InstallationParam {

    private Cluster cluster;

    private boolean create;

    private List<String> machineIdList;

    private List<Machine> machineList;

    private boolean autoBuild;

    private boolean autoInit;

    private boolean sudo;

    private int startPort;

    private int endPort;

    private String image;

    List<RedisNode> redisNodeList;

    Multimap<Machine, RedisNode> machineAndRedisNode;

    Map<String, String> configMap;

    private String redisMode;

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

    public List<String> getMachineIdList() {
        return machineIdList;
    }

    public void setMachineIdList(List<String> machineIdList) {
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

    public int getEndPort() {
        return endPort;
    }

    public void setEndPort(int endPort) {
        this.endPort = endPort;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public InstallationEnvironment getInstallationEnvironment() {
        return installationEnvironment;
    }

    public void setInstallationEnvironment(InstallationEnvironment installationEnvironment) {
        this.installationEnvironment = installationEnvironment;
    }
}
