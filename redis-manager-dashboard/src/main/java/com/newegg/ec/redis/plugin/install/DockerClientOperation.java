package com.newegg.ec.redis.plugin.install;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;
import com.google.common.base.Strings;
import com.newegg.ec.redis.util.CommonUtil;
import com.newegg.ec.redis.util.SignUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.newegg.ec.redis.plugin.install.service.impl.DockerNodeOperation.DOCKER_INSTALL_BASE_PATH;
import static com.newegg.ec.redis.util.RedisConfigUtil.REDIS_CONF;
import static com.newegg.ec.redis.util.SignUtil.MINUS;


/**
 * docker-java api: https://github.com/docker-java/docker-java/wiki
 *
 * @author Jay.H.Zou
 * @date 2019/8/12
 */
@Component
public class DockerClientOperation {

    @Value("${redis-manager.install.docker.docker-host:tcp://%s:2375}")
    private String dockerHost = "tcp://%s:2375/";

    private static final String VOLUME = DOCKER_INSTALL_BASE_PATH + "%d:/data";

    public static final String REDIS_DEFAULT_WORK_DIR = "/data/";

    static DockerCmdExecFactory dockerCmdExecFactory = new JerseyDockerCmdExecFactory()
            .withReadTimeout(300000)
            .withConnectTimeout(300000)
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
                .withDockerHost(String.format(dockerHost, ip));
        DefaultDockerClientConfig config = builder.build();
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


    public InspectContainerResponse inspectContainer(String ip, String containerId) {
        DockerClient dockerClient = getDockerClient(ip);
        return dockerClient.inspectContainerCmd(containerId).exec();
    }

    /**
     * redis.conf 由程序生成并修改
     * <p>
     * Start docker container  with expose port
     * sudo docker run \
     * --name redis-instance-8000 \
     * --net host \
     * -d -v /data/redis/docker/8000:/data \
     * redis:4.0.14 \
     * redis-server /data/redis.conf
     *
     * @param ip
     * @param port
     * @param image
     * @return
     */
    public String createContainer(String ip, int port, String image, String containerName) {
        DockerClient dockerClient = getDockerClient(ip);
        String bind = String.format(VOLUME, port);
        CreateContainerResponse container = dockerClient.createContainerCmd(image)
                // container name
                .withName(CommonUtil.replaceSpace(containerName))
                // host 模式启动
                .withHostConfig(new HostConfig().withNetworkMode("host"))
                // 挂载
                .withBinds(Bind.parse(bind))
                // 自动启动
                .withRestartPolicy(RestartPolicy.alwaysRestart())
                // 配置文件
                .withCmd(REDIS_DEFAULT_WORK_DIR + REDIS_CONF)
                .exec();
        return container.getId();
    }

    public String runContainer(String ip, String containerId) {
        DockerClient dockerClient = getDockerClient(ip);
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
    public void restartContainer(String ip, String containerId) {
        DockerClient dockerClient = getDockerClient(ip);
        dockerClient.restartContainerCmd(containerId).exec();
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
     * Remove docker container
     *
     * @param ip
     * @param containerId
     * @return
     */
    public void removeContainer(String ip, String containerId) {
        DockerClient dockerClient = getDockerClient(ip);
        dockerClient.removeContainerCmd(containerId).exec();
    }

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
        pullImageCmd.exec(new PullImageResultCallback()).awaitCompletion();
        return true;
    }

    /**
     * @param ip
     * @param repositoryAndTag
     * @return
     * @throws InterruptedException
     */
    public boolean pullImage(String ip, String repositoryAndTag) throws InterruptedException {
        String[] repoAndTag = SignUtil.splitByColon(repositoryAndTag);
        String tag = null;
        if (repoAndTag.length > 1) {
            tag = repoAndTag[1];
        }
        String repository = repoAndTag[0];
        return pullImage(ip, repository, tag);
    }
}
