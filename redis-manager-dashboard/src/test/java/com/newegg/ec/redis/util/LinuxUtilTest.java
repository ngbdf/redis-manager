package com.newegg.ec.redis.util;

import com.newegg.ec.redis.entity.Machine;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class LinuxUtilTest {

    @Test
    public void login() {
    }

    @Test
    public void execute() {
    }

    @Test
    public void getMachineResourceInfo() {
        Machine machine = new Machine();
        machine.setHost("10.16.20.164");
        machine.setUserName("jz3y");
        machine.setPassword("Coastline@999");
        Map<String, String> machineResourceInfo = LinuxUtil.getLinuxInfo(machine);
        System.err.println(machineResourceInfo);
    }
}