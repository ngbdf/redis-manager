package com.newegg.ec.redis.util;

import com.newegg.ec.redis.entity.Machine;
import org.junit.Test;

import java.util.Map;

public class LinuxInfoUtilTest {

    @Test
    public void login() {
    }

    @Test
    public void execute() {
    }

    @Test
    public void getMachineResourceInfo() throws Exception {
        Machine machine = new Machine();
        machine.setHost("127.0.0.1");
        machine.setUserName("xxx");
        machine.setPassword("xxx");
        Map<String, String> machineResourceInfo = LinuxInfoUtil.getLinuxInfo(machine);
        System.err.println(machineResourceInfo);
    }
}