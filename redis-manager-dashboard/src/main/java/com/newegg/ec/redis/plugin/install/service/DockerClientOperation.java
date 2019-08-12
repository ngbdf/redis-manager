package com.newegg.ec.redis.plugin.install.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * docker-java api: https://github.com/docker-java/docker-java/wiki
 *
 * @author Jay.H.Zou
 * @date 2019/8/12
 */
public class DockerClientOperation {

    @Value("${redis-manager.install.docker.docker-host:tcp://%s:2375}")
    private String dockerHost;

    static DockerCmdExecFactory dockerCmdExecFactory = new JerseyDockerCmdExecFactory()
            .withReadTimeout(1000)
            .withConnectTimeout(1000)
            .withMaxTotalConnections(100)
            .withMaxPerRouteConnections(10);

    /**
     * Get docker client
     *
     * @param ip
     * @return
     */
    public DockerClient getDockerClient(String ip) {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                // "tcp://%s:2376"
                .withDockerHost(String.format(dockerHost, ip))
                .withDockerTlsVerify(true)
                // optional
                .withDockerCertPath("/home/user/.docker")
                .withRegistryUsername("userName")
                .withRegistryPassword("password")
                .withRegistryEmail("github.com")
                .withRegistryUrl("https://index.docker.io/v2/")
                .withApiVersion("1.30")
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).withDockerCmdExecFactory(dockerCmdExecFactory).build();
        return dockerClient;
    }

    public Info getDockerInfo(String ip) {
        DockerClient dockerClient = getDockerClient(ip);
        return dockerClient.infoCmd().exec();
    }

    public List<SearchItem> getImages(String ip, String image) {
        DockerClient dockerClient = getDockerClient(ip);
        List<SearchItem> images = dockerClient.searchImagesCmd(image).exec();
        return images;
    }

    /**
     * 判断 image 是否存在
     *
     * @param ip
     * @param image
     * @return
     */
    public boolean imageExist(String ip, String image) {
        List<SearchItem> images = getImages(ip, image);
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
                .withCmd("/bin/bash")
                .withName(image + "-" + port)
                .withVolumes(volume)
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
        dockerClient.buildImageCmd(new File(path)).exec(new ResultCallback<BuildResponseItem>() {
            @Override
            public void onStart(Closeable closeable) {

            }

            @Override
            public void onNext(BuildResponseItem object) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void close() throws IOException {

            }
        });
    }

    public void pullImage(String ip, String image, String repository) {
        DockerClient dockerClient = getDockerClient(ip);
        dockerClient.pullImageCmd(image).withRepository(repository).exec(new ResultCallback<PullResponseItem>() {
            @Override
            public void onStart(Closeable closeable) {

            }

            @Override
            public void onNext(PullResponseItem object) {
                System.out.println(object.getStatus());
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("pull finished");
            }

            @Override
            public void close() throws IOException {

            }
        });
    }

    public void pushImage(String ip, String image) {
        DockerClient dockerClient = getDockerClient(ip);
        dockerClient.pushImageCmd(image).exec(new ResultCallback<PushResponseItem>() {
            @Override
            public void onStart(Closeable closeable) {

            }

            @Override
            public void onNext(PushResponseItem object) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void close() throws IOException {

            }
        });
    }
}
