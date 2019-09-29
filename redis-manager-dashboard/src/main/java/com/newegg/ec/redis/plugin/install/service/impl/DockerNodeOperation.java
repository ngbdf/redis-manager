package com.newegg.ec.redis.plugin.install.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
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
            throw new RuntimeException("images not allow empty!");
        }
    }

    @Override
    public List<String> getImageList() {
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
        Cluster cluster = installationParam.getCluster();
        String image = cluster.getImage();
        List<Future<Boolean>> resultFutureList = new ArrayList<>(machineList.size());
        for (Machine machine : machineList) {
            resultFutureList.add(threadPool.submit(() -> {
                try {
                    dockerClientOperation.pullImage(machine.getHost(), image);
                    return true;
                } catch (InterruptedException e) {
                    // TODO: websocket
                    e.printStackTrace();
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
                e.printStackTrace();
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
            boolean start = start(cluster, null, redisNode);
            if (!start) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean start(Cluster cluster, Machine machine, RedisNode redisNode) {
        String image = cluster.getImage();
        String clusterName = cluster.getClusterName();
        String containerNamePrefix = SignUtil.replaceSpaceToMinus(clusterName);
        int port = redisNode.getPort();
        String host = redisNode.getHost();
        String containerName = containerNamePrefix + MINUS + port;
        try {
            String containerId = dockerClientOperation.createContainer(host, port, image, containerName);
            dockerClientOperation.runContainer(host, containerId);
            return true;
        } catch (Exception e) {
            // TODO: websocket
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Map<String, String> getBaseConfigs(String bind, int port) {
        Map<String, String> configs = new HashMap<>(3);
        configs.put(DAEMONIZE, "no");
        configs.put(BIND, bind);
        configs.put(PORT, port + "");
        return configs;
    }
}
