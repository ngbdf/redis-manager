package com.newegg.ec.redis.plugin.install.service.impl;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.plugin.install.service.AbstractNodeOperation;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.List;
import java.util.Map;

/**
 * @author Jay.H.Zou
 * @date 10/29/2019
 */
public class KubernetesOperation extends AbstractNodeOperation {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

    }

    @Override
    public List<String> getImageList() {
        return null;
    }

    @Override
    public boolean checkEnvironment(InstallationParam installationParam) {
        return false;
    }

    @Override
    public boolean pullImage(InstallationParam installationParam) {
        return false;
    }

    @Override
    public boolean install(InstallationParam installationParam) {
        return false;
    }

    @Override
    protected Map<String, String> getBaseConfigs(String bind, int port, String dir) {
        return null;
    }

    @Override
    public boolean start(Cluster cluster, RedisNode redisNode) {
        return false;
    }

    @Override
    public boolean stop(Cluster cluster, RedisNode redisNode) {
        return false;
    }

    @Override
    public boolean restart(Cluster cluster, RedisNode redisNode) {
        return false;
    }

    @Override
    public boolean remove(Cluster cluster, RedisNode redisNode) {
        return false;
    }

}
