package com.newegg.ec.redis.entity;

/**
 * @author fw13
 * @date 2019/11/19 18:56
 * 操作类型  例如 创建、修改、删除
 */
public enum OperationType {

    /**
     * create
     */
    CREATE("create"),

    INSTALL("install"),

    DELETE("delete"),

    UPDATE("update"),

    ADD("add"),

    /**
     * 附加  例如channel rule
     */

    ATTACH("attach"),

    /**
     * 分离  例如channel rule
     */
    DETACH("detach"),

    IMPORT("import"),

    INIT_SLOTS("init slots"),

    FORGET("forget"),

    REPLICATE_OF("replicate of"),

    MOVE_SLOT("moveSlot"),

    FAIL_OVER("fail over"),

    PURGE_MEMORY("purge memory"),

    START("start"),

    STOP("stop"),

    RESTART("restart"),

    GRANT("grant"),

    REVOKE("revoke");

    private String type;

    OperationType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
