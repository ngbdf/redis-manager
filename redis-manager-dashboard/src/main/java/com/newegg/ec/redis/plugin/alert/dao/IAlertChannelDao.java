package com.newegg.ec.redis.plugin.alert.dao;

import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/29
 */
public interface IAlertChannelDao {

    List<AlertChannel> selectAlertChannelByGroupId(Integer groupId);

    List<AlertChannel> selectAlertChannelByIds(List<Integer> channelIdList);

    int insertAlertChannel(AlertChannel alertChannel);

    int updateAlertChannel(AlertChannel alertChannel);

    int deleteAlertChannelById(Integer channelId);

    int deleteAlertChannelByGroupId(Integer groupId);

}
