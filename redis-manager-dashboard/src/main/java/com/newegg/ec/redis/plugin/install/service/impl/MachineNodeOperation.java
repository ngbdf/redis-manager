package com.newegg.ec.redis.plugin.install.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.newegg.ec.redis.client.RedisClient;
import com.newegg.ec.redis.client.RedisClientFactory;
import com.newegg.ec.redis.controller.websocket.InstallationWebSocketHandler;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.plugin.install.service.AbstractNodeOperation;
import com.newegg.ec.redis.plugin.install.service.INodeOperation;
import com.newegg.ec.redis.service.IMachineService;
import com.newegg.ec.redis.util.LinuxInfoUtil;
import com.newegg.ec.redis.util.SSH2Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.newegg.ec.redis.config.SystemConfig.MACHINE_PACKAGE_ORIGINAL_PATH;
import static com.newegg.ec.redis.util.RedisConfigUtil.*;
import static com.newegg.ec.redis.util.SignUtil.COLON;
import static com.newegg.ec.redis.util.SignUtil.SLASH;

/**
 * @author Jay.H.Zou
 * @date 2019/8/13
 */
@Component
public class MachineNodeOperation extends AbstractNodeOperation implements INodeOperation {

    private static final Logger logger = LoggerFactory.getLogger(MachineNodeOperation.class);

    public static final String MACHINE_INSTALL_BASE_PATH = "/data/redis/machine/";

    @Value("${redis-manager.install.machine.package-path}")
    private String packagePath;

    @Autowired
    private IMachineService machineService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        INSTALL_BASE_PATH = MACHINE_INSTALL_BASE_PATH;
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
        String clusterName = cluster.getClusterName();
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
                    String message = "Pull machine image failed, host: " + machine.getHost();
                    InstallationWebSocketHandler.appendLog(clusterName, message);
                    InstallationWebSocketHandler.appendLog(clusterName, e.getMessage());
                    logger.error(message, e);
                    return false;
                }
            }));
        }
        for (Future<Boolean> resultFuture : resultFutureList) {
            try {
                if (!resultFuture.get(TIMEOUT, TimeUnit.MINUTES)) {
                    return false;
                }
            } catch (Exception e) {
                InstallationWebSocketHandler.appendLog(clusterName, e.getMessage());
                logger.error("", e);
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
                String result = SSH2Util.unzipToTargetPath(machine, tempPackagePath, targetPath, sudo);
                if (!Strings.isNullOrEmpty(result)) {
                    InstallationWebSocketHandler.appendLog(clusterName, "Unzip failed, host: " + machine.getHost());
                    InstallationWebSocketHandler.appendLog(clusterName, result);
                    return false;
                }
            } catch (Exception e) {
                InstallationWebSocketHandler.appendLog(clusterName, "Unzip failed, host: " + machine.getHost());
                InstallationWebSocketHandler.appendLog(clusterName, "Unzip failed, host: " + machine.getHost());
                logger.error("", e);
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean install(InstallationParam installationParam) {
        Multimap<Machine, RedisNode> machineAndRedisNode = installationParam.getMachineAndRedisNode();
        Cluster cluster = installationParam.getCluster();
        for (Map.Entry<Machine, RedisNode> entry : machineAndRedisNode.entries()) {
            RedisNode redisNode = entry.getValue();
            boolean start = start(cluster, redisNode);
            if (!start) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean start(Cluster cluster, RedisNode redisNode) {
        String clusterName = cluster.getClusterName();
        String template = "cd %s; sudo ./redis-server redis.conf";
        int port = redisNode.getPort();
        String targetPath = INSTALL_BASE_PATH + port;
        String host = redisNode.getHost();
        try {
            Machine machine = machineService.getMachineByHost(cluster.getGroupId(), host);
            if (machine == null) {
                return false;
            }
            String command = String.format(template, targetPath);
            String execute = SSH2Util.execute(machine, command);
            InstallationWebSocketHandler.appendLog(clusterName, execute);
            if (Strings.isNullOrEmpty(execute)) {
                return true;
            }
            command = command.replace("sudo", "");
            String execute2 = SSH2Util.execute(machine, command);
            if (Strings.isNullOrEmpty(execute2)) {
                return true;
            }
            InstallationWebSocketHandler.appendLog(clusterName, execute2);
            return false;
        } catch (Exception e) {
            String message = "Start the installation package failed, host: " + host + ", port: " + port;
            InstallationWebSocketHandler.appendLog(clusterName, message);
            InstallationWebSocketHandler.appendLog(clusterName, e.getMessage());
            logger.error(message, e);
            return false;
        }
    }

    @Override
    public boolean stop(Cluster cluster, RedisNode redisNode) {
        try {
            RedisClient redisClient = RedisClientFactory.buildRedisClient(redisNode, cluster.getRedisPassword());
            redisClient.shutdown();
            return true;
        } catch (Exception e) {
            logger.error("Stop redis node failed, " + redisNode.getHost() + ":" + redisNode.getPort(), e);
            return false;
        }
    }

    @Override
    public boolean restart(Cluster cluster, RedisNode redisNode) {
        boolean stop = stop(cluster, redisNode);
        boolean start = start(cluster, redisNode);
        return stop && start;
    }

    @Override
    public boolean remove(Cluster cluster, RedisNode redisNode) {
        String targetPath = INSTALL_BASE_PATH + redisNode.getPort();
        Machine machine = machineService.getMachineByHost(cluster.getGroupId(), redisNode.getHost());
        try {
            String rm = SSH2Util.rm(machine, targetPath, true);
            if (Strings.isNullOrEmpty(rm)) {
                return true;
            }
            String rm2 = SSH2Util.rm(machine, targetPath, false);
            if (Strings.isNullOrEmpty(rm2)) {
                return true;
            }
            logger.error("Remove redis node failed, host: " + redisNode.getHost() + ", port: " + redisNode.getPort());
            return false;
        } catch (Exception e) {
            logger.error("Remove redis node failed, host: " + redisNode.getHost() + ", port: " + redisNode.getPort(), e);
            return false;
        }

    }

    @Override
    public Map<String, String> getBaseConfigs(String bind, int port, String dir) {
        Map<String, String> configs = new HashMap<>(4);
        configs.put(DAEMONIZE, "yes");
        configs.put(BIND, bind);
        configs.put(PORT, port + "");
        configs.put(DIR, dir);
        return configs;
    }
}
