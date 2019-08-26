package com.newegg.ec.redis.plugin.install.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.plugin.install.service.AbstractInstallationOperation;
import com.newegg.ec.redis.plugin.install.service.DockerClientOperation;
import com.newegg.ec.redis.util.SignUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.newegg.ec.redis.util.SignUtil.MINUS;
import static com.newegg.ec.redis.util.SignUtil.SPACE;

/**
 * @author Jay.H.Zou
 * @date 2019/8/12
 */
public class DockerInstallationOperation extends AbstractInstallationOperation {

    private static final Logger logger = LoggerFactory.getLogger(DockerInstallationOperation.class);

    @Value("${redis-manager.install.docker.images}")
    private String images;

    public static final String DOCKER_INSTALL_BASE_PATH = "/data/redis/docker/";

    private static final int TIMEOUT = 300;

    @Autowired
    private DockerClientOperation dockerClientOperation;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        INSTALL_BASE_PATH = DOCKER_INSTALL_BASE_PATH;
    }

    @Override
    public List<String> getImageList() {
        if (Strings.isNullOrEmpty(images)) {
            throw new RuntimeException("images not allow empty!");
        }
        List<String> imageList = new ArrayList<>();
        imageList.addAll(Arrays.asList(SignUtil.splitByCommas(images.replace(SPACE, ""))));
        return imageList;
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
        List<Machine> machineList = installationParam.getMachineList();
        for (Machine machine : machineList) {
            String host = machine.getHost();
            try {
                dockerClientOperation.getDockerInfo(host);
            } catch (Exception e) {
                // TODO: websocket
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean pullImage(InstallationParam installationParam) {
        List<Machine> machineList = installationParam.getMachineList();
        String image = installationParam.getImage();
        List<Future<Boolean>> resultFutureList = new ArrayList<>(machineList.size());
        for (Machine machine : machineList) {
            resultFutureList.add(threadPool.submit(() -> {
                try {
                    dockerClientOperation.pullImage(machine.getHost(), image);
                    return true;
                } catch (InterruptedException e) {
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
        List<RedisNode> redisNodeList = installationParam.getRedisNodeList();
        String image = installationParam.getImage();
        Cluster cluster = installationParam.getCluster();
        String clusterName = cluster.getClusterName();
        String containerNamePrefix = SignUtil.replaceSpaceToMinus(clusterName);
        for (RedisNode redisNode : redisNodeList) {
            int port = redisNode.getPort();
            String host = redisNode.getHost();
            String containerName = containerNamePrefix + MINUS + port;
            try {
                String containerId = dockerClientOperation.createContainer(host, port, image, containerName);
                dockerClientOperation.runContainer(host, containerId);
            } catch (Exception e) {
                // TODO: websocket
                return false;
            }
        }
        return true;
    }
}
