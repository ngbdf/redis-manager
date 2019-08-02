package com.newegg.ec.redis.plugin.install.service;

import com.newegg.ec.redis.entity.RedisNode;

import java.util.List;

/**
 *
 *
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
public interface InstallationOperation {

    boolean checkEnvironment();

    boolean pullImage();

    /**
     * 1.从DB中获取相应版本的配置
     * 2.根据不同安装方式更改相应的配置
     * 3.根据安装的集群模式更改配置
     * @return
     */
    boolean buildConfig();

    boolean install(List<RedisNode> redisNodeList);

}
