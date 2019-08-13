package com.newegg.ec.redis.plugin.install.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.plugin.install.service.InstallationOperation;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Jay.H.Zou
 * @date 2019/8/13
 */
public class MachineOperation implements InstallationOperation {

    @Value("${redis-manager.install.machine.package-path: /redis/machine/}")
    private String packagePath;

    @Override
    public List<String> getPackageList() {
        if (Strings.isNullOrEmpty(packagePath)) {
            throw new RuntimeException("Machine package config is empty!");
        }
        Path path = Paths.get(packagePath);
        if (!Files.exists(path)) {
            throw new RuntimeException(packagePath + " not exist!");
        }
        if (!Files.isDirectory(path)) {
            throw new RuntimeException(packagePath + " is not directory!");
        }
        List<String> packages = new ArrayList<>();
        try {
            Stream<Path> list = Files.list(path);
            list.forEach(subPath -> {
                if (!Files.isDirectory(subPath)) {
                    packages.add(subPath.getFileName().toString());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean checkEnvironment(InstallationParam installationParam, List<Machine> machineList) {
        return false;
    }

    @Override
    public boolean pullImage() {
        return false;
    }

    @Override
    public boolean buildConfig() {
        return false;
    }

    @Override
    public boolean install(List<RedisNode> redisNodeList) {
        return false;
    }
}
