package com.newegg.ec.redis.plugin.alert.service;

import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 8/31/2019
 */
public interface IAlertRecordService {

    List<AlertRecord> getAlertRecordByClusterId(String clusterId);

    boolean addAlertRecord(AlertRecord alertRecord);

    boolean deleteAlertRecordByIds(List<String> recordIdList);

    boolean deleteAlertRecordByTime(Timestamp earliestTime);
}
