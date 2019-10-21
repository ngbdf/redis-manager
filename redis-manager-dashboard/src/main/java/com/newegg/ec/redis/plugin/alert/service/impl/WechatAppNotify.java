package com.newegg.ec.redis.plugin.alert.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;
import com.newegg.ec.redis.plugin.alert.service.AbstractAlertNotify;
import com.newegg.ec.redis.util.httpclient.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * {
 *    "touser" : "zouhuajian",
 *    "msgtype": "markdown",
 *    "agentid" : 1000003,
 *    "markdown": {
 *         "content": "您的会议室已经预定，稍后会同步到`邮箱`
 *                 >**事项详情**
 *                 >事　项：<font color=\"info\">开会</font>
 *                 >组织者：@miglioguan
 *                 >参与者：@miglioguan、@kunliu、@jamdeezhou、@kanexiong、@kisonwang
 *                 >
 *                 >会议室：<font color=\"info\">广州TIT 1楼 301</font>
 *                 >日　期：<font color=\"warning\">2018年5月18日</font>
 *                 >时　间：<font color=\"comment\">上午9:00-11:00</font>
 *                 >
 *                 >请准时参加会议。
 *                 >
 *                 >如需修改会议信息，请点击：[修改会议信息](https://work.weixin.qq.com)"
 *    }
 * }
 *
 * @author Jay.H.Zou
 * @date 2019/10/21
 */
public class WechatAppNotify extends AbstractAlertNotify {

    private static final Logger logger = LoggerFactory.getLogger(WechatAppNotify.class);

    private static final Cache<Integer, String> ACCESS_TOKEN_CACHE = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.HOURS).build();

    private static final String ACCESS_TOKEN = "access_token";

    private static final String ERRCODE = "errcode";

    private static final String ERRMSG = "errmsg";

    @Override
    protected JSONObject buildRequestBody(List<AlertRecord> alertRecordList) {

        return null;
    }

    @Override
    public void notify(Collection<AlertChannel> alertChannelList, List<AlertRecord> alertRecordList) {
        alertChannelList.forEach(alertChannel -> {
            Integer channelId = alertChannel.getChannelId();
            try {
                String accessToken = ACCESS_TOKEN_CACHE.get(channelId, new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        String accessToken = getAccessToken(alertChannel.getCorpId(), alertChannel.getCorpSecret());
                        ACCESS_TOKEN_CACHE.put(channelId, accessToken);
                        return accessToken;
                    }
                });

            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    private String getAccessToken(String corpId, String secret) throws IOException {
        String urlForAccessToken = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + corpId + "&corpsecret=" + secret;
        String response = HttpClientUtil.get(urlForAccessToken);
        if (StringUtils.isBlank(response)) {
            return null;
        }
        JSONObject responseObj = null;
        try {
            responseObj = JSONObject.parseObject(response);
        } catch (Exception e) {
            logger.error("http get wechat access token error.", e);
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
        int errorCode = response.getInteger(ERRCODE);
        return errorCode == 0;
    }
}
