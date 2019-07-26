package com.newegg.ec.redis.plugin.install;

import com.newegg.ec.redis.entity.Machine;

import java.util.List;

/**
 * 获取机器集群
 * 检查机器资源、端口情况
 * 获取安装包
 * 规划 Redis 集群拓扑图
 * 安装
 * 写入DB
 * 设置配置
 *
 *
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
public interface InstallOperation {

    List<Machine> getMachineList();

    boolean checkResources();

    boolean pullImage();

    boolean buildTopology();

    boolean install();

    boolean saveToDB();

    boolean changeRedisConf();

}
