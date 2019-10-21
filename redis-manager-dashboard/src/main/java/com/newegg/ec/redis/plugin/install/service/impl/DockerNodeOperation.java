package com.newegg.ec.redis.plugin.install.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.controller.websocket.InstallationWebSocketHandler;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.exception.ConfigurationException;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.plugin.install.service.AbstractNodeOperation;
import com.newegg.ec.redis.plugin.install.DockerClientOperation;
import com.newegg.ec.redis.plugin.install.service.INodeOperation;
import com.newegg.ec.redis.util.SignUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.newegg.ec.redis.util.RedisConfigUtil.*;
import static com.newegg.ec.redis.util.SignUtil.MINUS;
import static com.newegg.ec.redis.util.SignUtil.SPACE;

/**
 * @author Jay.H.Zou
 * @date 2019/8/12
 */
@Component
public class DockerNodeOperation extends AbstractNodeOperation implements INodeOperation {

    private static final Logger logger = LoggerFactory.getLogger(DockerNodeOperation.class);

    @Value("${redis-manager.install.docker.images}")
    private String images;

    public static final String DOCKER_INSTALL_BASE_PATH = "/data/redis/docker/";

    @Autowired
    private DockerClientOperation dockerClientOperation;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        INSTALL_BASE_PATH = DOCKER_INSTALL_BASE_PATH;
        if (Strings.isNullOrEmpty(images)) {
            throw new ConfigurationException("images not allow empty!");
        }
    }

    @Override
    public List<String> getImageList() {
        return new ArrayList<>(Arrays.asList(SignUtil.splitByCommas(images.replace(SPACE, ""))));
    }

    /**
     * 检测连接是否正常
     * 检查机器内存Memory资源
     * 检查 docker 环境(开启docker远程访问), 2375
     *
     * @param installationParam
     * @return
     */
    @Override
    public boolean checkEnvironment(InstallationParam installationParam) {
        String clusterName = installationParam.getCluster().getClusterName();
        List<Machine> machineList = installationParam.getMachineList();
        for (Machine machine : machineList) {
            String host = machine.getHost();
            try {
                dockerClientOperation.getDockerInfo(host);
            } catch (Exception e) {
                String message = "Check docker environment failed, host: " + host;
                InstallationWebSocketHandler.appendLog(clusterName, message);
                InstallationWebSocketHandler.appendLog(clusterName, e.getMessage());
                logger.error(message, e);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean pullImage(InstallationParam installationParam) {
        List<Machine> machineList = installationParam.getMachineList();
        Cluster cluster = installationParam.getCluster();
        String clusterName = cluster.getClusterName();
        String image = cluster.getImage();
        List<Future<Boolean>> resultFutureList = new ArrayList<>(machineList.size());
        for (Machine machine : machineList) {
            resultFutureList.add(threadPool.submit(() -> {
                String host = machine.getHost();
                try {
                    dockerClientOperation.pullImage(host, image);
                    return true;
                } catch (InterruptedException e) {
                    String message = "Pull docker image failed, host: " + host;
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
        return true;
    }

    /**
     * run container
     *
     * @param installationParam
     * @return
     */
    @Override
    public boolean install(InstallationParam installationParam) {
        List<RedisNode> allRedisNodes = installationParam.getRedisNodeList();
        Cluster cluster = installationParam.getCluster();
        for (RedisNode redisNode : allRedisNodes) {
            boolean start = start(cluster, redisNode);
            if (!start) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean start(Cluster cluster, RedisNode redisNode) {
        String image = cluster.getImage();
        String clusterName = cluster.getClusterName();
        String containerNamePrefix = SignUtil.replaceSpaceToMinus(clusterName);
        int port = redisNode.getPort();
        String host = redisNode.getHost();
        String containerName = (containerNamePrefix + MINUS + port).toLowerCase();
        try {
            String containerId = redisNode.getContainerId();
            if (Strings.isNullOrEmpty(containerId)) {
                containerId = dockerClientOperation.createContainer(host, port, image, containerName);
                dockerClientOperation.runContainer(host, containerId);
                redisNode.setContainerId(containerId);
                redisNode.setContainerName(containerName);
            } else {
                dockerClientOperation.runContainer(host, containerId);
            }
            return true;
        } catch (Exception e) {
            String message = "Start container failed, host: " + host + ", port: " + port;
            InstallationWebSocketHandler.appendLog(clusterName, message);
            InstallationWebSocketHandler.appendLog(clusterName, e.getMessage());
            logger.error(message, e);
            return false;
        }
    }

    @Override
    public boolean stop(Cluster cluster, RedisNode redisNode) {
        try {
            dockerClientOperation.stopContainer(redisNode.getHost(), redisNode.getContainerId());
            return true;
        } catch (Exception e) {
            logger.error("Stop container failed, host: " + redisNode.getHost() + ", container name: " + redisNode.getContainerName());
            return false;
        }
    }

    @Override
    public boolean restart(Cluster cluster, RedisNode redisNode) {
        try {
            dockerClientOperation.restartContainer(redisNode.getHost(), redisNode.getContainerId());
            return true;
        } catch (Exception e) {
            logger.error("Restart container failed, host: " + redisNode.getHost() + ", container name: " + redisNode.getContainerName());
            return false;
        }
    }

    @Override
    public boolean remove(Cluster cluster, RedisNode redisNode) {
        try {
            dockerClientOperation.removeContainer(redisNode.getHost(), redisNode.getContainerId());
            return true;
        } catch (Exception e) {
            logger.error("Remove container failed, host: " + redisNode.getHost() + ", container name: " + redisNode.getContainerName());
            return false;
        }
    }

    @Override
    public Map<String, String> getBaseConfigs(String bind, int port, String dir) {
        Map<String, String> configs = new HashMap<>(3);
        configs.put(DAEMONIZE, "no");
        configs.put(BIND, bind);
        configs.put(PORT, port + "");
        configs.put(DIR, "/data/");
        return configs;
    }
}
