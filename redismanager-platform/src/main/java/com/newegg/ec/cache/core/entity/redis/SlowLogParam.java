package com.newegg.ec.cache.core.entity.redis;

import com.newegg.ec.cache.core.entity.model.Host;

import java.util.List;

/**
 * Created by gl49 on 2018/5/2.
 */
public class SlowLogParam {

    private int clusterId;

    private List<Host> hostList;

    private int logLimit;

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public List<Host> getHostList() {
        return hostList;
    }

    public void setHostList(List<Host> hostList) {
        this.hostList = hostList;
    }

    public int getLogLimit() {
        return logLimit;
    }

    public void setLogLimit(int logLimit) {
        this.logLimit = logLimit;
    }

    @Override
    public String toString() {
        return "SlowLogParam{" +
                "clusterId=" + clusterId +
                "hostList=" + hostList +
                ", logLimit=" + logLimit +
                '}';
    }
}
