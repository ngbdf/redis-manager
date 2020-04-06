package com.newegg.ec.redis.plugin.alert.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.newegg.ec.redis.plugin.alert.dao.IAlertRecordDao;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;
import com.newegg.ec.redis.plugin.alert.service.IAlertRecordService;
import com.newegg.ec.redis.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jay.H.Zou
 * @date 2019/8/31
 */
@Service
public class AlertRecordService implements IAlertRecordService {

    private static final Logger logger = LoggerFactory.getLogger(AlertRecordService.class);

    @Value("${redis-manager.alert.data-keep-days:15}")
    private int dataKeepDays;

    @Autowired
    private IAlertRecordDao alertRecordDao;

    @Override
    public Map<String, Object> getAlertRecordByClusterId(Integer clusterId, Integer pageNo, Integer pageSize) {
        try {
            Map<String, Object> returnMap = new HashMap<>();

            Page<AlertRecord> alertRecords = PageHelper.startPage(pageNo, pageSize);
            alertRecordDao.selectAlertRecordByClusterId(clusterId);

            returnMap.put("alertRecords", alertRecords);
            returnMap.put("totalCount", alertRecords.getTotal());
            returnMap.put("totalPage",alertRecords.getPages());
            return returnMap;
        } catch (Exception e) {
            logger.error("Get alert record by cluster id failed, cluster id = " + clusterId, e);
            return null;
        }
    }

    @Override
    public boolean addAlertRecord(List<AlertRecord> alertRecordList) {
        try {
            int row = alertRecordDao.insertAlertRecordBatch(alertRecordList);
            return row > 0;
        } catch (Exception e) {
            logger.error("Add alert record  failed, " + alertRecordList, e);
            return false;
        }
    }

    @Override
    public boolean deleteAlertRecordById(Integer recordId) {
        try {
            alertRecordDao.deleteAlertRecordById(recordId);
            return true;
        } catch (Exception e) {
            logger.error("Delete alert record by ids failed, alert record id = " + recordId, e);
            return false;
        }
    }

    @Override
    public boolean deleteAlertRecordByIds(List<Integer> recordIdList) {
        try {
            alertRecordDao.deleteAlertRecordByIds(recordIdList);
            return true;
        } catch (Exception e) {
            logger.error("Delete alert record by ids failed, alert record ids = " + recordIdList, e);
            return false;
        }
    }

    @Override
    public boolean cleanAlertRecordByTime() {
        try {
            Timestamp earliestTime = TimeUtil.getTime(dataKeepDays * TimeUtil.ONE_DAY);
            alertRecordDao.deleteAlertRecordByTime(earliestTime);
            return true;
        } catch (Exception e) {
            logger.error("Delete alert record by earliest time failed", e);
            return false;
        }
    }

    @Override
    public Integer getAlertRecordNumber(Integer groupId) {
        try {
            return alertRecordDao.countAlertRecordNumber(groupId);
        } catch (Exception e) {
            logger.error("Delete alert record by earliest time failed", e);
            return 0;
        }
    }
}
