package com.newegg.ec.cache.module.clusteralarm.notify.impl;

import com.newegg.ec.cache.core.entity.model.Cluster;
import com.newegg.ec.cache.core.logger.CommonLogger;
import com.newegg.ec.cache.module.clusteralarm.notify.AbstractNotifyStrategy;
import org.springframework.stereotype.Component;

/**
 * Created by lf52 on 2018/12/1.
 *
 * 微信告警策略 （选用，需要配置企业号信息，定制化代码）
 */
@Component
public class WechatNotify extends AbstractNotifyStrategy {

    private static final CommonLogger logger = new CommonLogger(WechatNotify.class);

    @Override
    protected boolean checkNotify() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected void alarmToUser(Cluster cluster, String formula) {
        // TODO Auto-generated method stub
    }

}
