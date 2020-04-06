package com.newegg.ec.redis.plugin.install.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.newegg.ec.redis.config.SystemConfig;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.exception.ConfigurationException;
import com.newegg.ec.redis.plugin.install.entity.InstallationLogContainer;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.util.RedisUtil;
import com.newegg.ec.redis.util.SignUtil;
import com.newegg.ec.redis.util.httpclient.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.newegg.ec.redis.plugin.install.DockerClientOperation.REDIS_DEFAULT_WORK_DIR;
import static com.newegg.ec.redis.util.RedisConfigUtil.REDIS_CONF;
import static com.newegg.ec.redis.util.SignUtil.SLASH;
import static com.newegg.ec.redis.util.SignUtil.SPACE;

/**
 * @author Jay.H.Zou
 * @date 10/26/2019
 */
@Component
public class HumpbackNodeOperation extends DockerNodeOperation {

    private static final Logger logger = LoggerFactory.getLogger(HumpbackNodeOperation.class);

    @Value("${redis-manager.installation.humpback.images}")
    private String images;

    @Value("${redis-manager.installation.humpback.humpback-host}")
    private String humpbackHost;

    @Autowired
    private HumpbackAPI humpbackAPI;

    @Autowired
    private SystemConfig systemConfig;

    /**
     * humpback api
     */

    private static final String GET_DOCKER_VERSION = "dockerversion";

    private static String IMAGE_OPERATION;

    private static String CONTAINER_OPERATION;

    /**
     * humpback api
     */

