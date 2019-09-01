package com.newegg.ec.redis.plugin.alert.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;
import com.newegg.ec.redis.plugin.alert.service.AbstractAlertNotify;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 7/30/2019
 */
public class WechatWebHookNotify extends AbstractAlertNotify {

    private static final String CONTENT = "content";

    private static final String MENTIONED_MOBILE_LIST = "mentioned_mobile_list";

    @Override
    public void notify(List<AlertChannel> alertChannelList, List<AlertRecord> alertRecordList) {
        JSONObject requestBody = buildRequestBody(alertRecordList);

        // 真正发送消息
    }

    @Override
    protected JSONObject buildRequestBody(List<AlertRecord> alertRecordList) {
        JSONObject message = new JSONObject();

        return null;
    }

    /**
     * {
     *      "msgtype": "markdown",
     *      "markdown": {
     *      "content": "# used_memory \n **Rule**: used_memory > 15gb \n\n **Data**: used_memory = 16gb \n\n **Info**: 内存使用超过 15G",
     *      "mentioned_list":["wangqing","@all"],
     *      "mentioned_mobile_list":["13800001111","@all"]
     *      }
     * }
     *
     * @param alertRecord
     * @return
     */

}
