package com.newegg.ec.redis.plugin.install.entity;

/**
 * @author Jay.H.Zou
 * @date 7/25/2019
 */
public class RedisNode {

    private String redisNodeId;

    private String clusterId;

    private String host;

    private int port;

    private String image;

    private String directory;

    private NodeRole nodeRole;

    private String containerName;

    private InstallationEnvironment installationEnvironment;

    public void setNodeRole(NodeRole nodeRole) {
        this.nodeRole = nodeRole;
    }

    public enum  NodeRole {

        /**
         * redis master
         */
        MASTER,

        /**
         * slave
         */
        SLAVE,

        /**
         * replicate: for redis 5
         */
        REPLICATE;

    }
}
