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

    private int channelId;

    private int groupId;

    /**
     * 通道名
     */
    private String channelName;

    /**
     * email
     */
    private String emailFrom;

    private String emailTo;

    private String host;

    private String port;

    private String username;

    private String password;

    private Boolean smtpAuth;

    private Boolean smtpStartTtlEnable;

    private Boolean smtpStartTlsRequired;

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

    /**
     * 0: email
     * 1: wechat web hook
     * 2: wechat app
     * 3: dingding web hook
     *
     */
    private int channelType;

}
