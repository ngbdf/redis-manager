package com.newegg.ec.redis.plugin.alert.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;
import com.newegg.ec.redis.plugin.alert.service.IAlertService;
import com.newegg.ec.redis.util.httpclient.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.newegg.ec.redis.util.TimeUtil.TIME_FORMATTER;

/**
 * @author Jay.H.Zou
 * @date 2019/10/21
 */
@Service("wechatAppAlert")
public class WechatAppAlert implements IAlertService {

    private static final Logger logger = LoggerFactory.getLogger(WechatAppAlert.class);

    private static final Cache<Integer, String> ACCESS_TOKEN_CACHE = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.HOURS).build();

    private static final String URL_TEMPLATE = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=%s";

    private static final String ACCESS_TOKEN = "access_token";

    private static final String ERRCODE = "errcode";

    private static final String ERRMSG = "errmsg";

    private static final String TOUSER = "touser";

    private static final String AGENTID = "agentid";

    private static final String CONTENT = "content";

    private static final String NEW_LINE = "\n";

    @Override
    public void alert(AlertChannel alertChannel, List<AlertRecord> alertRecordList) {
        Integer channelId = alertChannel.getChannelId();
        try {
            String accessToken = ACCESS_TOKEN_CACHE.get(channelId, () -> {
                String token = getAccessToken(alertChannel.getCorpId(), alertChannel.getCorpSecret());
                if (!Strings.isNullOrEmpty(token)) {
                    ACCESS_TOKEN_CACHE.put(channelId, token);
                }
                return token;
            });
            if (Strings.isNullOrEmpty(accessToken)) {
                return;
            }
            JSONObject requestBody = buildRequestBody(alertChannel, alertRecordList);
            String url = String.format(URL_TEMPLATE, accessToken);
            HttpClientUtil.post(url, requestBody);
        } catch (Exception e) {
            logger.error("Wechat app notify failed, " + alertChannel, e);
        }
    }

    private JSONObject buildRequestBody(AlertChannel alertChannel, List<AlertRecord> alertRecordList) {
        JSONObject requestBody = new JSONObject();
        requestBody.put(TOUSER, AT_ALL);
        requestBody.put(AGENTID, alertChannel.getAgentId());
        requestBody.put(MSG_TYPE, TEXT);
        JSONObject text = new JSONObject();
        String content = buildMessage(alertRecordList);
        text.put(CONTENT, content);
        requestBody.put(TEXT, text);
        return requestBody;
    }

    private String buildMessage(List<AlertRecord> alertRecordList) {
        AlertRecord firstRecord = alertRecordList.get(0);
        StringBuffer message = new StringBuffer();
        message.append("Group Name: ").append(firstRecord.getGroupName()).append(NEW_LINE)
                .append("Cluster Name: ").append(firstRecord.getClusterName()).append(NEW_LINE);
        alertRecordList.forEach(alertRecord -> {
            message.append("Redis Node: ").append(alertRecord.getRedisNode()).append(NEW_LINE)
                    .append("Alert Rule: ").append(alertRecord.getAlertRule()).append(NEW_LINE)
                    .append("Actual Value: ").append(alertRecord.getActualData()).append(NEW_LINE);
            String ruleInfo = alertRecord.getRuleInfo();
            if (!Strings.isNullOrEmpty(ruleInfo)) {
                message.append("Rule Info: ").append(alertRecord.getRuleInfo()).append(NEW_LINE);
            }
            message.append(NEW_LINE);
        });
        message.append("Time: ").append(LocalDateTime.now().format(TIME_FORMATTER));
        return message.toString();
    }

    private String getAccessToken(String corpId, String secret) throws IOException {
        String urlForAccessToken = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + corpId + "&corpsecret=" + secret;
        JSONObject responseObj;
        try {
            String response = HttpClientUtil.get(urlForAccessToken);
            responseObj = JSONObject.parseObject(response);
        } catch (Exception e) {
            logger.error("Get access token failed.", e);
            return null;
        }
        if (checkAccessToken(responseObj)) {
            return responseObj.getString(ACCESS_TOKEN);
        } else {
            String errorMsg = responseObj.getString(ERRMSG);
            logger.error("http get wechat access token error, serect: " + secret + ", errorMsg: " + errorMsg);
            return null;
        }
    }

    private boolean checkAccessToken(JSONObject response) {
        if (response == null) {
            return false;
        }
        int errorCode = response.getInteger(ERRCODE);
        return errorCode == 0;
    }
}
