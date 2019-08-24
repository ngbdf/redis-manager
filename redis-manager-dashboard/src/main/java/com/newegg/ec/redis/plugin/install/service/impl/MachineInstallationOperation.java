package com.newegg.ec.redis.plugin.install.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.plugin.install.service.AbstractInstallationOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.newegg.ec.redis.util.SignUtil.SLASH;

/**
 * @author Jay.H.Zou
 * @date 2019/8/13
 */
@Component
public class MachineInstallationOperation extends AbstractInstallationOperation {

    @Value("${redis-manager.install.machine.package-path: /redis/machine/}")
    private String packagePath;

    public static final String MACHINE_INSTALL_BASE_PATH = "/data/redis/machine/";

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        INSTALL_BASE_PATH = MACHINE_INSTALL_BASE_PATH;
    }

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
    public boolean checkEnvironment(InstallationParam installationParam) {
        return true;
    }

    /**
     * 从本机copy到目标机器上
     *
     * @return
     */
    @Override
    public boolean pullImage(InstallationParam installationParam) {
        String image = installationParam.getImage();
        if (!packagePath.endsWith(SLASH)) {
            packagePath += SLASH;
        }
        List<Machine> machineList = installationParam.getMachineList();
        String localImagePath = packagePath + image;
        List<Future<Boolean>> resultFutureList = new ArrayList<>();
        for (Machine machine : machineList) {
            resultFutureList.add(threadPool.submit(() -> {
                /*
                 * 本机执行：安装包、redis.conf 拷贝到远程机器临时目录
                 * */
                //SSH2Util.scp(machine, localImagePath, MACHINE_INSTALL_BASE_PATH);
                return true;
                // TODO: websocket
                //return false;
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
    public boolean install(InstallationParam installationParam) {
        /*
         * 远程机器执行：创建相应的端口目录，将安装包；redis.conf拷贝至端口目录；解压；删除安装包
         * 远程机器执行：修改配置文件
         * 远程机器执行：启动
         */
        return false;
    }

}
