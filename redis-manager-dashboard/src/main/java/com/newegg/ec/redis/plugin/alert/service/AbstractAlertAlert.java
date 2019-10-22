package com.newegg.ec.redis.plugin.alert.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;

import java.time.LocalDateTime;
import java.util.List;

import static com.newegg.ec.redis.util.TimeUtil.TIME_FORMATTER;


/**
 * @author Jay.H.Zou
 * @date 7/30/2019
 */
public abstract class AbstractAlertAlert implements IAlertService {

    protected abstract JSONObject buildRequestBody(List<AlertRecord> alertRecordList);

    protected String buildMessage(List<AlertRecord> alertRecordList) {
        AlertRecord firstRecord = alertRecordList.get(0);
        StringBuffer message = new StringBuffer();
        message.append("**Group Name:** ").append(firstRecord.getGroupName()).append(NEW_LINE)
                .append("**Cluster Name:** ").append(firstRecord.getClusterName()).append(NEW_LINE).append(NEW_LINE);
        alertRecordList.forEach(alertRecord -> {
            message.append("Redis Node: **").append(alertRecord.getRedisNode()).append("**").append(NEW_LINE)
                    .append("Alert Rule: ").append(alertRecord.getAlertRule()).append(NEW_LINE)
                    .append("Actual Value: ").append(alertRecord.getActualData()).append(NEW_LINE);
            String ruleInfo = alertRecord.getRuleInfo();
            if (!Strings.isNullOrEmpty(ruleInfo)) {
                message.append("Rule Info: ").append(ruleInfo).append(NEW_LINE);
            }
            message.append(SPLIT_LINE).append(NEW_LINE);
        });
        message.append("Time: ").append(LocalDateTime.now().format(TIME_FORMATTER)).append(NEW_LINE).append(NEW_LINE);
        return message.toString();
    }
}
