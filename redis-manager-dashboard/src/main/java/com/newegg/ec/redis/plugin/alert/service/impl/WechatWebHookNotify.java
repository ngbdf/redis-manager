package com.newegg.ec.redis.plugin.alert.service.impl;

import com.alibaba.fastjson.JSONArray;
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
 * "msgtype": "markdown",
 * "markdown": {
 * "content": "# used_memory \n **Rule**: used_memory > 15gb \n\n **Data**: used_memory = 16gb \n\n **Info**: 内存使用超过 15G",
 * "mentioned_list":["wangqing","@all"],
 * "mentioned_mobile_list":["13800001111","@all"]
 * }
 * }
 *
 * @author Jay.H.Zou
 * @date 7/30/2019
 */
public class WechatWebHookNotify extends AbstractAlertNotify {

    private static final Logger logger = LoggerFactory.getLogger(WechatWebHookNotify.class);

    private static final String CONTENT = "content";

    private static final String MENTIONED_MOBILE_LIST = "mentioned_mobile_list";

    private static final String AT_ALL = "@all";

    @Override
    public void notify(Collection<AlertChannel> alertChannelList, List<AlertRecord> alertRecordList) {
        JSONObject requestBody = buildRequestBody(alertRecordList);
        alertChannelList.forEach(alertChannel -> {
            String webhook = alertChannel.getWebhook();
            try {
                HttpClientUtil.post(webhook, requestBody);
            } catch (IOException e) {
                logger.error("Wechat webhook notify failed, " + alertChannel, e);
            }
        });
        // 真正发送消息
    }

    @Override
    protected JSONObject buildRequestBody(List<AlertRecord> alertRecordList) {
        JSONObject requestBody = new JSONObject();
        requestBody.put(MSG_TYPE, MARKDOWN);
        JSONObject markdown = new JSONObject();
        String content = buildMessage(alertRecordList);
        markdown.put(CONTENT, content);
        JSONArray mobileArray = new JSONArray();
        mobileArray.add(AT_ALL);
        markdown.put(MENTIONED_MOBILE_LIST, mobileArray);
        return requestBody;
    }

}
