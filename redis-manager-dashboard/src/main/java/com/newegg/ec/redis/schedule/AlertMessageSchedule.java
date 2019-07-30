package com.newegg.ec.redis.schedule;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.NodeInfo;
import com.newegg.ec.redis.plugin.alert.dao.IAlertChannelDao;
import com.newegg.ec.redis.plugin.alert.entity.AlertMessage;
import com.newegg.ec.redis.plugin.alert.entity.AlertRule;
import com.newegg.ec.redis.plugin.alert.service.INotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 7/30/2019
 */
public class AlertMessageSchedule implements IDataCalculate {

    @Autowired
    private IAlertChannelDao alertChannelDao;

    @Autowired
    private INotifyService wechatAlertNotify;


    /**
     * 定时获取 node info 进行计算，默认5分钟一次
     */
    @Scheduled(cron = "")
    @Override
    public void calculate() {

    }

    /**
     * 基于集群级别的告警
     */
    public class AlertMessageSende implements Runnable {

        private Cluster cluster;

        private List<AlertRule> alertRuleList;

        public AlertMessageSende(Cluster cluster, List<AlertRule> alertRuleList) {
            this.cluster = cluster;
            this.alertRuleList = alertRuleList;
        }

        /**
         * 1.获取NodeInfoList
         * 2.匹配规则(检查间间隔)
         * 3.写入 DB
         * 4.发送消息
         */
        @Override
        public void run() {
        }
    }
}
