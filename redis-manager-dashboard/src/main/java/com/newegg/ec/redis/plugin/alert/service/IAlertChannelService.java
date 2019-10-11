package com.newegg.ec.redis.plugin.alert.service;

import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 8/31/2019
 */
public interface IAlertChannelService {

    List<AlertChannel> getAlertChannelByGroupId(Integer groupId);

    AlertChannel getAlertChannelById(Integer channelId);

    List<AlertChannel> getAlertChannelByIds(List<Integer> channelIdList);

    List<AlertChannel> getAlertChannelNotUsed(Integer groupId, List<Integer> channelIdList);

    boolean addAlertChannel(AlertChannel alertChannel);

    boolean updateAlertChannel(AlertChannel alertChannel);

    boolean deleteAlertChannelById(Integer channelId);

    boolean deleteAlertChannelByGroupId(Integer groupId);

}
