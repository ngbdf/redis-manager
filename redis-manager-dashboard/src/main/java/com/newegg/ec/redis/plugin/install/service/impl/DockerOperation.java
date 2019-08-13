package com.newegg.ec.redis.plugin.install.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.plugin.install.service.InstallationOperation;
import com.newegg.ec.redis.util.LinuxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.newegg.ec.redis.util.LinuxUtil.MEMORY_FREE;

/**
 * @author Jay.H.Zou
 * @date 2019/8/12
 */
public class DockerOperation implements InstallationOperation {

    private static final Logger logger = LoggerFactory.getLogger(DockerOperation.class);

    @Override
    public List<String> getPackageList() {
        return null;
    }

    /**
     * 检测连接是否正常
     * 检查机器内存Memory资源
     * 检查 docker 环境(开启docker远程访问), 2375
     * @param installationParam
     * @param machineList
     * @return
     */
    @Override
    public boolean checkEnvironment(InstallationParam installationParam, List<Machine> machineList) {
        for (Machine machine : machineList) {
            Map<String, String> info;
            try {
                info = LinuxUtil.getLinuxInfo(machine);
            } catch (Exception e) {
                // TODO: websocket
                logger.error("Get " + machine.getHost() + " failed", e);
                return false;
            }
            String memoryFreeStr = info.get(MEMORY_FREE);
            if (Strings.isNullOrEmpty(memoryFreeStr)) {
                // TODO: websocket
                return false;
            } else {
                Integer memoryFree = Integer.valueOf(memoryFreeStr);
                if (memoryFree <= MIN_MEMORY_FREE) {
                    // TODO: websocket
                    return false;
                }
            }
        }
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
