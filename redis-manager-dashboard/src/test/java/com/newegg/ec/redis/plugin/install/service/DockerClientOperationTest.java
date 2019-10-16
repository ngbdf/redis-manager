package com.newegg.ec.redis.plugin.install.service;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Info;
import com.newegg.ec.redis.plugin.install.DockerClientOperation;
import com.newegg.ec.redis.plugin.install.service.impl.DockerNodeOperation;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.newegg.ec.redis.util.RedisConfigUtil.REDIS_CONF;

public class DockerClientOperationTest {

    DockerClientOperation dockerClientOperation = new DockerClientOperation();

    String IP = "10.16.50.217";

    @Test
    public void getDockerClient() {
    }

    @Test
    public void getDockerInfo() {
        Info dockerInfo = dockerClientOperation.getDockerInfo(IP);
        System.err.println(dockerInfo);
    }

    @Test
    public void searchImages() {
        List<String> searchImages = dockerClientOperation.searchImages(IP, "java");
        searchImages.forEach(System.err::println);
    }

    @Test
    public void getImages() {
        Map<String, String> imageMap = dockerClientOperation.getImages(IP, "harbor.shec.bigdata/shec/centos");
        System.err.println(imageMap.size());
        imageMap.forEach((key, val) -> {
            System.err.println(key + " = " + val);
        });
    }

    @Test
    public void imageExist() {
        boolean exist = dockerClientOperation.imageExist(IP, "hierarchy");
        System.err.println(exist);
    }

    @Test
    public void inspectContainer() throws ParseException {
        InspectContainerResponse inspectContainerResponse = dockerClientOperation.inspectContainer(IP, "a9079e77d1d800e88b525269d88a528fd19b86a0aa54010c04b5494cead8f7cf");
        Boolean running = inspectContainerResponse.getState().getRunning();
        String created = inspectContainerResponse.getCreated();
        System.err.println(created);
        System.err.println(running);
    }

    @Test
    public void createContainer() {
        int port = 8000;
        List<String> command = new ArrayList<>();
        command.add(DockerNodeOperation.DOCKER_INSTALL_BASE_PATH + port + "/" + REDIS_CONF);
        String containerId = dockerClientOperation.createContainer(IP, port, "redis:4.0.14", "redis-instance");
        containerId = dockerClientOperation.runContainer(IP, containerId);
        System.err.println(containerId);
    }

    @Test
    public void runContainer() {
        String containerId = "ff842c9a946f";
        String container = dockerClientOperation.runContainer(IP, containerId);
        System.err.println(container);
    }

    @Test
    public void restartContainer() {
        dockerClientOperation.restartContainer(IP, "55a8eb71b887");
    }

    @Test
    public void stopContainer() {
        dockerClientOperation.stopContainer(IP, "10c09a20e218");

    }

    @Test
    public void removeContainer() {
        dockerClientOperation.removeContainer(IP, "10c09a20e218");
    }

    @Test
    public void pullImage() throws InterruptedException {
        dockerClientOperation.pullImage(IP, "redis", "4.0.14");
    }

}