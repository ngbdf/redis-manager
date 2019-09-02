package com.newegg.ec.redis.plugin.alert.service;

import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;

import java.util.Collection;
import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public interface INotifyService {

    /**
     * 通知
     * @param alertChannelList 此列表是全部的 channel, 需要过滤
     * @param alertRecordList
     */
    void notify(Collection<AlertChannel> alertChannelList, List<AlertRecord> alertRecordList);

}
