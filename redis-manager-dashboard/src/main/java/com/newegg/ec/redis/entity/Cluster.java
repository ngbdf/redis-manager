package com.newegg.ec.redis.entity;

import com.newegg.ec.redis.plugin.install.entity.InstallationEnvironment;

/**
 * 描述集群基本情况
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public class Cluster {

    private String clusterId;

    private String groupId;

    private String userIds;

    private String clusterToken;

    private String clusterName;

    private String nodes;

    private String redisMode;

    private String os;

    private String redisVersion;

    /**
     * 集群 key 总数
     */
    private long totalKeys;

    /**
     * 集群 expire 总数
     */
    private long totalExpires;

    private String redisPassword;

    private InstallationEnvironment installationEnv;

    /**
     * 0: install by redis manager
     * 1: other way
     */
    private int installationType;

}
