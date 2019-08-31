package com.newegg.ec.redis.plugin.alert.service.impl;

import com.newegg.ec.redis.plugin.alert.entity.AlertMessage;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;
import com.newegg.ec.redis.plugin.alert.service.AbstractAlertNotify;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 7/30/2019
 */
public class WechatAlertNotify extends AbstractAlertNotify {

    @Override
    public boolean notify(List<AlertRecord> alertRecordList) {
        AlertMessage alertMessage = buildMessageContent(alertRecordList);
        // 真正发送消息
        return false;
    }

    @Override
    protected AlertMessage buildMessageContent(List<AlertRecord> alertRecordList) {
        return null;
    }
}
