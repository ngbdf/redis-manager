package com.newegg.ec.cache.plugin.basemodel;

import net.sf.json.JSONObject;

/**
 * Created by lzz on 2018/4/20.
 */
public class NodeRequestPram {
    private PluginType pluginType;
    private JSONObject req;

    public PluginType getPluginType() {
        return pluginType;
    }

    public void setPluginType(PluginType pluginType) {
        this.pluginType = pluginType;
    }

    public JSONObject getReq() {
        return req;
    }

    public void setReq(JSONObject req) {
        this.req = req;
    }

    @Override
    public String toString() {
        return "OperatePram{" +
                "pluginType=" + pluginType +
                ", req=" + req +
                '}';
    }
}
