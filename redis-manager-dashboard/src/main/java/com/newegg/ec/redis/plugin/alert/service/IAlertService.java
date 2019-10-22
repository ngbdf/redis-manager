package com.newegg.ec.redis.plugin.alert.service;

import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;

import java.util.Collection;
import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/19
 */
public interface IAlertService {

    String NEW_LINE = "\n\n";

    String SPLIT_LINE = "--------------------------";

    String MSG_TYPE = "msgtype";

    String MARKDOWN = "markdown";

    String TEXT = "text";

    String AT_ALL = "@all";

    /**
     * 通知
     *
     * @param alertChannel
     * @param alertRecordList
     */
    void alert(AlertChannel alertChannel, List<AlertRecord> alertRecordList);

}
