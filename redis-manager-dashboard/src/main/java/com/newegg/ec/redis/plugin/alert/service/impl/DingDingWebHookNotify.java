package com.newegg.ec.redis.plugin.alert.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;
import com.newegg.ec.redis.util.httpclient.HttpClientUtil;
import com.newegg.ec.redis.plugin.alert.service.AbstractAlertNotify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * {
 *      "msgtype": "markdown",
 *      "markdown": {
 *          "title": "used_memory",
 *          "text": "# used_memory \n **Rule**: used_memory > 15gb \n\n **Data**: used_memory = 16gb \n\n **Info**: 内存使用超过 15G"
 *      },
 *      "at": {
 *          "atMobiles": [],
 *          "isAtAll": true
 *      }
 * }
 * @author Jay.H.Zou
 * @date 7/30/2019
 */
public class DingDingWebHookNotify extends AbstractAlertNotify {

    private static final Logger logger = LoggerFactory.getLogger(DingDingWebHookNotify.class);

    private static final String TITLE = "title";

    private static final String TEXT = "text";

    private static final String AT = "at";

    private static final String IS_AT_ALL = "isAtAll";

    @Override
    public void notify(Collection<AlertChannel> alertChannelList, List<AlertRecord> alertRecordList) {
        JSONObject requestBody = buildRequestBody(alertRecordList);
        alertChannelList.forEach(alertChannel -> {
            String webhook = alertChannel.getWebhook();
            try {
                HttpClientUtil.post(webhook, requestBody);
            } catch (IOException e) {
                logger.error("DingDing notify failed, " + alertChannel, e);
            }
        });

    }

    @Override
    protected JSONObject buildRequestBody(List<AlertRecord> alertRecordList) {
        JSONObject requestBody = new JSONObject();
        requestBody.put(MSG_TYPE, MARKDOWN);
        JSONObject markdown = new JSONObject();
        String text = buildMessage(alertRecordList);
        markdown.put(TEXT, text);
        JSONObject at = new JSONObject();
        at.put(IS_AT_ALL, true);
        requestBody.put(MARKDOWN, markdown);
        requestBody.put(AT, at);
        return requestBody;
    }

}
