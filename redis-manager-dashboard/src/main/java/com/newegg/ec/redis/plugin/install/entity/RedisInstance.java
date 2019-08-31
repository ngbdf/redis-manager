package com.newegg.ec.redis.plugin.install.entity;

import com.newegg.ec.redis.entity.RedisNode;

/**
 * @author Jay.H.Zou
 * @date 2019/8/2
 */
public class RedisInstance extends RedisNode {

    private String directory;

    private String containerName;

    private InstallationEnvironment installationEnvironment;

}
