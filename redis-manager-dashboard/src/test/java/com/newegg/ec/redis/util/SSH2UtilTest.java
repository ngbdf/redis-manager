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
        SSH2Util.mkdir(machine, DockerNodeOperation.DOCKER_INSTALL_BASE_PATH + "cluster", true);
    }

    @Test
    public void rm() throws Exception {
        String rm = SSH2Util.rm(machine, "/data/redis/docker/cluster", true);
        System.err.println(rm);
    }

    @Test
    public void copyRedisConfigToRemote() throws Exception {
        String s = SSH2Util.copyFileToRemote(machine, "/data/redis/docker/cluster", "10.16.164.20:8182/redis-manager/config/cluster/redis.conf", true);
        System.err.println(s);
    }
}