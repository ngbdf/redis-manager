package com.newegg.ec.redis.plugin.alert.service;

import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 8/31/2019
 */
public interface IAlertChannelService {

    List<AlertChannel> getAlertChannelByGroupId(String groupId);

    List<AlertChannel> getAlertChannelByIds(List<String> channelIdList);

    boolean addAlertChannel(AlertChannel alertChannel);

    boolean updateAlertChannel(AlertChannel alertChannel);

    boolean deleteAlertChannelById(String channelId);

    boolean deleteAlertChannelByGroupId(String groupId);

}
