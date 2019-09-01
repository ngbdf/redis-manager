package com.newegg.ec.redis.plugin.alert.service;

import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public interface INotifyService {

    /**
     * 通知
     * @param alertChannelList 此列表是根据不同的通知方式分类后的
     * @param alertRecordList
     */
    void notify(List<AlertChannel> alertChannelList, List<AlertRecord> alertRecordList);

}
