package com.newegg.ec.redis.entity;

/**
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
public class SlowLogParam {

    private String clusterId;

    private String node;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

}