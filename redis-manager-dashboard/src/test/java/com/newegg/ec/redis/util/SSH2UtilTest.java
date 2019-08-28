package com.newegg.ec.redis.util;

import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.plugin.install.service.impl.DockerNodeOperation;
import org.junit.Before;
import org.junit.Test;

public class SSH2UtilTest {

    Machine machine = new Machine();

    int port = 8000;

    @Before
    public void buildMachine() {
        machine.setHost("");
        machine.setUserName("");
        machine.setPassword("1234");
    }

    @Test
    public void cp() throws Exception {
        SSH2Util.copy(machine, "/home/hadoop/jay/redis.conf", DockerNodeOperation.DOCKER_INSTALL_BASE_PATH + port, true);
    }

    @Test
    public void remoteCopy() throws Exception {
        SSH2Util.wget(machine, "/data/test/", "redis3.0.6.tar", "http://121.1.1.1:8182/package/redis3.0.6.tar", true);
    }

    @Test
    public void mkdir() throws Exception {
        SSH2Util.mkdir(machine, DockerNodeOperation.DOCKER_INSTALL_BASE_PATH + port, false);
    }

    @Test
    public void rm() throws Exception {
        SSH2Util.rm(machine, "/data/test/redis3.0.6.tar.1", true);
    }

    @Test
    public void copyRedisConfigToRemote() throws Exception {
        SSH2Util.copyFileToRemote(machine, "/data/redis/docker/9000/", "127.0.0.1:8000/site/API-Gateway.png", true);
    }
}