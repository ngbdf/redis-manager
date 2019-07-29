package com.newegg.ec.redis.plugin.alert.entity;


/**
 * 告警消息
 *
 * @author Jay.H.Zou
 * @date 2019/7/29
 */
public class AlertMessage {

    private String messageType;

    private String title;

    private String content;

    private String text;

    /**
     * default: true
     */
    private boolean isAtAll;

    private String mobiles;
}
