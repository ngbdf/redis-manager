package com.newegg.ec.redis.plugin.install.service;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.util.LinuxInfoUtil;
import com.newegg.ec.redis.util.NetworkUtil;
import com.newegg.ec.redis.util.RedisConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.newegg.ec.redis.util.LinuxInfoUtil.MEMORY_FREE;
import static com.newegg.ec.redis.util.RedisConfigUtil.*;
import static com.newegg.ec.redis.util.RedisUtil.STANDALONE;
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

    @Value("${redis-manager.install.data-dir:/data/redis-manager/}")
    private String dataDir;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (!dataDir.endsWith(SLASH)) {
            dataDir += SLASH;
        }
        File file = new File(dataDir);
        if (!file.exists()) {
            if (file.mkdirs()) {
                throw new RuntimeException(dataDir + " create failed.");
            }
        }
    }

    /**
     * Check free memory of machine
     * Check ports
     *
     * @param installationParam
     * @param machineList
     * @return
     */
    public boolean checkInstallationEnv(InstallationParam installationParam, List<Machine> machineList) {
        boolean commonCheck = true;
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
        return commonCheck && checkEnvironment(installationParam, machineList);
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
        try {
            // 配置文件写入本地机器
            String tempPath = redisConfPath(dataDir, cluster.getClusterName());
            RedisConfigUtil.generateRedisConfig(tempPath, mode);
        } catch (Exception e) {
            // TODO: websocket
            return false;
        }
        return true;
    }


    public void clean(InstallationParam installationParam) {
        // 删除redis.conf临时目录
        Cluster cluster = installationParam.getCluster();
        String tempPath = redisConfDir(dataDir, cluster.getClusterName());
        File tempRedisConf = new File(tempPath);
        tempRedisConf.delete();

    }

}
