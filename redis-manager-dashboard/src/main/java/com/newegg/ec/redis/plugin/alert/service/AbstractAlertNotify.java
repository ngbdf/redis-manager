package com.newegg.ec.redis.plugin.alert.service;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import com.newegg.ec.redis.plugin.alert.entity.AlertMessage;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;

import java.time.LocalDateTime;
import java.util.List;

import static com.newegg.ec.redis.util.TimeUtil.TIME_FORMATTER;


/**
 * @author Jay.H.Zou
 * @date 7/30/2019
 */
public abstract class AbstractAlertNotify implements INotifyService {

    protected static final String NEW_LINE = "\\n\\n";

    protected static final String MSG_TYPE = "msgtype";

    protected static final String MARKDOWN = "markdown";

    protected abstract JSONObject buildRequestBody(List<AlertRecord> alertRecordList);

    protected String buildMessage(List<AlertRecord> alertRecordList) {
        AlertRecord firstRecord = alertRecordList.get(0);
        StringBuffer message = new StringBuffer();
        message.append("**Group Name:*v* ").append(firstRecord.getGroupName()).append(NEW_LINE)
                .append("**Cluster Name:** ").append(firstRecord.getClusterName()).append(NEW_LINE);
        alertRecordList.forEach(alertRecord -> {
            message.append("**Redis Node:** ").append(alertRecord.getRedisNode()).append(NEW_LINE)
                    .append("**Alert Rule:** ").append(alertRecord.getAlertRule()).append(NEW_LINE)
                    .append("**ActualValue:** ").append(alertRecord.getActualData()).append(NEW_LINE)
                    .append("**Rule Info:** ").append(alertRecord.getRuleInfo()).append(NEW_LINE).append(NEW_LINE);
        });
        message.append("**Time:** ").append( LocalDateTime.now().format(TIME_FORMATTER));
        return message.toString();
    }
}
