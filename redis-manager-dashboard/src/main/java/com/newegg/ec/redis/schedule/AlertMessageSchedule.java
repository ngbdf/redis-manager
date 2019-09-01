package com.newegg.ec.redis.schedule;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.CaseFormat;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.NodeInfo;
import com.newegg.ec.redis.entity.NodeInfoParam;
import com.newegg.ec.redis.entity.NodeInfoType;
import com.newegg.ec.redis.plugin.alert.dao.IAlertChannelDao;
import com.newegg.ec.redis.plugin.alert.entity.AlertRule;
import com.newegg.ec.redis.plugin.alert.service.IAlertRuleService;
import com.newegg.ec.redis.plugin.alert.service.INotifyService;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.INodeInfoService;
import com.newegg.ec.redis.util.SignUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static javax.management.timer.Timer.ONE_MINUTE;

/**
 * @author Jay.H.Zou
 * @date 7/30/2019
 */
public class AlertMessageSchedule implements IDataCollection, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AlertMessageSchedule.class);

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private IAlertRuleService alertRuleService;

    @Autowired
    private INodeInfoService nodeInfoService;

    @Autowired
    private IAlertChannelDao alertChannelDao;

    @Autowired
    private INotifyService wechatAlertNotify;

    private static ExecutorService threadPool;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        threadPool = new ThreadPoolExecutor(5, 5, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("redis-notify-pool-thread-%d").build(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 定时获取 node info 进行计算，默认5分钟一次
     */
    @Scheduled(cron = "")
    @Override
    public void collect() {
        try {
            List<Cluster> allClusterList = clusterService.getAllClusterList();
            if (allClusterList != null && !allClusterList.isEmpty()) {
                allClusterList.forEach(cluster -> {
                    threadPool.submit(new AlertTask(cluster));
                });
            }
        } catch (Exception e) {
            logger.error("Alert scheduled failed.");
        }
    }

    private class AlertTask implements Runnable {

        private Cluster cluster;

        public AlertTask(Cluster cluster) {
            this.cluster = cluster;
        }

        @Override
        public void run() {
            try {
                int clusterId = cluster.getClusterId();
                String ruleIds = cluster.getRuleIds();
                List<String> ruleIdList = Arrays.asList(SignUtil.splitByCommas(ruleIds));
                List<AlertRule> alertRuleList = alertRuleService.getAlertRuleIds(ruleIdList);
                NodeInfoParam nodeInfoParam = new NodeInfoParam(clusterId, NodeInfoType.DataType.NODE, NodeInfoType.TimeType.MINUTE, null);
                List<NodeInfo> lastTimeNodeInfoList = nodeInfoService.getLastTimeNodeInfoList(nodeInfoParam);

                lastTimeNodeInfoList.forEach(nodeInfo -> {
                    JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(nodeInfo));
                    alertRuleList.forEach(alertRule -> {
                        // 是否生效
                        if (!alertRule.getStatus()) {
                            return;
                        }
                        // 未到检测时间
                        if (System.currentTimeMillis() - alertRule.getLastCheckTime().getTime() < alertRule.getCheckCycle() * ONE_MINUTE) {
                            return;
                        }

                        String alertKey = alertRule.getAlertKey();
                        String nodeInfoField = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, alertKey);
                        double alertValue = alertRule.getAlertValue();
                        Double actualVal = jsonObject.getDouble(nodeInfoField);
                        if (actualVal == null) {
                            return;
                        }

                        int compareType = alertRule.getCompareType();
                        boolean compare = compare(alertValue, actualVal, compareType);
                        if (compare) {

                        }
                    });
                });

            } catch (Exception e) {
                logger.error("Alert task failed, " + cluster, e);
            }
        }
    }

    /**
     * 比较类型
     * 0: 相等
     * 1: 大于等于
     * -1: 小于等于
     * 2: 不等于
     */
    private boolean compare(double alertValue, double actualValue, int compareType) {
        BigDecimal alertValueBigDecimal = BigDecimal.valueOf(actualValue);
        BigDecimal actualValueBigDecimal = BigDecimal.valueOf(actualValue);
        switch (compareType) {
            case 0:
                return alertValueBigDecimal.equals(actualValueBigDecimal);
            case 1:
                return alertValue >= actualValue;
            case -1:
                return alertValue <= actualValue;
            case 2:
                return !alertValueBigDecimal.equals(actualValueBigDecimal);
            default:
                return false;
        }

    }

    /**
     * 基于集群级别的告警
     */
    public class AlertMessageNotify implements Runnable {

        private Cluster cluster;

        private List<AlertRule> alertRuleList;

        public AlertMessageNotify(Cluster cluster, List<AlertRule> alertRuleList) {
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
