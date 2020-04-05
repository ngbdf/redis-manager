package com.newegg.ec.redis.plugin.alert.service;

import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author Jay.H.Zou
 * @date 8/31/2019
 */
public interface IAlertRecordService {

    Map<String, Object> getAlertRecordByClusterId(Integer clusterId, Integer pageNo, Integer pageSize);

    boolean addAlertRecord(List<AlertRecord> alertRecordList);

    boolean deleteAlertRecordById(Integer recordId);

    boolean deleteAlertRecordByIds(List<Integer> recordIdList);

    boolean cleanAlertRecordByTime();

    Integer getAlertRecordNumber(Integer groupId);
}
