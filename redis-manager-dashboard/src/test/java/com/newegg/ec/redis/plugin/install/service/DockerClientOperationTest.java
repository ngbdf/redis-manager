package com.newegg.ec.redis.plugin.install.service;

import com.github.dockerjava.api.model.Info;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class DockerClientOperationTest {

    DockerClientOperation dockerClientOperation = new DockerClientOperation();

    String IP = "";

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
        Map<String, String> imageMap = dockerClientOperation.getImages(IP, "redis");
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
    public void startContainer() {
    }

    @Test
    public void stopContainer() {
    }

    @Test
    public void buildImage() {
        dockerClientOperation.buildImage(IP, "/opt/app/redis/Dockerfile", "redis-4-test:v1");
    }

    @Test
    public void pullImage() throws InterruptedException {
        dockerClientOperation.pullImage(IP, "https://index.docker.io/v1/redis/", "latest");
    }

    @Test
    public void pushImage() {
    }
}