package com.newegg.ec.redis.plugin.alert.service;

import com.newegg.ec.redis.plugin.alert.entity.AlertMessage;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;

import java.util.List;


/**
 * @author Jay.H.Zou
 * @date 7/30/2019
 */
public abstract class AbstractAlertNotify implements INotifyService {

    protected abstract AlertMessage buildMessageContent(List<AlertRecord> alertRecordList);

}
