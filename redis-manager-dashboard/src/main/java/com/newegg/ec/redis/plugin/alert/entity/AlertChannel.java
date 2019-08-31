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

    /**
     * 0: email
     * 1: wechat web hook
     * 2: wechat app
     * 3: dingding web hook
     *
     */
    private int type;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public void setSmtpUsername(String smtpUsername) {
        this.smtpUsername = smtpUsername;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public String getCorpId() {
        return corpId;
    }

    public void setCorpId(String corpId) {
        this.corpId = corpId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getCorpSecret() {
        return corpSecret;
    }

    public void setCorpSecret(String corpSecret) {
        this.corpSecret = corpSecret;
    }

}
