package com.newegg.ec.cache.backend.checker.strategy.impl;

import com.newegg.ec.cache.app.dao.IClusterCheckLogDao;
import com.newegg.ec.cache.app.dao.IClusterCheckRuleDao;
import com.newegg.ec.cache.app.model.Cluster;
import com.newegg.ec.cache.app.util.httpclient.HttpClientUtil;
import com.newegg.ec.cache.backend.checker.strategy.AbstractNotifyStrategy;
import com.newegg.ec.cache.core.logger.CommonLogger;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * Created by lf52 on 2018/12/1.
 *
 * 微信告警策略 （选用，需要配置企业号信息，定制化代码）
 */
@Component
public class WechatNotify extends AbstractNotifyStrategy {

    private static final CommonLogger logger = new CommonLogger(WechatNotify.class);

    @Value("${spring.wechat.alarm.url}")
    private String wechatUrl;

    @Value("${spring.wechat.alarm.roleId}")
    private String roleId;

    @Resource
    private IClusterCheckRuleDao checkRuleDao;

    @Resource
    private IClusterCheckLogDao checkLogDao;

    @Override
    protected boolean checkNotify() {
        return StringUtils.isNotEmpty(wechatUrl);
    }

    @Override
    protected void alarmToUser(Cluster cluster, String formula) {
        //发微信
        JSONObject params = new JSONObject();
        params.put("metric", "1");
        params.put("metricValue", "2");
        params.put("roleId", roleId);
        params.put("clientId", cluster.getClusterName());
        params.put("roleName", cluster.getClusterName() + ":" + formula);
        params.put("errorMessage", "Hello All, " + cluster.getClusterName() + ":" + formula + " Redis Cluster Alarm Log In Last 10 Mins,Please Check !");
        try {
            String response = HttpClientUtil.getPostResponse(wechatUrl, params);
            logger.info("wechat response: " + response);
        } catch (IOException e) {
            logger.error("Send Alarm Info To WeChat Error ", e);
        }
    }

}
