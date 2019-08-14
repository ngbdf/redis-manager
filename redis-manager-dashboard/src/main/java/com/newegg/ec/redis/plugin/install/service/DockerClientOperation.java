package com.newegg.ec.redis.plugin.install.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;
import com.google.common.base.Strings;
import com.newegg.ec.redis.util.SplitUtil;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
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
    private String userName = "admin";

    @Value("${redis-manager.install.docker.registry.password}")
    private String password = "Newegg@1234";

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
        if (!Strings.isNullOrEmpty(userName) && !Strings.isNullOrEmpty(password)) {
            builder.withRegistryUsername(userName).withRegistryPassword(password);
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
     * @param repository
     * @return repoTags, imageId
     */
    public List<String> searchImages(String ip, String repository) {
        DockerClient dockerClient = getDockerClient(ip);
        List<String> searchImages = new ArrayList<>();
        List<SearchItem> searchItems = dockerClient.searchImagesCmd(repository).exec();
        for (SearchItem searchItem : searchItems) {
            String name = searchItem.getName();
            searchImages.add(name);
        }
        return searchImages;
    }

    /**
     * @param ip
     * @param imageName
     * @return repoTags, imageId
     */
    public Map<String, String> getImages(String ip, String imageName) {
        DockerClient dockerClient = getDockerClient(ip);

        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        if (!Strings.isNullOrEmpty(imageName)) {
            listImagesCmd.withImageNameFilter(imageName);
        }
        List<Image> images = listImagesCmd.exec();
        Map<String, String> imageMap = new HashMap<>();
        Iterator<Image> iterator = images.iterator();
        while (iterator.hasNext()) {
            Image next = iterator.next();
            String[] repoTags = next.getRepoTags();
            for (String repoTag : repoTags) {
                imageMap.put(repoTag, next.getId());
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
     *  sudo docker run --net host -d -v /data/8001:/data --name redis-test 857c4ab5f029 redis-server --daemonize no --cluster-enabled yes --port 8001 --bind xxx
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
     * @param imageNameWithRepository
     */
    public boolean tagImage(String ip, String imageId, String imageNameWithRepository, String tag) {
        DockerClient dockerClient = getDockerClient(ip);
        dockerClient.tagImageCmd(imageId, imageNameWithRepository, tag).exec();
        return true;
    }

    /**
     * Build docker image
     *
     * @param ip
     * @param path
     * @return repositoryAndTag
     */
    /*public Pair<String, String> buildImage(String ip, String path, String imageNameWithRepository, String tag) {
        DockerClient dockerClient = getDockerClient(ip);
        final String[] repoTag = {null};
        String imageId = dockerClient.buildImageCmd(new File(path)).exec(new BuildImageResultCallback() {
            // 完成后
            @Override
            public void onNext(BuildResponseItem item) {
                if (!Strings.isNullOrEmpty(imageNameWithRepository) && !Strings.isNullOrEmpty(tag)) {
                    tagImage(ip, item.getImageId(), imageNameWithRepository, tag);
                    repoTag[0] = imageNameWithRepository + ":" + tag;
                }
                super.onNext(item);
            }
        }).awaitImageId();

        return new Pair<>(imageId, repoTag[0]);
    }

    public Pair<String, String> buildImage(String ip, String path) {
        return buildImage(ip, path, null, null);
    }*/

    /**
     * @param ip
     * @param repository
     * @param tag
     * @return
     * @throws InterruptedException
     */
    public boolean pullImage(String ip, String repository, String tag) throws InterruptedException {
        DockerClient dockerClient = getDockerClient(ip);
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(repository);
        if (!Strings.isNullOrEmpty(tag)) {
            pullImageCmd.withTag(tag);
        }
        AuthConfig authConfig = dockerClient.authConfig();
        if (authConfig != null) {
            pullImageCmd.withAuthConfig(authConfig);
        }
        pullImageCmd.exec(new PullImageResultCallback()).awaitCompletion();
        return true;
    }

    /**
     * push 镜像
     * @param ip
     * @param repositoryAndTag
     * @throws InterruptedException
     */
    public void pushImage(String ip, String repositoryAndTag) throws InterruptedException {
        DockerClient dockerClient = getDockerClient(ip);
        AuthConfig authConfig = dockerClient.authConfig();
        PushImageCmd pushImageCmd = dockerClient.pushImageCmd(repositoryAndTag);
        if (authConfig != null) {
            pushImageCmd.withAuthConfig(authConfig);
        }
        pushImageCmd.exec(new PushImageResultCallback()).awaitCompletion();
    }
}
