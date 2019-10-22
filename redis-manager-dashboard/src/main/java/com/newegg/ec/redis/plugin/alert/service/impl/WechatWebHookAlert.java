package com.newegg.ec.redis.plugin.alert.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;
import com.newegg.ec.redis.util.httpclient.HttpClientUtil;
import com.newegg.ec.redis.plugin.alert.service.AbstractAlertAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author Jay.H.Zou
 * @date 7/30/2019
 */
@Service("wechatWebHookAlert")
public class WechatWebHookAlert extends AbstractAlertAlert {

    private static final Logger logger = LoggerFactory.getLogger(WechatWebHookAlert.class);

    private static final String CONTENT = "content";

    private static final String MENTIONED_MOBILE_LIST = "mentioned_mobile_list";

    @Override
    public void alert(AlertChannel alertChannel, List<AlertRecord> alertRecordList) {
        JSONObject requestBody = buildRequestBody(alertRecordList);
            String webhook = alertChannel.getWebhook();
            try {
                HttpClientUtil.post(webhook, requestBody);
            } catch (IOException e) {
                logger.error("Wechat webhook notify failed, " + alertChannel, e);
            }
    }

    /**
     * {
     * "msgtype": "markdown",
     * "markdown": {
     * "content": "# used_memory \n **Rule**: used_memory > 15gb \n\n **Data**: used_memory = 16gb \n\n **Info**: 内存使用超过 15G",
     * "mentioned_list":["wangqing","@all"],
     * "mentioned_mobile_list":["13800001111","@all"]
     * }
     * }
     * @param alertRecordList
     * @return
     */
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
        requestBody.put(MARKDOWN, markdown);
        return requestBody;
    }

}
