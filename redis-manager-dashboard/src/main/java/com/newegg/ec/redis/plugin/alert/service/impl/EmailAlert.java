package com.newegg.ec.redis.plugin.alert.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;
import com.newegg.ec.redis.plugin.alert.service.IAlertService;
import com.newegg.ec.redis.util.SignUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Properties;

import static com.newegg.ec.redis.util.TimeUtil.TIME_FORMATTER;

/**
 * @author Jay.H.Zou
 * @date 9/3/2019
 */
@Service("emailAlert")
public class EmailAlert implements IAlertService {

    private static final Logger logger = LoggerFactory.getLogger(EmailAlert.class);

    @Override
    public void alert(AlertChannel alertChannel, List<AlertRecord> alertRecordList) {
        try {
            AlertRecord alertRecord = alertRecordList.get(0);
            String subject = buildSubject(alertRecord);
            String content = buildHtmlContent(alertRecordList);
            JavaMailSender javaMailSender = getJavaMailSender(alertChannel);
            //使用JavaMail的MimeMessage，支持更加复杂的邮件格式和内容  
            MimeMessage msg = javaMailSender.createMimeMessage();
            //创建MimeMessageHelper对象，处理MimeMessage的辅助类  
            // msg:发送的邮件信息，true:是否为HTML格式的文件，utf-8:设置编码格式(因为发送html格式时、内容可能出现乱码)
            MimeMessageHelper helper;
            helper = new MimeMessageHelper(msg, true, "utf-8");
            //使用辅助类MimeMessage设定参数  
            helper.setFrom(alertChannel.getEmailFrom());
            String emailTos = alertChannel.getEmailTo();
            helper.setTo(SignUtil.splitBySemicolon(emailTos));
            helper.setSubject(subject);
            helper.setText(content, true); //true参数说明该内容格式为HTML
            javaMailSender.send(msg);
        } catch (MessagingException e) {
            logger.error("Send email failed.", e);
        }
    }

    private String buildSubject(AlertRecord alertRecord) {
        return "(Redis Manager Alert) " + alertRecord.getGroupName() + SignUtil.MINUS + alertRecord.getClusterName();
    }

    private String buildHtmlContent(List<AlertRecord> alertRecordList) {
        StringBuilder content = new StringBuilder();
        content.append("<body style='font-family: \"Microsoft YaHei\", sans-serif, \"微软雅黑\", Arial, \"Helvetica Neue\", Helvetica, \"PingFang SC\", \"Hiragino Sans GB\";'>" +
                "<h3>Redis Manager Alert</h3>");
        alertRecordList.forEach(alertRecord -> {
            String item = "<h4>%s</h4>" +
                    "<ul>" +
                    "<li>Alert Rule: %s</li>" +
                    "<li>Actual Value: %s</li>" +
                    "<li>Rule Info: %s</li>" +
                    "<li>Time: %s</li>" +
                    "</ul>";
            String ruleInfo = alertRecord.getRuleInfo();
            content.append(String.format(item,
                    alertRecord.getRedisNode(),
                    alertRecord.getAlertRule(),
                    alertRecord.getActualData(),
                    Strings.isNullOrEmpty(ruleInfo) ? "" : ruleInfo,
                    ZonedDateTime.now().format(TIME_FORMATTER)));
        });
        content.append("</body>");
        return content.toString();
    }

    private JavaMailSender getJavaMailSender(AlertChannel alertChannel) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        String smtpHost = alertChannel.getSmtpHost();
        String[] hostAndPort = smtpHost.split(SignUtil.COLON);
        mailSender.setHost(hostAndPort[0]);
        if (hostAndPort.length > 1) {
            mailSender.setPort(Integer.parseInt(hostAndPort[0]));
        }
        mailSender.setUsername(alertChannel.getEmailUserName());
        mailSender.setPassword(alertChannel.getEmailPassword());
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        return mailSender;
    }

}
