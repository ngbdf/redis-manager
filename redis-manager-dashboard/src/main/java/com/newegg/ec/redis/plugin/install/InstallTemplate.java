package com.newegg.ec.redis.plugin.install;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.ClusterType;
import com.newegg.ec.redis.entity.InstallParam;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.service.IMachineService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 集群安装模板
 *
 * @author Jay.H.Zou
 * @date 2019/7/26
 */
public class InstallTemplate {

    @Autowired
    private IMachineService machineService;

    /**
     * 模板通用，调用方传入不同的策略
     *
     * @param installOperation
     * @param installParam
     * @return
     */
    public boolean install(InstallOperation installOperation, InstallParam installParam) {
        Cluster cluster = installParam.getCluster();
        ClusterType clusterType = cluster.getClusterType();
        int startPort = installParam.getStartPort();
        int endPort = installParam.getEndPort();
        String machineGroup = installParam.getMachineGroup();
        List<Machine> machineByMachineGroup = machineService.getMachineByMachineGroup(machineGroup);

        return false;
    }

    private boolean checkEnvironmnet(InstallParam installParam) {
        return true;
    }

    private boolean changeRedisConf() {
        return true;
    }

    private boolean saveToDB() {
        return true;
    }
}
