package com.newegg.ec.redis.plugin.alert.dao;

import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;

import java.sql.Timestamp;
import java.util.List;

/**
 *
 * @author Jay.H.Zou
 * @date 2019/7/29
 */
public interface IAlertRecordDao {

    List<AlertRecord> selectAlertRecordByClusterId(Integer clusterId);

    int insertAlertRecord(List<AlertRecord> alertRecordList);

    int deleteAlertRecordByIds(List<Integer> recordIdList);

    int deleteAlertRecordByTime(Timestamp earliestTime);
}
