package com.newegg.ec.cache.backend.checker.strategy.impl;

import com.newegg.ec.cache.app.model.Cluster;
import com.newegg.ec.cache.backend.checker.strategy.AbstractNotifyStrategy;
import org.springframework.stereotype.Component;

/**
 * Created by lf52 on 2018/12/1.
 *
 * 默认告警策略 ： 邮件(通用，基于javamail实现，配置邮件服务器相关即可)
 */
@Component
public class MailNotify extends AbstractNotifyStrategy {


    @Override
    protected boolean checkNotify() {
        return false;
    }

    @Override
    protected void alarmToUser(Cluster cluster, String formula) {

    }

}
