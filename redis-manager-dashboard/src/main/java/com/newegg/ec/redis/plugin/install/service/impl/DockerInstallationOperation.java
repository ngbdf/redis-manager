package com.newegg.ec.redis.plugin.install.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.plugin.install.service.AbstractInstallationOperation;
import com.newegg.ec.redis.plugin.install.service.DockerClientOperation;
import com.newegg.ec.redis.util.CommonUtil;
import com.newegg.ec.redis.util.RedisConfigUtil;
import com.newegg.ec.redis.util.SSH2Util;
import com.newegg.ec.redis.util.SignUtil;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.concurrent.*;

import static com.newegg.ec.redis.plugin.install.service.DockerClientOperation.REDIS_DEFAULT_WORK_DIR;
import static com.newegg.ec.redis.util.RedisConfigUtil.*;
import static com.newegg.ec.redis.util.RedisUtil.STANDALONE;
import static com.newegg.ec.redis.util.SignUtil.SPACE;

/**
 * @author Jay.H.Zou
 * @date 2019/8/12
 */
public class DockerInstallationOperation extends AbstractInstallationOperation {

    private static final Logger logger = LoggerFactory.getLogger(DockerInstallationOperation.class);

    @Value("${redis-manager.install.docker.images}")
    private String images;

    public static final String DOCKER_INSTALL_BASE_PATH = "/data/redis/docker/";

    public static final String DOCKER_TEMP_CONFIG_PATH = "/data/redis/docker/temp/";

    private static final int TIMEOUT = 300;

    @Autowired
    private DockerClientOperation dockerClientOperation;

    @Override
    public List<String> getImageList() {
        if (Strings.isNullOrEmpty(images)) {
            throw new RuntimeException("images not allow empty!");
        }
        List<String> imageList = new ArrayList<>();
        imageList.addAll(Arrays.asList(SignUtil.splitByCommas(images.replace(SPACE, ""))));
        return imageList;
    }

    /**
     * 检测连接是否正常
     * 检查机器内存Memory资源
     * 检查 docker 环境(开启docker远程访问), 2375
     *
     * @param installationParam
     * @param machineList
     * @return
     */
    @Override
    public boolean checkEnvironment(InstallationParam installationParam, List<Machine> machineList) {
        for (Machine machine : machineList) {
            String host = machine.getHost();
            try {
                dockerClientOperation.getDockerInfo(host);
            } catch (Exception e) {
                // TODO: websocket
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean buildConfig(InstallationParam installationParam) {
        // redis 集群模式
        String redisMode = installationParam.getRedisMode();
        int mode;
        if (Objects.equals(redisMode, STANDALONE)) {
            mode = STANDALONE_TYPE;
        } else {
            // default: cluster
            mode = CLUSTER_TYPE;
        }
        // 判断redis version
        Cluster cluster = installationParam.getCluster();
        try {
            // 配置文件写入本地机器
            String tempPath = DOCKER_TEMP_CONFIG_PATH + CommonUtil.replaceSpace(cluster.getClusterName());
            RedisConfigUtil.generateRedisConfig(tempPath, mode);
        } catch (Exception e) {
            // TODO: websocket
            return false;
        }
        return false;
    }

    @Override
    public boolean pullImage(InstallationParam installationParam, List<Machine> machineList) {
        String image = installationParam.getImage();
        List<Future<Boolean>> resultFutureList = new ArrayList<>(machineList.size());
        for (Machine machine : machineList) {
            resultFutureList.add(threadPool.submit(() -> {
                try {
                    dockerClientOperation.pullImage(machine.getHost(), image);
                } catch (InterruptedException e) {
                    // TODO: websocket
                    return false;
                }
                return true;
            }));
        }
        for (Future<Boolean> resultFuture : resultFutureList) {
            try {
                if (!resultFuture.get(TIMEOUT, TimeUnit.SECONDS)) {
                    return false;
                }
            } catch (Exception e) {
                // TODO: websocket
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean install(InstallationParam installationParam, List<Machine> machineList, List<RedisNode> redisNodeList) {
        /*
         * 远程机器：创建容器
         * 本机：拷贝配置文件至远程机器，删除本地临时配置文件
         * 远程机器：拷贝到端口目录，并修改配置文件
         * 启动
         * */
        boolean sudo = installationParam.isSudo();
        Cluster cluster = installationParam.getCluster();
        String tempClusterName = CommonUtil.replaceSpace(cluster.getClusterName());
        String tempPath = DOCKER_TEMP_CONFIG_PATH + tempClusterName;
        Map<Machine, List<RedisNode>> machineAndRedisNodeList = new HashMap<>(machineList.size());
        for (Machine machine : machineList) {
            List<RedisNode> machineNodes = machineAndRedisNodeList.get(machine);
            if (machineNodes == null) {
                machineNodes = new ArrayList<>();
            }
            for (RedisNode redisNode : redisNodeList) {
                if (Objects.equals(machine.getHost(), redisNode.getHost())) {
                    machineNodes.add(redisNode);
                }
            }
            machineAndRedisNodeList.put(machine, machineNodes);
        }
        // 分发配置文件
        for (Map.Entry<Machine, List<RedisNode>> entry : machineAndRedisNodeList.entrySet()) {
            Machine machine = entry.getKey();
            List<RedisNode> redisNodes = entry.getValue();
            for (RedisNode redisNode : redisNodes) {
                String targetPath = DOCKER_INSTALL_BASE_PATH + redisNode.getPort();
                try {
                    // create directory
                    SSH2Util.mkdir(machine, targetPath, sudo);
                    // TODO: 测试非sudo能否scp
                    // 本机拷贝至安装机器节点
                    //SSH2Util.scp(machine, tempPath, targetPath);
                    // 修改配置文件
                    List<Pair<String, String>> configs = getBaseConfigs(redisNode.getHost(), redisNode.getPort(), REDIS_DEFAULT_WORK_DIR);
                    RedisConfigUtil.variableAssignment(machine, targetPath, configs, sudo);
                } catch (Exception e) {
                    // TODO: websocket
                    return false;
                }
                // run docker
                try {

                } catch (Exception e) {

                }
            }
        }

        return false;
    }

    private List<Pair<String, String>> getBaseConfigs(String bind, int port, String dir) {
        List<Pair<String, String>> configs = new ArrayList<>();
        configs.add(new Pair<>(BIND, bind));
        configs.add(new Pair<>(PORT, port + ""));
        configs.add(new Pair<>(DIR, dir));
        return configs;
    }
}
