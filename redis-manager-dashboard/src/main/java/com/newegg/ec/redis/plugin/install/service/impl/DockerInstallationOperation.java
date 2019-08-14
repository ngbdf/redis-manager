package com.newegg.ec.redis.plugin.install.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.plugin.install.service.DockerClientOperation;
import com.newegg.ec.redis.plugin.install.service.AbstractInstallationOperation;
import com.newegg.ec.redis.util.SplitUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/8/12
 */
public class DockerInstallationOperation extends AbstractInstallationOperation {

    private static final Logger logger = LoggerFactory.getLogger(DockerInstallationOperation.class);

    @Value("${redis-manager.install.docker.images}")
    private String images;

    @Autowired
    private DockerClientOperation dockerClientOperation;

    @Override
    public List<String> getPackageList() {
        if (Strings.isNullOrEmpty(images)) {
            throw new RuntimeException("images not allow empty!");
        }
        List<String> iamgeList = new ArrayList<>();
        iamgeList.addAll(Arrays.asList(SplitUtil.splitByCommas(images.replace(" ", ""))));
        return iamgeList;
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

        return true;
    }

    @Override
    public boolean pullImage() {
        return false;
    }

    @Override
    public boolean buildConfig() {
        return false;
    }

    @Override
    public boolean install(List<RedisNode> redisNodeList) {
        return false;
    }
}
