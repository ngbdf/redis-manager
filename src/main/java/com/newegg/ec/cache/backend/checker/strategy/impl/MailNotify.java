package com.newegg.ec.cache.backend.checker.strategy.impl;

import com.newegg.ec.cache.app.model.Cluster;
import com.newegg.ec.cache.backend.checker.strategy.AbstractNotifyStrategy;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SMTPAppender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by lf52 on 2018/12/1.
 *
 * 默认告警策略 ： 邮件(通用，基于log4j实现)
 */
@Component
public class MailNotify extends AbstractNotifyStrategy {

    private static final Logger logger = Logger.getLogger(MailNotify.class);

    private SMTPAppender appender = new SMTPAppender();

    @Value("${spring.mail.alarm}")
    private boolean mailAlarm;

    @Value("${spring.mail.SMTPHost}")
    private String SMTPHost;

    @Value("${spring.mail.mailFrom}")
    private String mailFrom;

    @Value("${spring.mail.mailTo}")
    private String mailTo;

    @Value("${spring.mail.SMTPUsername}")
    private String SMTPUsername;

    @Value("${spring.mail.SMTPPassword}")
    private String SMTPPassword;


    @Override
    protected boolean checkNotify() {
        return mailAlarm;
    }

    @Override
    protected void alarmToUser(Cluster cluster, String formula) {
        try {
            appender.setSMTPHost(SMTPHost);
            appender.setSMTPUsername(SMTPUsername);
            appender.setSMTPPassword(SMTPPassword);

            appender.setFrom(mailFrom);
            appender.setTo(mailTo);
            appender.setLocationInfo(true);
            appender.setSubject("RedisAlarmNotify");
            appender.setLayout(new HTMLLayout());
            appender.activateOptions();
            logger.addAppender(appender);
            logger.error("Hello All, " + cluster.getClusterName() + ":" + formula + " Redis Cluster Alarm Log In Last 10 Mins,Please Check !");

        } catch (Exception e) {
            //ingore
        }

    }

}
