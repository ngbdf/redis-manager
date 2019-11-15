package com.newegg.ec.redis.plugin.alert.service;

import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 8/31/2019
 */
public interface IAlertRecordService {

    List<AlertRecord> getAlertRecordByClusterId(Integer clusterId);

    boolean addAlertRecord(List<AlertRecord> alertRecordList);

    boolean deleteAlertRecordById(Integer recordId);

    boolean deleteAlertRecordByIds(List<Integer> recordIdList);

    boolean deleteAlertRecordByTime(Timestamp earliestTime);

    Integer getAlertRecordNumber(Integer groupId);
}
