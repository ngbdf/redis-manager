package com.newegg.ec.redis.plugin.alert.entity;

/**
 * 告警通道: group 级别
 * Email
 * Wechat Company
 * DingDing
 *
 * @author Jay.H.Zou
 * @date 2019/7/29
 */
public class AlertChannel {

    private String channelId;

    private String groupId;

    /**
     * 通道类型
     */
    private String channelTyppe;

    /**
     * 通道名
     */
    private String channelName;

    /**
     * email
     */
    private String emailFrom;

    private String emailTo;

    private String smtpHost;

    private String smtpUsername;

    private String smtpPassword;

    /**
     * Wechat & DingDing web hook
     */
    private String webhook;

    /**
     * 微信企业应用相关
     */
    private String corpId;

    private String agentId;

    private String corpSecret;


}
