package com.newegg.ec.redis.util;

import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.plugin.install.service.impl.DockerInstallationOperation;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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
        List<Pair<String, String>> configList = new ArrayList<>();
        configList.add(new Pair<>("port", "8000"));
        configList.add(new Pair<>("dir",path));
        configList.add(new Pair<>("bind", "10.16.50.217"));
        RedisConfigUtil.variableAssignment(machine, path, configList, true);
    }

}
