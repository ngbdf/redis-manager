package com.newegg.ec.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kyle.K.Zhao
 * @date 1/9/2020 09:35
 */
@Configuration
@ConfigurationProperties("redis-manager.rct")
public class RCTConfig {

    @Value("${redis-manager.rct.elasticsearch.rest.urls:''}")
    private String esUrls;

    @Value("${redis-manager.rct.elasticsearch.slowlog.index:''}")
    private String slowlogIndexName;

    @Value("${redis-manager.rct.elasticsearch.client-list.index:''}")
    private String clientIndex;
    @Value("${redis-manager.rct.elasticsearch.enable:false}")
    private boolean elasticsearchEnable;
    @Value("${redis-manager.rct.rdb.generate.enable:false}")
    private boolean rdbGenerateEnable;
    @Value("${redis-manager.rct.dev.enable:false}")
    private boolean devEnable;

    @Value("${redis-manager.rct.dev.rdb.port:9002}")
    private String devRDBPort;

    private Email email;

    private User user;

    public String getClientIndex() {
        return clientIndex;
    }

    public void setClientIndex(String clientIndex) {
        this.clientIndex = clientIndex;
    }

    public String getEsUrls() {
        return esUrls;
    }

    public void setEsUrls(String esUrls) {
        this.esUrls = esUrls;
    }

    public String getSlowlogIndexName() {
        return slowlogIndexName;
    }

    public void setSlowlogIndexName(String slowlogIndexName) {
        this.slowlogIndexName = slowlogIndexName;
    }


    public boolean isElasticsearchEnable() {
        return elasticsearchEnable;
    }

    public void setElasticsearchEnable(boolean elasticsearchEnable) {
        this.elasticsearchEnable = elasticsearchEnable;
    }

    public boolean isRdbGenerateEnable() {
        return rdbGenerateEnable;
    }

    public void setRdbGenerateEnable(boolean rdbGenerateEnable) {
        this.rdbGenerateEnable = rdbGenerateEnable;
    }



    public boolean isDevEnable() {
        return devEnable;
    }

    public void setDevEnable(boolean devEnable) {
        this.devEnable = devEnable;
    }



    public String getDevRDBPort() {
        return devRDBPort;
    }

    public void setDevRDBPort(String devRDBPort) {
        this.devRDBPort = devRDBPort;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static class Email {
        private String smtp;
        private String fromName;
        private String userName;
        private String password;
        private String subject;

        public String getSmtp() {
            return smtp;
        }

        public void setSmtp(String smtp) {
            this.smtp = smtp;
        }

        public String getFromName() {
            return fromName;
        }

        public void setFromName(String fromName) {
            this.fromName = fromName;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }
    }

    public static class User {
        private String userName;
        private String password;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}