    public static final String DOCKER_INSTALL_BASE_PATH = "/data/redis/docker/";

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (!systemConfig.getHumpbackEnabled()) {
            return;
        }
        if (Strings.isNullOrEmpty(humpbackHost)) {
            if (Strings.isNullOrEmpty(images)) {
                throw new ConfigurationException("humpback host not allow empty!");
            }
        }
        INSTALL_BASE_PATH = DOCKER_INSTALL_BASE_PATH;
        if (Strings.isNullOrEmpty(images)) {
            throw new ConfigurationException("images not allow empty!");
        }
        if (!humpbackHost.endsWith(SLASH)) {
            humpbackHost += SLASH;
        }
        IMAGE_OPERATION = humpbackHost + "images";
        CONTAINER_OPERATION = humpbackHost + "containers";
    }

    @Override
    public List<String> getImageList() {
        return new ArrayList<>(Arrays.asList(SignUtil.splitByCommas(images.replace(SPACE, ""))));
    }

    @Override
    public boolean checkEnvironment(InstallationParam installationParam) {
        /*String clusterName = installationParam.getCluster().getClusterName();
        List<Machine> machineList = installationParam.getMachineList();
        for (Machine machine : machineList) {
            String host = machine.getHost();
            try {
                humpbackAPI.getDockerVersion(host);
            } catch (Exception e) {
                String message = "Check humpback environment failed, host: " + host;
                InstallationLogContainer.appendLog(clusterName, message);
                InstallationLogContainer.appendLog(clusterName, e.getMessage());
                logger.error(message, e);
                return false;
            }
        }*/
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
                    humpbackAPI.pullImage(host, image);
                    return true;
                } catch (Exception e) {
                    String message = "Pull docker image failed, host: " + host;
                    InstallationLogContainer.appendLog(clusterName, message);
                    InstallationLogContainer.appendLog(clusterName, e.getMessage());
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
                InstallationLogContainer.appendLog(clusterName, e.getMessage());
                logger.error("", e);
                return false;
            }
        }
        return true;
    }


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
        int port = redisNode.getPort();
        String host = redisNode.getHost();
        String containerName = RedisUtil.generateContainerName(clusterName, port);
        try {
            String containerId = redisNode.getContainerId();
            if (Strings.isNullOrEmpty(containerId)) {
                JSONObject response = humpbackAPI.createContainer(host, image, containerName, DOCKER_INSTALL_BASE_PATH + port);
                containerId = response.getString("Id");
                redisNode.setContainerId(containerId);
                redisNode.setContainerName(containerName);
            }
            humpbackAPI.runContainer(host, containerName);
            return true;
        } catch (Exception e) {
            String message = "Start container failed, host: " + host + ", port: " + port;
            InstallationLogContainer.appendLog(clusterName, message);
            InstallationLogContainer.appendLog(clusterName, e.getMessage());
            logger.error(message, e);
            return false;
        }
    }

    @Override
    public boolean stop(Cluster cluster, RedisNode redisNode) {
        try {
            humpbackAPI.stopContainer(redisNode.getHost(), redisNode.getContainerName());
            return true;
        } catch (Exception e) {
            logger.error("Stop container failed, host: " + redisNode.getHost() + ", container name: " + redisNode.getContainerName());
            return false;
        }
    }

    @Override
    public boolean restart(Cluster cluster, RedisNode redisNode) {
        try {
            humpbackAPI.restartContainer(redisNode.getHost(), redisNode.getContainerName());
            return true;
        } catch (Exception e) {
            logger.error("Restart container failed, host: " + redisNode.getHost() + ", container name: " + redisNode.getContainerName());
            return false;
        }
    }

    @Override
    public boolean remove(Cluster cluster, RedisNode redisNode) {
        try {
            humpbackAPI.removeContainer(redisNode.getHost(), redisNode.getContainerId());
            return true;
        } catch (Exception e) {
            logger.error("Remove container failed, host: " + redisNode.getHost() + ", container name: " + redisNode.getContainerName());
            return false;
        }
    }

    @Bean
    public HumpbackAPI generateHumpbackAPI() {
        return new HumpbackAPI();
    }

    private class HumpbackAPI {

        public JSONObject getDockerVersion(String host) throws IOException {
            String urlTemplate = humpbackHost + GET_DOCKER_VERSION;
            String response = HttpClientUtil.get(String.format(urlTemplate, host));
            return JSONObject.parseObject(response);
        }

        public JSONObject pullImage(String host, String image) throws IOException {
            JSONObject request = new JSONObject();
            request.put("Image", image);
            String response = HttpClientUtil.post(String.format(CONTAINER_OPERATION, host), request);
            return JSONObject.parseObject(response);
        }

        public JSONObject createContainer(String host, String image, String containerName, String hostVolume) throws IOException {
            JSONObject request = buildCreateContainerRequest(image, containerName, hostVolume);
            String response = HttpClientUtil.post(String.format(CONTAINER_OPERATION, host), request);
            return JSONObject.parseObject(response);
        }

        public JSONObject runContainer(String host, String containerName) throws IOException {
            return containerOperation(host, containerName, "start");
        }

        public JSONObject stopContainer(String host, String containerName) throws IOException {
            return containerOperation(host, containerName, "stop");
        }

        public JSONObject restartContainer(String host, String containerName) throws IOException {
            return containerOperation(host, containerName, "restart");
        }

        public JSONObject removeContainer(String host, String containerId) throws IOException {
            String urlTemplate = CONTAINER_OPERATION + SLASH + containerId + SLASH + "?force=true";
            String response = HttpClientUtil.delete(String.format(urlTemplate, host));
            return JSONObject.parseObject(response);
        }

        JSONObject containerOperation(String host, String containerName, String action) throws IOException {
            JSONObject request = new JSONObject();
            request.put("Action", action);
            request.put("Container", containerName);
            String response = HttpClientUtil.put(String.format(CONTAINER_OPERATION, host), request);
            return JSONObject.parseObject(response);
        }

        private JSONObject buildCreateContainerRequest(String image, String containerName, String hostVolume) {
            JSONObject request = new JSONObject();
            request.put("Image", image);
            JSONArray volumeArr = new JSONArray();
            JSONObject volume = new JSONObject();
            volume.put("ContainerVolume", REDIS_DEFAULT_WORK_DIR);
            volume.put("HostVolume", hostVolume);
            volumeArr.add(volume);
            request.put("Volumes", volumeArr);
            request.put("NetworkMode", "host");
            request.put("RestartPolicy", "always");
            request.put("Name", containerName);
            request.put("Command",  REDIS_DEFAULT_WORK_DIR + REDIS_CONF);
            return request;
        }
    }

}
