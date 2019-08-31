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
    private boolean atAll;

    private String mobiles;

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isAtAll() {
        return atAll;
    }

    public void setAtAll(boolean atAll) {
        this.atAll = atAll;
    }

    public String getMobiles() {
        return mobiles;
    }

    public void setMobiles(String mobiles) {
        this.mobiles = mobiles;
    }
}
