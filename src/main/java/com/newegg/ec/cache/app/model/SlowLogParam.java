package com.newegg.ec.cache.app.model;

import java.util.List;

/**
 * Created by gl49 on 2018/5/2.
 */
public class SlowLogParam {
    private List<Host> hostList;
    private int logLimit;

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
                "hostList=" + hostList +
                ", logLimit=" + logLimit +
                '}';
    }
}
