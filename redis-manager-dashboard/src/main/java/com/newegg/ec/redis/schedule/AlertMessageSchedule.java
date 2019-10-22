package com.newegg.ec.redis.schedule;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.newegg.ec.redis.entity.*;
import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;
import com.newegg.ec.redis.plugin.alert.entity.AlertRule;
import com.newegg.ec.redis.plugin.alert.service.IAlertChannelService;
import com.newegg.ec.redis.plugin.alert.service.IAlertRecordService;
import com.newegg.ec.redis.plugin.alert.service.IAlertRuleService;
import com.newegg.ec.redis.plugin.alert.service.IAlertService;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.IGroupService;
import com.newegg.ec.redis.service.INodeInfoService;
import com.newegg.ec.redis.util.SignUtil;
import com.newegg.ec.redis.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.newegg.ec.redis.util.SignUtil.EQUAL_SIGN;
import static javax.management.timer.Timer.ONE_MINUTE;

/**
 * @author Jay.H.Zou
 * @date 7/30/2019
 */
@Component
public class AlertMessageSchedule implements IDataCollection, IDataCleanup, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AlertMessageSchedule.class);

    /**
     * 0: email
     * 1: wechat web hook
     * 2: dingding web hook
     * 3: wechat app
     */
    private static final int EMAIL = 0;

    private static final int WECHAT_WEB_HOOK = 1;

    private static final int DINGDING_WEB_HOOK = 2;

    private static final int WECHAT_APP = 3;

    @Autowired
    private IGroupService groupService;

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private IAlertRuleService alertRuleService;

    @Autowired
    private IAlertChannelService alertChannelService;

    @Autowired
    private IAlertRecordService alertRecordService;

    @Autowired
    private INodeInfoService nodeInfoService;

    @Autowired
    private IAlertService emailAlert;

    @Autowired
    private IAlertService wechatWebHookAlert;

    @Autowired
    private IAlertService dingDingWebHookAlert;

    @Autowired
    private IAlertService wechatAppAlert;

    @Value("${redis-manager.alert.data-keep-days:15}")
    private int dataKeepDays;

    private static ExecutorService threadPool;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        threadPool = new ThreadPoolExecutor(2, 5, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("redis-notify-pool-thread-%d").build(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 定时获取 node info 进行计算，默认5分钟一次
     */
    @Async
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void collect() {
        try {
            List<Group> allGroup = groupService.getAllGroup();
            if (allGroup != null && !allGroup.isEmpty()) {
                allGroup.forEach(group -> {
                    threadPool.submit(new AlertTask(group));
                });
            }
        } catch (Exception e) {
            logger.error("Alert scheduled failed.");
        }
    }

    /**
     * 每周星期天凌晨1点实行一次
     */
//    @Async
//    @Scheduled(cron = "0 0 1 ? * L")
    @Override
    public void cleanup() {
        try {
            Timestamp earliestTime = TimeUtil.getTime(dataKeepDays * TimeUtil.ONE_DAY);
            alertRecordService.deleteAlertRecordByTime(earliestTime);
        } catch (Exception e) {
            logger.error("Cleanup alert data failed.", e);
        }
    }

    private class AlertTask implements Runnable {

        private Group group;

        public AlertTask(Group group) {
            this.group = group;
        }

        @Override
        public void run() {
            try {
                int groupId = group.getGroupId();
                // 获取有效的规则
                List<AlertRule> validAlertRuleList = getValidAlertRule(groupId);
                if (validAlertRuleList == null || validAlertRuleList.isEmpty()) {
                    return;
                }
                // 更新规则
                updateRuleLastCheckTime(validAlertRuleList);
                // 获取 AlertChannel
                List<AlertChannel> validAlertChannel = alertChannelService.getAlertChannelByGroupId(groupId);
                if (validAlertChannel == null || validAlertChannel.isEmpty()) {
                    return;
                }
                // 获取 cluster
                List<Cluster> clusterList = clusterService.getClusterListByGroupId(groupId);
                if (clusterList == null || clusterList.isEmpty()) {
                    return;
                }
                clusterList.forEach(cluster -> {
                    List<Integer> ruleIdList = getRuleIdList(cluster.getRuleIds());
                    if (ruleIdList == null || ruleIdList.isEmpty()) {
                        return;
                    }
                    // 获取集群规则
                    List<AlertRule> alertRuleList = getAlertRuleByIds(validAlertRuleList, ruleIdList);
                    int clusterId = cluster.getClusterId();
                    // 获取 node info 列表
                    NodeInfoParam nodeInfoParam = new NodeInfoParam(clusterId, DataType.NODE, TimeType.MINUTE, null);
                    List<NodeInfo> lastTimeNodeInfoList = nodeInfoService.getLastTimeNodeInfoList(nodeInfoParam);
                    // 构建告警记录
                    List<AlertRecord> alertRecordList = new ArrayList<>();
                    alertRuleList.forEach(alertRule -> lastTimeNodeInfoList.forEach(nodeInfo -> {
                        if (isNotify(nodeInfo, alertRule)) {
                            alertRecordList.add(buildAlertRecord(group, cluster, nodeInfo, alertRule));
                            alertRule.setLastCheckTime(TimeUtil.getCurrentTimestamp());
                        }
                    }));
                    // 获取告警通道并发送消息
                    List<Integer> alertChannelIdList = getAlertChannelIdList(cluster.getChannelIds());
                    Multimap<Integer, AlertChannel> channelMultimap = getAlertChannelByIds(validAlertChannel, alertChannelIdList);
                    if (!channelMultimap.isEmpty() && !alertRecordList.isEmpty()) {
                        distribution(channelMultimap, alertRecordList);
                    }
                    saveRecordToDB(cluster.getClusterName(), alertRecordList);
                });
            } catch (Exception e) {
                logger.error("Alert task failed, " + group, e);
            }
        }
    }

    /**
     * 获取 group 下没有冻结(valid==true)且满足时间周期(lastCheckTime - nowTime >= cycleTime)的规则
     *
     * @param groupId
     * @return
     */
    private List<AlertRule> getValidAlertRule(int groupId) {
        List<AlertRule> validAlertRuleList = alertRuleService.getAlertRuleByGroupId(groupId);
        if (validAlertRuleList == null || validAlertRuleList.isEmpty()) {
            return null;
        }
        validAlertRuleList.removeIf(alertRule -> !isValid(alertRule));
        return validAlertRuleList;
    }

    private List<AlertRule> getAlertRuleByIds(List<AlertRule> validAlertRuleList, List<Integer> ruleIdList) {
        List<AlertRule> alertRuleList = new ArrayList<>();
        validAlertRuleList.forEach(alertRule -> {
            if (ruleIdList.contains(alertRule.getRuleId())) {
                alertRuleList.add(alertRule);
            }
        });
        return alertRuleList;
    }

    private List<Integer> getRuleIdList(String ruleIds) {
        return idsToIntegerList(ruleIds);
    }

    private List<Integer> getAlertChannelIdList(String channelIds) {
        return idsToIntegerList(channelIds);
    }

    private Multimap<Integer, AlertChannel> getAlertChannelByIds(List<AlertChannel> validAlertChannelList, List<Integer> channelIdList) {
        List<AlertChannel> alertChannelList = new ArrayList<>();
        validAlertChannelList.forEach(alertChannel -> {
            if (channelIdList.contains(alertChannel.getChannelId())) {
                alertChannelList.add(alertChannel);
            }
        });
        return classifyChannel(alertChannelList);
    }

    private Multimap<Integer, AlertChannel> classifyChannel(List<AlertChannel> alertChannelList) {
        Multimap<Integer, AlertChannel> channelMultimap = ArrayListMultimap.create();
        alertChannelList.forEach(alertChannel -> {
            channelMultimap.put(alertChannel.getChannelType(), alertChannel);
        });
        return channelMultimap;
    }

    /**
     * "1,2,3,4" => [1, 2, 3, 4]
     *
     * @param ids
     * @return
     */
    private List<Integer> idsToIntegerList(String ids) {
        if (Strings.isNullOrEmpty(ids)) {
            return null;
        }
        List<Integer> idList = new ArrayList<>();
        String[] idArr = SignUtil.splitByCommas(ids);
        for (String id : idArr) {
            idList.add(Integer.parseInt(id));
        }
        return idList;
    }

    /**
     * 校验是否需要告警
     *
     * @param nodeInfo
     * @param alertRule
     * @return
     */
    private boolean isNotify(NodeInfo nodeInfo, AlertRule alertRule) {
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(nodeInfo));
        String alertKey = alertRule.getRuleKey();
        double alertValue = alertRule.getRuleValue();
        String nodeInfoField = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, alertKey);
        Double actualVal = jsonObject.getDouble(nodeInfoField);
        if (actualVal == null) {
            return false;
        }
        int compareType = alertRule.getCompareType();
        return compare(alertValue, actualVal, compareType);
    }

    /**
     * 检验规则是否可用
     *
     * @param alertRule
     * @return
     */
    private boolean isValid(AlertRule alertRule) {
        // 规则状态是否有效
        if (!alertRule.getValid()) {
            return false;
        }
        return true;
        // 检测时间
        /*long duration = System.currentTimeMillis() - alertRule.getLastCheckTime().getTime();
        System.err.println(duration / 1000 / 60);
        return duration >= alertRule.getCheckCycle() * ONE_MINUTE;*/
    }

    /**
     * 比较类型
     * 0: 相等
     * 1: 大于
     * -1: 小于
     * 2: 不等于
     */
    private boolean compare(double alertValue, double actualValue, int compareType) {
        BigDecimal alertValueBigDecimal = BigDecimal.valueOf(alertValue);
        BigDecimal actualValueBigDecimal = BigDecimal.valueOf(actualValue);
        switch (compareType) {
            case 0:
                return alertValueBigDecimal.equals(actualValueBigDecimal);
            case 1:
                return actualValueBigDecimal.compareTo(alertValueBigDecimal) > 0;
            case -1:
                return actualValueBigDecimal.compareTo(alertValueBigDecimal) < 0;
            case 2:
                return !alertValueBigDecimal.equals(actualValueBigDecimal);
            default:
                return false;
        }

    }

    private AlertRecord buildAlertRecord(Group group, Cluster cluster, NodeInfo nodeInfo, AlertRule rule) {
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(nodeInfo));
        AlertRecord record = new AlertRecord();
        String alertKey = rule.getRuleKey();
        String nodeInfoField = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, alertKey);
        Double actualVal = jsonObject.getDouble(nodeInfoField);
        record.setGroupId(group.getGroupId());
        record.setGroupName(group.getGroupName());
        record.setClusterId(cluster.getClusterId());
        record.setClusterName(cluster.getClusterName());
        record.setRuleId(rule.getRuleId());
        record.setRedisNode(nodeInfo.getNode());
        record.setAlertRule(rule.getRuleKey() + getCompareSign(rule.getCompareType()) + rule.getRuleValue());
        record.setActualData(rule.getRuleKey() + EQUAL_SIGN + actualVal);
        record.setCheckCycle(rule.getCheckCycle());
        record.setRuleInfo(rule.getRuleInfo());
        return record;
    }

    /**
     * 0: =
     * 1: >
     * -1: <
     * 2: !=
     *
     * @param compareType
     * @return
     */
    private String getCompareSign(Integer compareType) {
        switch (compareType) {
            case 0:
                return "=";
            case 1:
                return ">";
            case -1:
                return "<";
            case 2:
                return "!=";
            default:
                return "/";
        }
    }

    private void distribution(Multimap<Integer, AlertChannel> channelMultimap, List<AlertRecord> alertRecordList) {
        alert(emailAlert, channelMultimap.get(EMAIL), alertRecordList);
        alert(wechatWebHookAlert, channelMultimap.get(WECHAT_WEB_HOOK), alertRecordList);
        alert(dingDingWebHookAlert, channelMultimap.get(DINGDING_WEB_HOOK), alertRecordList);
        alert(wechatAppAlert, channelMultimap.get(WECHAT_APP), alertRecordList);
    }

    private void alert(IAlertService alertService, Collection<AlertChannel> alertChannelCollection, List<AlertRecord> alertRecordList) {
        alertChannelCollection.forEach(alertChannel -> {
            alertService.alert(alertChannel, alertRecordList);
        });
    }

    private void saveRecordToDB(String clusterName, List<AlertRecord> alertRecordList) {
        if (alertRecordList.isEmpty()) {
            return;
        }
        try {
            alertRecordService.addAlertRecord(alertRecordList);
        } catch (Exception e) {
            logger.error("Save alert to db failed, cluster name = " + clusterName, e);
        }
    }

    private void updateRuleLastCheckTime(List<AlertRule> validAlertRuleList) {
        try {
            List<Integer> ruleIdList = new ArrayList<>();
            validAlertRuleList.forEach(alertRule -> ruleIdList.add(alertRule.getRuleId()));
            alertRuleService.updateAlertRuleLastCheckTime(ruleIdList);
        } catch (Exception e) {
            logger.error("Update alert rule last check time, " + validAlertRuleList, e);
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
