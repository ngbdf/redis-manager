package com.newegg.ec.redis.plugin.alert.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;
import com.newegg.ec.redis.plugin.alert.service.AbstractAlertNotify;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * @author Jay.H.Zou
 * @date 9/3/2019
 */
public class EmailNotify extends AbstractAlertNotify {



    @Override
    protected JSONObject buildRequestBody(List<AlertRecord> alertRecordList) {
        return null;
    }

    @Override
    public void notify(Collection<AlertChannel> alertChannelList, List<AlertRecord> alertRecordList) {

    }

    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername("my.gmail@gmail.com");
        mailSender.setPassword("password");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        return mailSender;
    }

}
