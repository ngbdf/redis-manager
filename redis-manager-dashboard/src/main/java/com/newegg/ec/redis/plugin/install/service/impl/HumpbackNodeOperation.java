package com.newegg.ec.redis.plugin.install.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.newegg.ec.redis.controller.websocket.InstallationWebSocketHandler;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.exception.ConfigurationException;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.plugin.install.service.AbstractNodeOperation;
import com.newegg.ec.redis.util.SignUtil;
import com.newegg.ec.redis.util.httpclient.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.newegg.ec.redis.util.SignUtil.SPACE;

/**
 * @author Jay.H.Zou
 * @date 10/26/2019
 */
public class HumpbackNodeOperation extends AbstractNodeOperation {

    private static final Logger logger = LoggerFactory.getLogger(HumpbackNodeOperation.class);

    @Value("${redis-manager.install.humpback.images}")
    private String images;

    @Value("${redis-manager.install.humpback.humpback-host：http://%s:8500/dockerapi/v2/}")
    private String humpbackHost;

    /** humpback api */

    private static final String GET_DOCKER_VERSION = "dockerversion";

    private static final String IMAGES = "images";

    private static final String CONTAINER = "containers";

    /** humpback api */

    public static final String DOCKER_INSTALL_BASE_PATH = "/data/redis/docker/";

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        INSTALL_BASE_PATH = DOCKER_INSTALL_BASE_PATH;
        if (Strings.isNullOrEmpty(images)) {
            throw new ConfigurationException("images not allow empty!");
        }
        if (!humpbackHost.endsWith(SignUtil.SLASH)) {
            humpbackHost += SignUtil.SLASH;
        }
    }

    @Override
    public List<String> getImageList() {
        return new ArrayList<>(Arrays.asList(SignUtil.splitByCommas(images.replace(SPACE, ""))));
    }

    @Override
    public boolean checkEnvironment(InstallationParam installationParam) {
        String clusterName = installationParam.getCluster().getClusterName();
        List<Machine> machineList = installationParam.getMachineList();
        for (Machine machine : machineList) {
            String host = machine.getHost();
            try {

            } catch (Exception e) {
                String message = "Check humpback environment failed, host: " + host;
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

        String urlTemplate = humpbackHost + IMAGES;

        JSONObject requestBody = new JSONObject();
        requestBody.put("Image", image);
        List<Future<Boolean>> resultFutureList = new ArrayList<>(machineList.size());
        for (Machine machine : machineList) {
            resultFutureList.add(threadPool.submit(() -> {
                String host = machine.getHost();
                try {
                    String post = HttpClientUtil.post(String.format(urlTemplate, host), requestBody);
                    return true;
                } catch (Exception e) {
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

//    public static void main(String[] args) throws IOException {
//        JSONObject requestBody = new JSONObject();
//        requestBody.put("Image", "docker.neg/shec/redis-4.0.8:v1");
//        String url = String.format("http://%s:8500/dockerapi/v2/images", "1xxx");
//        String post = HttpClientUtil.post(url, requestBody);
//        System.err.println(post);
//    }


    @Override
    public boolean install(InstallationParam installationParam) {
        return false;
    }

    @Override
    protected Map<String, String> getBaseConfigs(String bind, int port, String dir) {
        return null;
    }

    @Override
    public boolean start(Cluster cluster, RedisNode redisNode) {
        return false;
    }

    @Override
    public boolean stop(Cluster cluster, RedisNode redisNode) {
        return false;
    }

    @Override
    public boolean restart(Cluster cluster, RedisNode redisNode) {
        return false;
    }

    @Override
    public boolean remove(Cluster cluster, RedisNode redisNode) {
        return false;
    }

    private class HumpbackAPI {

        public JSONObject getDockerVersion(String host) throws IOException {
            String urlTemplate = humpbackHost + GET_DOCKER_VERSION;
            String response = HttpClientUtil.get(String.format(urlTemplate, host));
            return JSONObject.parseObject(response);
        }

    }


}
