package com.newegg.ec.redis.util;

import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.plugin.install.service.impl.DockerInstallationOperation;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Jay.H.Zou
 * @date 2019/8/12
 */
public class RedisConfigUtilTest {

    Machine machine = new Machine();

    int port = 8000;

    @Before
    public void buildMachine() {
    }

    @Test
    public void createConf() throws IOException {
        RedisConfigUtil.generateRedisConfig("E:/", RedisConfigUtil.CLUSTER_TYPE);
    }

    @Test
    public void variableAssignment() throws Exception {
        String path = DockerInstallationOperation.DOCKER_INSTALL_BASE_PATH + port;
        Map<String, String> configs = new HashMap<>();
        configs.put("port", "8000");
        configs.put("dir", path);
        configs.put("bind", "10.16.50.217");
        RedisConfigUtil.variableAssignment(machine, path, configs, true);
    }

    @Test
    public void copyRedisConfigToRemote() throws Exception {
        RedisConfigUtil.copyRedisConfigToRemote(machine, "/data/redis/docker/9000/", "172.16.35.219:8000/site/API-Gateway.png", true);
    }
}
