package com.newegg.ec.redis.plugin.install.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.SearchImagesCmd;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * docker-java api: https://github.com/docker-java/docker-java/wiki
 *
 * @author Jay.H.Zou
 * @date 2019/8/12
 */
@Component
public class DockerClientOperation {

    @Value("${redis-manager.install.docker.docker-host:tcp://%s:2375}")
    private String dockerHost = "tcp://%s:2375";

    @Value("${redis-manager.install.docker.registry.username}")
    private String userName;

    @Value("${redis-manager.install.docker.registry.password}")
    private String password;

    @Value("${redis-manager.install.docker.api-version:2.0}")
    private String apiVersion;

    static DockerCmdExecFactory dockerCmdExecFactory = new JerseyDockerCmdExecFactory()
            .withReadTimeout(10000)
            .withConnectTimeout(10000)
            .withMaxTotalConnections(1000)
            .withMaxPerRouteConnections(100);

    /**
     * Get docker client
     *
     * @param ip
     * @return
     */
    public DockerClient getDockerClient(String ip) {
        DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder()
                // "tcp://%s:2375"
                .withDockerHost(String.format(dockerHost, ip));
        //.withDockerTlsVerify(true);
        // optional
        if (!Strings.isNullOrEmpty(userName) && !Strings.isNullOrEmpty(password)) {
            builder.withRegistryUsername(userName).withRegistryPassword(password).withRegistryEmail("jay.h.zou@newegg.com").withRegistryUrl("humpback-hub.newegg.org");
        }
        if (!Strings.isNullOrEmpty(apiVersion)) {
            builder.withApiVersion(apiVersion);
        }
        DefaultDockerClientConfig config = builder.withApiVersion("2.0").build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).withDockerCmdExecFactory(dockerCmdExecFactory).build();
        return dockerClient;
    }

    public Info getDockerInfo(String ip) {
        DockerClient dockerClient = getDockerClient(ip);
        return dockerClient.infoCmd().exec();
    }

    /**
     * @param ip
     * @param image
     * @return repoTags, imageId
     */
    public List<String> searchImages(String ip, String image) {
        DockerClient dockerClient = getDockerClient(ip);
        List<String> searchImages = new ArrayList<>();
        List<SearchItem> searchItems = dockerClient.searchImagesCmd(image).exec();
        for (SearchItem searchItem : searchItems) {
            String name = searchItem.getName();
            searchImages.add(name);
        }
        return searchImages;
    }

    /**
     * @param ip
     * @param image
     * @return repoTags, imageId
     */
    public Map<String, String> getImages(String ip, String image) {
        DockerClient dockerClient = getDockerClient(ip);
        List<Image> images = dockerClient.listImagesCmd().exec();
        Map<String, String> imageMap = new HashMap<>();
        Iterator<Image> iterator = images.iterator();
        while (iterator.hasNext()) {
            Image next = iterator.next();
            String[] repoTags = next.getRepoTags();
            for (String repoTag : repoTags) {
                if (repoTag.contains(image)) {
                    imageMap.put(repoTag, next.getId());
                }
            }
        }
        return imageMap;
    }

    /**
     * 判断 image 是否存在
     *
     * @param ip
     * @param image
     * @return
     */
    public boolean imageExist(String ip, String image) {
        Map<String, String> images = getImages(ip, image);
        return images != null && !images.isEmpty();
    }

    /**
     * Start docker container  with expose port
     *
     * @param ip
     * @param port
     * @param image
     * @return
     */
    public String startContainer(String ip, int port, String image, String containerPath, String hostPath) {
        DockerClient dockerClient = getDockerClient(ip);
        Volume volume = new Volume(hostPath);
        CreateContainerResponse container = dockerClient.createContainerCmd(image)
                // TODO: 挂载
                //.withCmd("/bin/bash")
                .withName(image + "-" + port)
                //.withBinds(new Bind("/src/webapp1", volume, true))
                //.withVolumes(volume)
                .exec();
        String containerId = container.getId();
        dockerClient.startContainerCmd(containerId).exec();
        return containerId;
    }

    /**
     * Stop docker container
     *
     * @param ip
     * @param containerId
     * @return
     */
    public void stopContainer(String ip, String containerId) {
        DockerClient dockerClient = getDockerClient(ip);
        dockerClient.stopContainerCmd(containerId).exec();
    }

    /**
     * Build docker image
     *
     * @param ip
     * @param path
     * @param image
     */
    public void buildImage(String ip, String path, String image) {
        DockerClient dockerClient = getDockerClient(ip);
        dockerClient.buildImageCmd(new File(path)).exec(new BuildImageResultCallback() {
            @Override
            public void onNext(BuildResponseItem item) {
                System.out.println("" + item);
                super.onNext(item);
            }
        });
    }

    public void pullImage(String ip, String repository, String tag) throws InterruptedException {
        DockerClient dockerClient = getDockerClient(ip);
        dockerClient.pullImageCmd(repository).withTag(tag).exec(new PullImageResultCallback()).awaitCompletion();
    }

    public void pullImage2(String ip, String image) {
        DockerClient dockerClient = getDockerClient(ip);
        dockerClient.pullImageCmd(image).withAuthConfig(dockerClient.authConfig()).exec(new PullImageResultCallback());
    }

    public void pushImage(String ip, String image) {
        DockerClient dockerClient = getDockerClient(ip);
        dockerClient.pushImageCmd(image).exec(new PushImageResultCallback());
    }
}
