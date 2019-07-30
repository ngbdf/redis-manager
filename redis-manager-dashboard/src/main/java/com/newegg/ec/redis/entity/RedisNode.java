package com.newegg.ec.redis.entity;

import redis.clients.jedis.HostAndPort;

/**
 * @author Jay.H.Zou
 * @date 7/25/2019
 */
public class RedisNode {



    private HostAndPort hostAndPort;

    private NodeRole nodeRole;

    public HostAndPort getHostAndPort() {
        return hostAndPort;
    }

    public void setHostAndPort(HostAndPort hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    public NodeRole getNodeRole() {
        return nodeRole;
    }

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
