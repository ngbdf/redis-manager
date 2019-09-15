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

    private String userName;

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

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getSmtpAuth() {
        return smtpAuth;
    }

    public void setSmtpAuth(Boolean smtpAuth) {
        this.smtpAuth = smtpAuth;
    }

    public Boolean getSmtpStartTtlEnable() {
        return smtpStartTtlEnable;
    }

    public void setSmtpStartTtlEnable(Boolean smtpStartTtlEnable) {
        this.smtpStartTtlEnable = smtpStartTtlEnable;
    }

    public Boolean getSmtpStartTlsRequired() {
        return smtpStartTlsRequired;
    }

    public void setSmtpStartTlsRequired(Boolean smtpStartTlsRequired) {
        this.smtpStartTlsRequired = smtpStartTlsRequired;
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

    public int getChannelType() {
        return channelType;
    }

    public void setChannelType(int channelType) {
        this.channelType = channelType;
    }
}
