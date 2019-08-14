package com.newegg.ec.redis.plugin.install.service;

import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.util.LinuxUtil;
import com.newegg.ec.redis.util.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.newegg.ec.redis.util.LinuxUtil.MEMORY_FREE;

/**
 * @author Jay.H.Zou
 * @date 2019/8/14
 */
public abstract class AbstractInstallationOperation implements InstallationOperation {

    private static final Logger logger = LoggerFactory.getLogger(AbstractInstallationOperation.class);

    /**
     * Check free memory of machine
     * Check ports
     *
     * @param installationParam
     * @param machineList
     * @return
     */
    public boolean checkInstallationEnv(InstallationParam installationParam, List<Machine> machineList) {
        boolean commonCheck = true;
        for (Machine machine : machineList) {
            Map<String, String> info = null;
            try {
                info = LinuxUtil.getLinuxInfo(machine);
            } catch (Exception e) {
                // TODO: websocket
                logger.error("Get " + machine.getHost() + " failed", e);
                commonCheck = false;
            }
            String memoryFreeStr = info.get(MEMORY_FREE);
            if (Strings.isNullOrEmpty(memoryFreeStr)) {
                // TODO: websocket
                commonCheck = false;
            } else {
                Integer memoryFree = Integer.valueOf(memoryFreeStr);
                if (memoryFree <= MIN_MEMORY_FREE) {
                    // TODO: websocket
                    commonCheck = false;
                }
            }
        }

        List<RedisNode> redisNodeList = installationParam.getRedisNodeList();
        for (RedisNode redisNode : redisNodeList) {
            String ip = redisNode.getHost();
            int port = redisNode.getPort();
            // 如果端口能通，则认为该端口被占用
            if (NetworkUtil.telnet(ip, port)) {
                // TODO: websocket
                commonCheck = false;
            }
        }
        return commonCheck && checkEnvironment(installationParam, machineList);
    }

}
