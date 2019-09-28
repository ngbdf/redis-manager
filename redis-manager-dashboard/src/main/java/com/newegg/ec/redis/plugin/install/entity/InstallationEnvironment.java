package com.newegg.ec.redis.plugin.install.entity;

/**
 * @author Jay.H.Zou
 * @date 2019/7/28
 */
public class InstallationEnvironment {

    private InstallationEnvironment() {
    }

    /**
     * docker
     */
    public static final int DOCKER = 0;
    /**
     * machine
     */
    public static final int MACHINE = 1;

    /**
     * kubernetes
     */
    public static final int KUBERNETES = 2;
}
