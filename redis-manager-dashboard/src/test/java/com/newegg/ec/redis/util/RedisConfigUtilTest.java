package com.newegg.ec.redis.util;

import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.plugin.install.service.impl.DockerNodeOperation;
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
        RedisConfigUtil.generateRedisConfig("E:/redis-manager/conf/standalone/", RedisConfigUtil.STANDALONE_TYPE);
    }

    @Test
    public void variableAssignment() throws Exception {
        String path = DockerNodeOperation.DOCKER_INSTALL_BASE_PATH + port;
        Map<String, String> configs = new HashMap<>();
        configs.put("port", "8000");
        configs.put("dir", path);
        configs.put("bind", "10.1.5.6");
        RedisConfigUtil.variableAssignment(machine, path, configs, true);
    }

}
