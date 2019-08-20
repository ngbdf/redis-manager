package com.newegg.ec.redis.plugin.install.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.plugin.install.service.AbstractInstallationOperation;
import com.newegg.ec.redis.util.CommonUtil;
import com.newegg.ec.redis.util.RedisConfigUtil;
import com.newegg.ec.redis.util.RemoteFileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.newegg.ec.redis.util.RedisConfigUtil.CLUSTER_TYPE;
import static com.newegg.ec.redis.util.RedisConfigUtil.STANDALONE_TYPE;
import static com.newegg.ec.redis.util.RedisUtil.STANDALONE;

/**
 * @author Jay.H.Zou
 * @date 2019/8/13
 */
@Component
public class MachineInstallationOperation extends AbstractInstallationOperation {

    @Value("${redis-manager.install.machine.package-path: /redis/machine/}")
    private String packagePath;

    public static final String MACHINE_INSTALL_BASE_PATH = "/data/redis/machine/";

    /**
     * 存放redis.conf的临时目录
     */
    public static final String MACHINE_TEMP_CONFIG_PATH = "/data/redis/machine/temp/";

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
        // redis 集群模式
        String redisMode = installationParam.getRedisMode();
        int mode;
        if (Objects.equals(redisMode, STANDALONE)) {
            mode = STANDALONE_TYPE;
        } else {
            // default: cluster
            mode = CLUSTER_TYPE;
        }
        // 判断redis version
        Cluster cluster = installationParam.getCluster();
        String redisPassword = cluster.getRedisPassword();
        try {
            // 配置文件写入本地机器
            RedisConfigUtil.generateRedisConfig(MACHINE_TEMP_CONFIG_PATH + CommonUtil.replaceSpace(cluster.getClusterName()), mode, redisPassword);
        } catch (Exception e) {
            // TODO: websocket
            return false;
        }
        return true;
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
            resultFutureList.add(threadPool.submit(() -> {
                try {
                    /*
                     * 本机执行：安装包、redis.conf 拷贝到远程机器临时目录
                     * */
                    RemoteFileUtil.scp(machine, localImagePath, MACHINE_INSTALL_BASE_PATH);
                    return true;
                } catch (IOException e) {
                    // TODO: websocket
                    return false;
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
        /*
         * 远程机器执行：创建相应的端口目录，将安装包；redis.conf拷贝至端口目录；解压；删除安装包
         * 远程机器执行：修改配置文件
         * 远程机器执行：启动
         */
        return false;
    }
}
