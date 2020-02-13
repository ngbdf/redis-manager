package com.newegg.ec.redis.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.client.IRedisClient;
import com.newegg.ec.redis.dao.ISentinelMastersDao;
import com.newegg.ec.redis.entity.SentinelMaster;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.service.ISentinelMastersService;
import com.newegg.ec.redis.util.SignUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Jay.H.Zou
 * @date 1/22/2020
 */
@Service
public class SentinelMastersService implements ISentinelMastersService {

    private static final Logger logger = LoggerFactory.getLogger(SentinelMastersService.class);

    @Autowired
    private ISentinelMastersDao sentinelMastersDao;

    @Autowired
    private IRedisService redisService;

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private IRedisClient redisClient;

    @Override
    public List<SentinelMaster> getSentinelMasterByClusterId(Integer clusterId) {
        try {
            List<SentinelMaster> realSentinelMasterList = redisService.getSentinelMasters(clusterService.getClusterById(clusterId));
            List<SentinelMaster> dbSentinelMasterList = sentinelMastersDao.selectSentinelMasterByClusterId(clusterId);
            return mergeRedisNode(realSentinelMasterList, dbSentinelMasterList);
        } catch (Exception e) {
            logger.error("Get sentinel masters by cluster id failed.", e);
            return null;
        }
    }

    private List<SentinelMaster> mergeRedisNode(List<SentinelMaster> realSentinelMasterList, List<SentinelMaster> dbSentinelMasterList) {
        List<SentinelMaster> sentinelMasterList = new LinkedList<>();
        Iterator<SentinelMaster> realIterator = realSentinelMasterList.iterator();
        while (realIterator.hasNext()) {
            SentinelMaster realSentinelMaster = realIterator.next();
            realSentinelMaster.setMonitor(true);
            Iterator<SentinelMaster> dbIterator = dbSentinelMasterList.iterator();
            while (dbIterator.hasNext()) {
                SentinelMaster dbSentinelMaster = dbIterator.next();
                if (Objects.equals(realSentinelMaster.getMasterName(), dbSentinelMaster.getMasterName())) {
                    boolean masterNodeChanged = !Objects.equals(dbSentinelMaster.getLastMasterNode(), dbSentinelMaster.getMasterNode());
                    realSentinelMaster.setMasterChanged(masterNodeChanged);
                    realSentinelMaster.setLastMasterNode(dbSentinelMaster.getLastMasterNode());
                    realIterator.remove();
                    dbIterator.remove();
                }
            }
            sentinelMasterList.add(realSentinelMaster);
        }
        if (dbSentinelMasterList.isEmpty()) {
            return sentinelMasterList;
        }
        dbSentinelMasterList.forEach(dbSentinelMaster -> {
            dbSentinelMaster.setMonitor(false);
            boolean masterNodeChanged = !Objects.equals(dbSentinelMaster.getLastMasterNode(), dbSentinelMaster.getMasterNode());
            dbSentinelMaster.setMasterChanged(masterNodeChanged);
            sentinelMasterList.add(dbSentinelMaster);
        });
        return sentinelMasterList;
    }

    @Override
    public SentinelMaster getSentinelMasterByMasterName(Integer clusterId, String masterName) {
        try {
            if (Strings.isNullOrEmpty(masterName)) {
                logger.warn("master name can't be null.");
                return null;
            }
            return sentinelMastersDao.selectSentinelMasterByMasterName(clusterId, masterName);
        } catch (Exception e) {
            logger.info("Get sentinel master by master name failed.", e);
            return null;
        }
    }

    @Override
    public SentinelMaster getSentinelMasterById(Integer sentinelMasterId) {
        try {
            return sentinelMastersDao.selectSentinelMasterById(sentinelMasterId);
        } catch (Exception e) {
            logger.error("Get sentinel master by id failed.", e);
            return null;
        }
    }

    @Override
    public boolean updateSentinelMaster(SentinelMaster sentinelMaster) {
        try {
            return sentinelMastersDao.updateSentinelMaster(sentinelMaster) > 0;
        } catch (Exception e) {
            logger.error("Update sentinel master failed, " + sentinelMaster, e);
            return false;
        }
    }

    @Override
    public boolean addSentinelMaster(SentinelMaster sentinelMaster) {
        try {
            sentinelMaster.setLastMasterNode(sentinelMaster.getMasterHost() + SignUtil.COLON + sentinelMaster.getMasterPort());
            return sentinelMastersDao.insertSentinelMaster(sentinelMaster) > 0;
        } catch (Exception e) {
            logger.error("Add sentinel master failed. ", e);
            return false;
        }
    }

    @Override
    public boolean deleteSentinelMaster(SentinelMaster sentinelMaster) {
        return false;
    }

    @Override
    public boolean deleteSentinelMasterById(Integer sentinelMasterId) {
        try {
            SentinelMaster sentinelMaster = getSentinelMasterById(sentinelMasterId);
            String masterName = sentinelMaster.getMasterName();
            redisClient.removeMaster(masterName);
            return sentinelMastersDao.deleteSentinelMasterById(sentinelMasterId) > 0;
        } catch (Exception e) {
            logger.error("Remove sentinel master failed.", e);
            return false;
        } finally {
            redisClient.close();
        }
    }

    @Override
    public boolean deleteSentinelMasterByClusterId(Integer clusterId) {
        try {
            return sentinelMastersDao.deleteSentinelMasterByClusterId(clusterId) > 0;
        } catch (Exception e) {
            logger.error("Delete sentinel master failed. ", e);
            return false;
        }
    }
}
