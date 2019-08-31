package com.newegg.ec.redis.plugin.alert.dao;

import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/29
 */
public interface IAlertChannelDao {

    List<AlertChannel> selectAlertChannelByGroupId(String groupId);

    List<AlertChannel> selectAlertChannelByIds(List<String> channelIdList);

    int insertAlertChannel(AlertChannel alertChannel);

    int updateAlertChannel(AlertChannel alertChannel);

    int deleteAlertChannelById(String channelId);

    int deleteAlertChannelByGroupId(String groupId);

}
