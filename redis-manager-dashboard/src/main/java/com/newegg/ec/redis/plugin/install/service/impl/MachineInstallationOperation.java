package com.newegg.ec.redis.plugin.install.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.plugin.install.service.AbstractInstallationOperation;
import com.newegg.ec.redis.util.RemoteFileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Jay.H.Zou
 * @date 2019/8/13
 */
@Component
public class MachineInstallationOperation extends AbstractInstallationOperation {

    @Value("${redis-manager.install.machine.package-path: /redis/machine/}")
    private String packagePath;

    @Override
    public List<String> getImageList() {
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
        List<String> imageList = new ArrayList<>();
        File[] packageFiles = file.listFiles(item -> {
            String name = item.getName();
            return item.isFile() && (name.endsWith("tar") || name.endsWith("tar.gz"));
        });
        for (File packageFile : packageFiles) {
            imageList.add(packageFile.getName());
        }
        return imageList;
    }

    @Override
    public boolean checkEnvironment(InstallationParam installationParam, List<Machine> machineList) {

        return true;
    }

    @Override
    public boolean buildConfig(InstallationParam installationParam) {
        return false;
    }

    /**
     * 从本机copy到目标机器上
     *
     * @return
     */
    @Override
    public boolean pullImage(InstallationParam installationParam, List<Machine> machineList) {
        String image = installationParam.getImage();
        if (!packagePath.endsWith("/")) {
            packagePath += "/";
        }
        String localImagePath = packagePath + image;
        List<Future<Boolean>> resultFutureList = new ArrayList<>();
        for (Machine machine : machineList) {
            resultFutureList.add(threadPool.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        RemoteFileUtil.scp(machine, localImagePath, "");
                        return true;
                    } catch (IOException e) {
                        // TODO: websocket
                        return false;
                    }
                }
            }));
        }
        for (Future<Boolean> resultFuture : resultFutureList) {
            try {
                if (!resultFuture.get(300, TimeUnit.SECONDS)) {
                    return false;
                }
            } catch (Exception e) {
                // TODO: websocket
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean install(List<RedisNode> redisNodeList) {
        return false;
    }
}
