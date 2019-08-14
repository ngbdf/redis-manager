package com.newegg.ec.redis.plugin.install.service;

import com.github.dockerjava.api.model.Info;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class DockerClientOperationTest {

    DockerClientOperation dockerClientOperation = new DockerClientOperation();

    String IP = "10.16.46.171";

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
    public void startContainer() {
    }

    @Test
    public void stopContainer() {
    }

    @Test
    public void pullImage() throws InterruptedException {
        dockerClientOperation.pullImage(IP, "harbor.shec.bigdata/shec/itemservice-spring-dev", "1.3.5.0");
    }

    @Test
    public void tagImage() {
        dockerClientOperation.tagImage(IP, "docker.neg/shec/redis-4.0.10:v3", "harbor.shec.bigdata/shec/redis-4.0.10", "1.0");
    }

    /*@Test
    public void buildImage() {
        dockerClientOperation.buildImage(IP, "/opt/app/docker/redis-4.0.8/Dockerfile", "redis-4-test", "v1");
    }*/

    @Test
    public void pushImage() throws InterruptedException {
        dockerClientOperation.pushImage(IP, "harbor.shec.bigdata/shec/redis-4.0.10:1.0");
    }
}