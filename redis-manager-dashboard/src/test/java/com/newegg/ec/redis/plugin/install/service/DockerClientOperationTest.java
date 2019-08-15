package com.newegg.ec.redis.plugin.install.service;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Info;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DockerClientOperationTest {

    DockerClientOperation dockerClientOperation = new DockerClientOperation();

    String IP = "10.1.1.1";

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
        InspectContainerResponse inspectContainerResponse = dockerClientOperation.inspectContainer(IP, "55a8eb71b888");
        Boolean running = inspectContainerResponse.getState().getRunning();
        String created = inspectContainerResponse.getCreated();
        System.err.println(created);
        System.err.println(running);
    }

    @Test
    public void runContainer() {
        List<String> command = new ArrayList<>();
        command.add("--daemonize no");
        command.add("--cluster-enabled yes");
        command.add("--port 8007");
        command.add("--bind 10.16.50.217");
        dockerClientOperation.runContainer(IP, 8007, "redis:4.0.14", "redis-instance", command);
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
        dockerClientOperation.pullImage(IP, "harbor.shec.bigdata/shec/itemservice-spring-dev", "1.3.5.0");
    }

}