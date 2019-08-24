package com.newegg.ec.redis.plugin.install.service;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.newegg.ec.redis.config.SystemConfig;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.util.LinuxInfoUtil;
import com.newegg.ec.redis.util.NetworkUtil;
import com.newegg.ec.redis.util.RedisConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.newegg.ec.redis.plugin.install.service.DockerClientOperation.REDIS_DEFAULT_WORK_DIR;
import static com.newegg.ec.redis.util.LinuxInfoUtil.MEMORY_FREE;
import static com.newegg.ec.redis.util.RedisConfigUtil.*;
import static com.newegg.ec.redis.util.SignUtil.COLON;
import static com.newegg.ec.redis.util.SignUtil.SLASH;

/**
 * @author Jay.H.Zou
 * @date 2019/8/14
 */
public abstract class AbstractInstallationOperation implements InstallationOperation, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractInstallationOperation.class);

    protected static ExecutorService threadPool = new ThreadPoolExecutor(10, 20, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("pull-image-pool-thread-%d").build(),
            new ThreadPoolExecutor.AbortPolicy());

    @Value("${server.port}")
    private int serverPort;

    /**
     * redis-manager 数据存放目录
     */
    @Autowired
    private SystemConfig systemConfig;

    /**
     * 目标机器安装基础目录
     */
    protected String INSTALL_BASE_PATH;

    protected AbstractInstallationOperation() {
    }

    public void installInfoPrepare(InstallationParam installationParam) {
        Multimap<Machine, RedisNode> machineAndRedisNode = ArrayListMultimap.create();
        List<Machine> machineList = installationParam.getMachineList();
        List<RedisNode> redisNodeList = installationParam.getRedisNodeList();
        for (Machine machine : machineList) {
            for (RedisNode redisNode : redisNodeList) {
                if (Objects.equals(machine.getHost(), redisNode.getHost())) {
                    machineAndRedisNode.put(machine, redisNode);
                }
            }
        }
        installationParam.setMachineAndRedisNode(machineAndRedisNode);
    }

    /**
     * Check free memory of machine
     * Check ports
     *
     * @param installationParam
     * @return
     */
    public boolean checkInstallationEnv(InstallationParam installationParam) {
        boolean commonCheck = true;
        List<Machine> machineList = installationParam.getMachineList();
        for (Machine machine : machineList) {
            Map<String, String> info = null;
            try {
                info = LinuxInfoUtil.getLinuxInfo(machine);
            } catch (Exception e) {
                // TODO: websocket
                logger.error("Get " + machine.getHost() + " failed", e);
                commonCheck = false;
            }
            String memoryFreeStr = info.get(MEMORY_FREE);
            if (Strings.isNullOrEmpty(memoryFreeStr)) {
                // TODO: websocket
                commonCheck = false;
            } else {
                Integer memoryFree = Integer.valueOf(memoryFreeStr);
                if (memoryFree <= MIN_MEMORY_FREE) {
                    // TODO: websocket
                    commonCheck = false;
                }
            }
        }

        List<RedisNode> redisNodeList = installationParam.getRedisNodeList();
        for (RedisNode redisNode : redisNodeList) {
            String ip = redisNode.getHost();
            int port = redisNode.getPort();
            // 如果端口能通，则认为该端口被占用
            if (NetworkUtil.telnet(ip, port)) {
                // TODO: websocket
                commonCheck = false;
            }
        }
        return commonCheck && checkEnvironment(installationParam);
    }

    @Override
    public boolean buildConfig(InstallationParam installationParam) {
        boolean sudo = installationParam.isSudo();
        String url;
        try {
            // 模式
            String redisMode = installationParam.getRedisMode();
            // eg: ip:port/config/cluster/redis.conf
            url = LinuxInfoUtil.getIpAddress() + COLON + serverPort + CONFIG_PATH + redisMode + SLASH + REDIS_CONF;
        } catch (SocketException e) {
            return false;
        }
        Multimap<Machine, RedisNode> machineAndRedisNode = installationParam.getMachineAndRedisNode();
        // 分发配置文件
        for (Map.Entry<Machine, RedisNode> entry : machineAndRedisNode.entries()) {
            Machine machine = entry.getKey();
            RedisNode redisNode = entry.getValue();
            String targetPath = INSTALL_BASE_PATH + redisNode.getPort();
            try {
                RedisConfigUtil.copyRedisConfigToRemote(machine, targetPath, url, sudo);
                // 修改配置文件
                Map<String, String> configs = getBaseConfigs(redisNode.getHost(), redisNode.getPort(), REDIS_DEFAULT_WORK_DIR);
                RedisConfigUtil.variableAssignment(machine, targetPath, configs, sudo);
            } catch (Exception e) {
                // TODO: websocket
                return false;
            }
        }
        return true;
    }

    private Map<String, String> getBaseConfigs(String bind, int port, String dir) {
        Map<String, String> configs = new HashMap<>(3);
        configs.put(BIND, bind);
        configs.put(PORT, port + "");
        configs.put(DIR, dir);
        return configs;
    }
}
