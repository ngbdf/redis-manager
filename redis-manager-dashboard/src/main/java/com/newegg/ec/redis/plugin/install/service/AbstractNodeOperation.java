package com.newegg.ec.redis.plugin.install.service;

import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.newegg.ec.redis.config.SystemConfig;
import com.newegg.ec.redis.controller.websocket.InstallationWebSocketHandler;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.util.LinuxInfoUtil;
import com.newegg.ec.redis.util.NetworkUtil;
import com.newegg.ec.redis.util.RedisConfigUtil;
import com.newegg.ec.redis.util.SSH2Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static com.newegg.ec.redis.config.SystemConfig.CONFIG_ORIGINAL_PATH;
import static com.newegg.ec.redis.util.LinuxInfoUtil.MEMORY_FREE;
import static com.newegg.ec.redis.util.RedisConfigUtil.REDIS_CONF;
import static com.newegg.ec.redis.util.SignUtil.COLON;
import static com.newegg.ec.redis.util.SignUtil.SLASH;

/**
 * @author Jay.H.Zou
 * @date 2019/8/14
 */
public abstract class AbstractNodeOperation implements InstallationOperation, INodeOperation, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractNodeOperation.class);

    protected static ExecutorService threadPool = new ThreadPoolExecutor(4, 10, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("pull-image-pool-thread-%d").build(),
            new ThreadPoolExecutor.AbortPolicy());

    protected static final int TIMEOUT = 5;

    /**
     * redis-manager 数据存放目录
     */
    @Autowired
    protected SystemConfig systemConfig;

    /**
     * 目标机器安装基础目录
     */
    protected String INSTALL_BASE_PATH;

    /**
     * Check free memory of machine
     * Check ports
     *
     * @param installationParam
     * @return
     */
    public boolean checkInstallationEnv(InstallationParam installationParam) {
        boolean commonCheck = true;
        String clusterName = installationParam.getCluster().getClusterName();
        List<Machine> machineList = installationParam.getMachineList();
        for (Machine machine : machineList) {
            String host = machine.getHost();
            Map<String, String> info = null;
            try {
                info = LinuxInfoUtil.getLinuxInfo(machine);
            } catch (Exception e) {
                String message = "Get " + host + " info failed";
                InstallationWebSocketHandler.appendLog(clusterName, message);
                InstallationWebSocketHandler.appendLog(clusterName, e.getMessage());
                logger.error(message, e);
                commonCheck = false;
            }
            String memoryFreeStr = info.get(MEMORY_FREE);
            if (Strings.isNullOrEmpty(memoryFreeStr)) {
                InstallationWebSocketHandler.appendLog(clusterName, "Can't get " + host + " memory info.");
                commonCheck = false;
            } else {
                Integer memoryFree = Integer.valueOf(memoryFreeStr);
                if (memoryFree <= MIN_MEMORY_FREE) {
                    InstallationWebSocketHandler.appendLog(clusterName, host + " not enough memory, free memory: " + memoryFree);
                    commonCheck = false;
                }
            }
            // TODO: for test
            commonCheck = true;
        }
        return commonCheck && checkEnvironment(installationParam);
    }

    public boolean checkPorts(InstallationParam installationParam) {
        if (installationParam.isAutoBuild()) {
            return true;
        }
        String clusterName = installationParam.getCluster().getClusterName();
        List<RedisNode> allRedisNodes = installationParam.getRedisNodeList();
        for (RedisNode redisNode : allRedisNodes) {
            String ip = redisNode.getHost();
            int port = redisNode.getPort();
            // 如果端口能通，则认为该端口被占用
            if (NetworkUtil.telnet(ip, port)) {
                InstallationWebSocketHandler.appendLog(clusterName, "The port has been used, host: " + ip + ", port: " + port);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean pullConfig(InstallationParam installationParam) {
        boolean sudo = installationParam.isSudo();
        String clusterName = installationParam.getCluster().getClusterName();
        String url;
        // 模式
        String redisMode = installationParam.getCluster().getRedisMode();
        try {
            // eg: ip:port/redis-manager/config/cluster/redis.conf
            url = LinuxInfoUtil.getIpAddress() + COLON + systemConfig.getServerPort() + CONFIG_ORIGINAL_PATH + redisMode + SLASH + REDIS_CONF;
        } catch (SocketException e) {
            return false;
        }
        List<Machine> machineList = installationParam.getMachineList();
        String tempPath = INSTALL_BASE_PATH + redisMode;

        for (Machine machine : machineList) {
            try {
                // 将 redis.conf 分发到目标机器的临时目录
                String result = SSH2Util.copyFileToRemote(machine, tempPath, url, sudo);
                logger.warn(result);
                if (Strings.isNullOrEmpty(result) || !result.contains("OK")) {
                    String message = "Copy redis.conf to target machine failed, host: " + machine.getHost();
                    InstallationWebSocketHandler.appendLog(clusterName, message);
                    return false;
                }
            } catch (Exception e) {
                String message = "Download redis.conf to target machine failed, host: " + machine.getHost();
                InstallationWebSocketHandler.appendLog(clusterName, message);
                InstallationWebSocketHandler.appendLog(clusterName, e.getMessage());
                logger.error(message, e);
                return false;
            }
        }
        // copy 配置文件，并修改参数
        String tempRedisConf = tempPath + SLASH + REDIS_CONF;
        Multimap<Machine, RedisNode> machineAndRedisNode = installationParam.getMachineAndRedisNode();
        for (Map.Entry<Machine, RedisNode> entry : machineAndRedisNode.entries()) {
            Machine machine = entry.getKey();
            RedisNode redisNode = entry.getValue();
            String targetPath = INSTALL_BASE_PATH + redisNode.getPort();
            try {
                // 清理、复制
                String copyResult = SSH2Util.copy2(machine, tempRedisConf, targetPath, sudo);
                if (!Strings.isNullOrEmpty(copyResult)) {
                    InstallationWebSocketHandler.appendLog(clusterName, copyResult);
                    return false;
                }
                // 修改配置文件
                Map<String, String> configs = getBaseConfigs(redisNode.getHost(), redisNode.getPort(), targetPath);
                String changeResult = RedisConfigUtil.variableAssignment(machine, targetPath, configs, sudo);
                if (!Strings.isNullOrEmpty(changeResult)) {
                    InstallationWebSocketHandler.appendLog(clusterName, changeResult);
                    return false;
                }
            } catch (Exception e) {
                String message = "Copy or change redis.conf to target machine failed, host: " + machine.getHost();
                InstallationWebSocketHandler.appendLog(clusterName, message);
                InstallationWebSocketHandler.appendLog(clusterName, e.getMessage());
                logger.error(message, e);
                return false;
            }
        }
        return true;
    }

    protected abstract Map<String, String> getBaseConfigs(String bind, int port, String dir);
}
