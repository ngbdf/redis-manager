package com.newegg.ec.cache.app.component;

import com.newegg.ec.cache.plugin.INodeOperate;
import com.newegg.ec.cache.plugin.basemodel.PluginType;
import com.newegg.ec.cache.plugin.docker.DockerManager;
import com.newegg.ec.cache.plugin.humpback.HumpbackManager;
import com.newegg.ec.cache.plugin.machine.MachineManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by lzz on 2018/4/20.
 */
@Component
public class NodeManager {
    @Resource
    public MachineManager machineManager;
    @Resource
    public DockerManager dockerManager;
    @Resource
    public HumpbackManager humpbackManager;
    @Value("${cache.plugin}")
    private String pluginList;

    public NodeManager() {

    }

    public String[] pluginList() {
        if (StringUtils.isBlank(pluginList)) {
            return new String[0];
        } else {
            return pluginList.split(",");
        }
    }

    public INodeOperate factoryOperate(PluginType pluginType) {
        INodeOperate nodeOperate = null;
        switch (pluginType) {
            case machine:
                nodeOperate = machineManager;
                break;
            case docker:
                nodeOperate = dockerManager;
                break;
            case humpback:
                nodeOperate = humpbackManager;
                break;
        }
        return nodeOperate;
    }
}
