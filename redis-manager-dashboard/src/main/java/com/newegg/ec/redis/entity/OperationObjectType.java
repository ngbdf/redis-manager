package com.newegg.ec.redis.entity;

/**
 * @author fw13
 * @date 2019/11/20 11:14
 * 操作对象。例如 机器、规则、节点
 */
public enum OperationObjectType {

    ALERT_RULE("alert rule"),

    ALERT_CHANNEL("alert channel"),

    ALERT_RECORD("alert record"),

    MACHINE("machine"),

    USER("user"),

    GROUP("group"),

    NODE("node"),

    CLUSTER("cluster"),

    REDIS_CONFIG("redis config");

    private String objType;

    OperationObjectType(String objType) {
        this.objType = objType;
    }

    public String getObjType() {
        return objType;
    }

    public void setObjType(String objType) {
        this.objType = objType;
    }
}
