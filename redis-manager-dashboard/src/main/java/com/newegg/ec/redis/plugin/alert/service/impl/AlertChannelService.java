package com.newegg.ec.redis.plugin.alert.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.plugin.alert.dao.IAlertChannelDao;
import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import com.newegg.ec.redis.plugin.alert.service.IAlertChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 8/31/2019
 */
@Service
public class AlertChannelService implements IAlertChannelService {

    private static final Logger logger = LoggerFactory.getLogger(AlertChannelService.class);

    @Autowired
    private IAlertChannelDao alertChannelDao;

    @Override
    public List<AlertChannel> getAlertChannelByGroupId(Integer groupId) {
        try {
            return alertChannelDao.selectAlertChannelByGroupId(groupId);
        } catch (Exception e) {
            logger.error("Get alert channel list by group id failed, group =" + groupId, e);
            return null;
        }
    }

    @Override
    public List<AlertChannel> getAlertChannelByIds(List<Integer> channelIdList) {
        if (channelIdList == null || channelIdList.isEmpty()) {
            return null;
        }
        try {
            return alertChannelDao.selectAlertChannelByIds(channelIdList);
        } catch (Exception e) {
            logger.error("Get alert channel by ids failed, channel ids = " + channelIdList, e);
            return null;
        }
    }

    @Override
    public boolean addAlertChannel(AlertChannel alertChannel) {
        try {
            int row = alertChannelDao.insertAlertChannel(alertChannel);
            return row == 1;
        } catch (Exception e) {
            logger.error("Add alert channel failed, " + alertChannel, e);
            return false;
        }
    }

    @Override
    public boolean updateAlertChannel(AlertChannel alertChannel) {
        try {
            int row = alertChannelDao.updateAlertChannel(alertChannel);
            return row == 1;
        } catch (Exception e) {
            logger.error("Update alert channel failed, " + alertChannel, e);
            return false;
        }
    }

    @Override
    public boolean deleteAlertChannelById(Integer channelId) {
        try {
            int row = alertChannelDao.deleteAlertChannelById(channelId);
            return row == 1;
        } catch (Exception e) {
            logger.error("Delete alert channel by id failed, channel id = " + channelId, e);
            return false;
        }
    }

    @Override
    public boolean deleteAlertChannelByGroupId(Integer groupId) {
        try {
            int row = alertChannelDao.deleteAlertChannelById(groupId);
            return row > 0;
        } catch (Exception e) {
            logger.error("Delete alert channel by group id failed, group id = " + groupId, e);
            return false;
        }
    }
}
