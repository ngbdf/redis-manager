package com.newegg.ec.redis.entity;

import java.util.Objects;

/**
 * @author Jay.H.Zou
 * @date 2019/8/2
 */
public enum NodeRole {

    /**
     * redis master
     */
    MASTER("master"),

    /**
     * slave
     */
    SLAVE("slave"),

    /**
     * replicate: for redis 5
     */
    REPLICA("replica"),

    UNKNOWN("unknown");

    private String value;

    NodeRole(){}

    NodeRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static NodeRole value(String value) {
        for (NodeRole nodeRole : values()) {
            if (Objects.equals(nodeRole.getValue(), value)) {
                return nodeRole;
            }
        }
        return UNKNOWN;
    }

}
