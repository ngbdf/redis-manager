package com.newegg.ec.redis.plugin.install.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.newegg.ec.redis.client.RedisClient;
import com.newegg.ec.redis.client.RedisClientFactory;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.plugin.install.service.AbstractOperationManage;
import com.newegg.ec.redis.plugin.install.service.INodeOperation;
import com.newegg.ec.redis.util.LinuxInfoUtil;
import com.newegg.ec.redis.util.SSH2Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.newegg.ec.redis.config.SystemConfig.MACHINE_PACKAGE_ORIGINAL_PATH;
import static com.newegg.ec.redis.util.SignUtil.COLON;
import static com.newegg.ec.redis.util.SignUtil.SLASH;

/**
 * @author Jay.H.Zou
 * @date 2019/8/13
 */
@Component
public class MachineOperationManage extends AbstractOperationManage implements INodeOperation {

    @Value("${redis-manager.install.machine.package-path: /redis/machine/}")
    private String packagePath;

    public static final String MACHINE_INSTALL_BASE_PATH = "/data/redis/machine/";

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        INSTALL_BASE_PATH = MACHINE_INSTALL_BASE_PATH;
        if (!packagePath.endsWith(SLASH)) {
            packagePath += SLASH;
        }
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
    }

    @Override
    public List<String> getImageList() {
        File file = new File(packagePath);
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
        boolean sudo = installationParam.isSudo();
        Cluster cluster = installationParam.getCluster();
        String image = cluster.getImage();
        String url;
        try {
            // eg: ip:port/redis/machine/xxx.tar.gz
            url = LinuxInfoUtil.getIpAddress() + COLON + systemConfig.getServerPort() + MACHINE_PACKAGE_ORIGINAL_PATH + image;
        } catch (SocketException e) {
            return false;
        }
        List<Machine> machineList = installationParam.getMachineList();
        String tempPath = INSTALL_BASE_PATH + "package/";
        List<Future<Boolean>> resultFutureList = new ArrayList<>(machineList.size());
        for (Machine machine : machineList) {
            resultFutureList.add(threadPool.submit(() -> {
                try {
                    // 将 redis.conf 分发到目标机器的临时目录
                    SSH2Util.copyFileToRemote(machine, tempPath, url, sudo);
                    return true;
                } catch (Exception e) {
                    // TODO: websocket
                    return false;
                }
            }));
        }
        for (Future<Boolean> resultFuture : resultFutureList) {
            try {
                if (!resultFuture.get(TIMEOUT, TimeUnit.SECONDS)) {
                    return false;
                }
            } catch (Exception e) {
                // TODO: websocket
                return false;
            }
        }
        String tempPackagePath = tempPath + SLASH + image;
        Multimap<Machine, RedisNode> machineAndRedisNode = installationParam.getMachineAndRedisNode();
        for (Map.Entry<Machine, RedisNode> entry : machineAndRedisNode.entries()) {
            Machine machine = entry.getKey();
            RedisNode redisNode = entry.getValue();
            String targetPath = INSTALL_BASE_PATH + redisNode.getPort();
            try {
                // 解压到目标目录
                SSH2Util.unzipToTargetPath(machine, tempPackagePath, targetPath, sudo);
            } catch (Exception e) {
                // TODO: websocket
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean install(InstallationParam installationParam) {
        Multimap<Machine, RedisNode> machineAndRedisNode = installationParam.getMachineAndRedisNode();
        for (Map.Entry<Machine, RedisNode> entry : machineAndRedisNode.entries()) {
            Machine machine = entry.getKey();
            RedisNode redisNode = entry.getValue();
            boolean start = start(null, machine, redisNode);
            if (!start) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean start(Cluster cluster, Machine machine, RedisNode redisNode) {
        String template = "cd %s; ./redis-server redis.conf";
        String targetPath = INSTALL_BASE_PATH + redisNode.getPort();
        try {
            SSH2Util.execute(machine, String.format(template, targetPath));
            return true;
        } catch (Exception e) {
            // TODO: websocket
            return false;
        }
    }

}
