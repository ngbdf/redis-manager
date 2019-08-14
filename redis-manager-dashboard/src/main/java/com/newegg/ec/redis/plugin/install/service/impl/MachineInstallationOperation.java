package com.newegg.ec.redis.plugin.install.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.plugin.install.service.AbstractInstallationOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/8/13
 */
@Component
public class MachineInstallationOperation extends AbstractInstallationOperation {

    @Value("${redis-manager.install.machine.package-path: /redis/machine/}")
    private String packagePath;

    @Override
    public List<String> getPackageList() {
        if (Strings.isNullOrEmpty(packagePath)) {
            throw new RuntimeException("Machine package config is empty!");
        }
        File file = new File(packagePath);
        if (!file.exists()) {
            throw new RuntimeException(packagePath + " not exist!");
        }
        if (!file.isDirectory()) {
            throw new RuntimeException(packagePath + " is not directory!");
        }
        List<String> packageNameList = new ArrayList<>();
        File[] packageFiles = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File item) {
                String name = item.getName();
                return item.isFile() && (name.endsWith("tar") || name.endsWith("tar.gz"));
            }
        });
        for (File packageFile : packageFiles) {
            packageNameList.add(packageFile.getName());
        }
        return packageNameList;
    }

    @Override
    public boolean checkEnvironment(InstallationParam installationParam, List<Machine> machineList) {
        return true;
    }

    /**
     * 从本机copy到目标机器上
     * @return
     */
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
