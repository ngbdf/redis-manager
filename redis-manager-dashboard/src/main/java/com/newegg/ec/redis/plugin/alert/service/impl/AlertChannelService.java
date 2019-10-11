package com.newegg.ec.redis.plugin.alert.service.impl;

import com.newegg.ec.redis.plugin.alert.dao.IAlertChannelDao;
import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import com.newegg.ec.redis.plugin.alert.service.IAlertChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * For test: https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=94d09bcf-0e04-4933-b83b-fd34c56b0196
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
    public AlertChannel getAlertChannelById(Integer channelId) {
        try {
            return alertChannelDao.selectAlertChannelById(channelId);
        } catch (Exception e) {
            logger.error("Get alert channel by id failed.", e);
            return null;
        }
    }

    @Override
    public List<AlertChannel> getAlertChannelByIds(List<Integer> channelIdList) {
        try {
            return alertChannelDao.selectAlertChannelByIds(channelIdList);
        } catch (Exception e) {
            logger.error("Get alert channel by ids failed.", e);
            return null;
        }
    }

    @Override
    public List<AlertChannel> getAlertChannelNotUsed(Integer groupId, List<Integer> channelIdList) {
        try {
            return alertChannelDao.selectAlertChannelNotUsed(groupId, channelIdList);
        } catch (Exception e) {
            logger.error("Get alert channel by ids failed.", e);
            return null;
        }
    }

    @Override
    public boolean addAlertChannel(AlertChannel alertChannel) {
        try {
            return alertChannelDao.insertAlertChannel(alertChannel) > 0;
        } catch (Exception e) {
            logger.error("Add alert channel failed, " + alertChannel, e);
            return false;
        }
    }

    @Override
    public boolean updateAlertChannel(AlertChannel alertChannel) {
        try {
            return alertChannelDao.updateAlertChannel(alertChannel) > 0;
        } catch (Exception e) {
            logger.error("Update alert channel failed, " + alertChannel, e);
            return false;
        }
    }

    @Override
    public boolean deleteAlertChannelById(Integer channelId) {
        try {
            return alertChannelDao.deleteAlertChannelById(channelId) > 0;
        } catch (Exception e) {
            logger.error("Delete alert channel by id failed, channel id = " + channelId, e);
            return false;
        }
    }

    @Override
    public boolean deleteAlertChannelByGroupId(Integer groupId) {
        try {
            return alertChannelDao.deleteAlertChannelById(groupId) > 0;
        } catch (Exception e) {
            logger.error("Delete alert channel by group id failed, group id = " + groupId, e);
            return false;
        }
    }
}
