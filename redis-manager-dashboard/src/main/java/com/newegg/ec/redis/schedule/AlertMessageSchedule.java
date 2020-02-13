package com.newegg.ec.redis.schedule;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.newegg.ec.redis.client.RedisClient;
import com.newegg.ec.redis.client.RedisClientFactory;
import com.newegg.ec.redis.entity.*;
import com.newegg.ec.redis.plugin.alert.entity.AlertChannel;
import com.newegg.ec.redis.plugin.alert.entity.AlertRecord;
import com.newegg.ec.redis.plugin.alert.entity.AlertRule;
import com.newegg.ec.redis.plugin.alert.service.IAlertChannelService;
import com.newegg.ec.redis.plugin.alert.service.IAlertRecordService;
import com.newegg.ec.redis.plugin.alert.service.IAlertRuleService;
import com.newegg.ec.redis.plugin.alert.service.IAlertService;
import com.newegg.ec.redis.service.*;
import com.newegg.ec.redis.util.RedisUtil;
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
import redis.clients.jedis.HostAndPort;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.newegg.ec.redis.util.RedisClusterInfoUtil.parseClusterInfoToObject;
import static com.newegg.ec.redis.util.RedisUtil.*;
import static com.newegg.ec.redis.util.SignUtil.EQUAL_SIGN;
import static com.newegg.ec.redis.util.TimeUtil.FIVE_SECONDS;
import static javax.management.timer.Timer.ONE_MINUTE;
import static javax.management.timer.Timer.ONE_SECOND;

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

    private static final int ALERT_RECORD_LIMIT = 16;

    @Autowired
    private IGroupService groupService;

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private IRedisNodeService redisNodeService;

    @Autowired
    private IRedisService redisService;

    @Autowired
    private ISentinelMastersService sentinelMastersService;

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

    private ExecutorService threadPool;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        int coreSize = Runtime.getRuntime().availableProcessors();
        threadPool = new ThreadPoolExecutor(coreSize, coreSize, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("redis-notify-pool-thread-%d").build());
    }

    /**
     * 定时获取 node info 进行计算
     */
    @Async
    @Scheduled(cron = "0 0/1 * * * ? ")
    @Override
    public void collect() {
        try {
            List<Group> allGroup = groupService.getAllGroup();
            if (allGroup != null && !allGroup.isEmpty()) {
                logger.info("Start to check alert rules...");
                allGroup.forEach(group -> {
                    threadPool.submit(new AlertTask(group));
                });
            }
        } catch (Exception e) {
            logger.error("Alert scheduled failed.", e);
        }
    }

    /**
     * 每天凌晨0点实行一次，清理数据
     */
    @Async
    @Scheduled(cron = "0 0 0 * * ?")
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

        AlertTask(Group group) {
            this.group = group;
        }

        @Override
        public void run() {
            try {
                Integer groupId = group.getGroupId();
                // 获取 cluster
                List<Cluster> clusterList = clusterService.getClusterListByGroupId(groupId);
                if (clusterList == null || clusterList.isEmpty()) {
                    return;
                }

                // 获取有效的规则
                List<AlertRule> validAlertRuleList = getValidAlertRule(groupId);
                if (validAlertRuleList == null || validAlertRuleList.isEmpty()) {
                    return;
                }
                // 更新规则
                updateRuleLastCheckTime(validAlertRuleList);
                // 获取 AlertChannel
                List<AlertChannel> validAlertChannel = alertChannelService.getAlertChannelByGroupId(groupId);
                clusterList.forEach(cluster -> {
                    List<Integer> ruleIdList = getRuleIdList(cluster.getRuleIds());
                    // 获取集群规则
                    List<AlertRule> alertRuleList = validAlertRuleList.stream()
                            .filter(alertRule -> alertRule.getGlobal() || ruleIdList.contains(alertRule.getRuleId()))
                            .collect(Collectors.toList());
                    if (alertRuleList.isEmpty()) {
                        return;
                    }
                    List<AlertRecord> alertRecordList = getNodeInfoAlertRecord(group, cluster, alertRuleList);
                    // 获取集群级别的告警
                    List<AlertRecord> clusterAlertRecordList = getClusterAlertRecord(group, cluster, alertRuleList);
                    logger.info("Start to send alert message...");
                    // save to database
                    saveRecordToDB(cluster.getClusterName(), alertRecordList);
                    saveRecordToDB(cluster.getClusterName(), clusterAlertRecordList);
                    // 获取告警通道并发送消息
                    List<Integer> alertChannelIdList = getAlertChannelIdList(cluster.getChannelIds());
                    Multimap<Integer, AlertChannel> channelMultimap = getAlertChannelByIds(validAlertChannel, alertChannelIdList);
                    if (channelMultimap != null && !channelMultimap.isEmpty()) {
                        if (!alertRecordList.isEmpty()) {
                            distribution(channelMultimap, alertRecordList);
                        }
                        if (!clusterAlertRecordList.isEmpty()) {
                            distribution(channelMultimap, clusterAlertRecordList);
                        }
                    }
                });
            } catch (Exception e) {
                logger.error("Alert task failed, " + group, e);
            }
        }
    }

    private List<AlertRecord> getNodeInfoAlertRecord(Group group, Cluster cluster, List<AlertRule> alertRuleList) {
        List<AlertRecord> alertRecordList = new ArrayList<>();
        // 获取 node info 列表
        NodeInfoParam nodeInfoParam = new NodeInfoParam(cluster.getClusterId(), TimeType.MINUTE, null);
        List<NodeInfo> lastTimeNodeInfoList = nodeInfoService.getLastTimeNodeInfoList(nodeInfoParam);
        // 构建告警记录
        for (AlertRule alertRule : alertRuleList) {
            if (alertRule.getClusterAlert()) {
                continue;
            }
            for (NodeInfo nodeInfo : lastTimeNodeInfoList) {
                if (isNotify(nodeInfo, alertRule)) {
                    alertRecordList.add(buildNodeInfoAlertRecord(group, cluster, nodeInfo, alertRule));
                }
            }
        }
        return alertRecordList;
    }

    /**
     * cluster/standalone check
     * <p>
     * 1.connect cluster/standalone failed
     * 2.cluster(mode) cluster_state isn't ok
     * 3.node not in cluster/standalone
     * 4.node shutdown
     *
     * @param group
     * @param cluster
     * @param alertRuleList
     * @return
     */
    private List<AlertRecord> getClusterAlertRecord(Group group, Cluster cluster, List<AlertRule> alertRuleList) {
        List<AlertRecord> alertRecordList = new ArrayList<>();
        String seedNodes = cluster.getNodes();
        for (AlertRule alertRule : alertRuleList) {
            if (!alertRule.getClusterAlert()) {
                continue;
            }
            RedisClient redisClient = null;
            try {
                redisClient = RedisClientFactory.buildRedisClient(RedisUtil.nodesToHostAndPort(seedNodes), cluster.getRedisPassword());
                if (Objects.equals(CLUSTER, cluster.getRedisMode())) {
                    Map<String, String> clusterInfo = redisClient.getClusterInfo();
                    Cluster currentCluster = parseClusterInfoToObject(clusterInfo);
                    Cluster.ClusterState clusterState = currentCluster.getClusterState();
                    if (!Objects.equals(Cluster.ClusterState.HEALTH, currentCluster.getClusterState())) {
                        alertRecordList.add(buildClusterAlertRecord(group, cluster, alertRule, seedNodes, "Cluster state not ok"));
                        cluster.setClusterState(clusterState);
                    }
                }
            } catch (Exception e) {
                logger.error("Connected " + cluster.getClusterName() + " failed.", e);
                alertRecordList.add(buildClusterAlertRecord(group, cluster, alertRule, seedNodes, e.getMessage()));
                cluster.setClusterState(Cluster.ClusterState.BAD);
                clusterService.updateClusterState(cluster);
                return alertRecordList;
            } finally {
                if (redisClient != null) {
                    redisClient.close();
                }
            }
            List<RedisNode> redisNodeList = redisNodeService.getRedisNodeListByClusterId(cluster.getClusterId());
            if (redisNodeList == null || redisNodeList.isEmpty()) {
                alertRecordList.add(buildClusterAlertRecord(group, cluster, alertRule, seedNodes, "Get nodes failed"));
                cluster.setClusterState(Cluster.ClusterState.BAD);
                continue;
            }
            redisNodeList.forEach(redisNode -> {
                String node = RedisUtil.getNodeString(redisNode);
                String reason = !redisNode.getRunStatus() ? node + " is shutdown" : !redisNode.getInCluster() ? node + " not in cluster" : null;
                if (!Strings.isNullOrEmpty(reason)) {
                    alertRecordList.add(buildClusterAlertRecord(group, cluster, alertRule, node, reason));
                    cluster.setClusterState(Cluster.ClusterState.WARN);
                }
            });
            if (Objects.equals(SENTINEL, cluster.getRedisMode())) {
                List<AlertRecord> sentinelMasterRecord = getSentinelMasterRecord(group, cluster, alertRule);
                alertRecordList.addAll(sentinelMasterRecord);
            }
        }
        clusterService.updateClusterState(cluster);
        return alertRecordList;
    }

    /**
     * 1. not monitor
     * 2. master changed
     * 3. status or state not ok
     *
     * @param group
     * @param cluster
     * @param alertRule
     * @return
     */
    private List<AlertRecord> getSentinelMasterRecord(Group group, Cluster cluster, AlertRule alertRule) {
        List<AlertRecord> alertRecordList = new ArrayList<>();
        List<SentinelMaster> sentinelMasterList = sentinelMastersService.getSentinelMasterByClusterId(cluster.getClusterId());
        sentinelMasterList.forEach(sentinelMaster -> {
            String node = sentinelMaster.getMasterHost() + SignUtil.COLON + sentinelMaster.getMasterPort();
            String masterName = sentinelMaster.getMasterName();
            StringBuilder reason = new StringBuilder();
            if (!sentinelMaster.getMonitor()) {
                reason.append(masterName).append(" is not being monitored now.\n");
            }
            if (sentinelMaster.getMasterChanged()) {
                reason.append(masterName).append(" failover, old master: ")
                        .append(sentinelMaster.getLastMasterNode())
                        .append(", new master: ")
                        .append(sentinelMaster.getMasterHost())
                        .append(SignUtil.COLON)
                        .append(sentinelMaster.getMasterPort()).append(".\n");
            }
            String flags = sentinelMaster.getFlags();
            if (!Objects.equals("master", flags)) {
                reason.append(masterName).append(" flags: ").append(flags).append(".\n");
            }
            String status = sentinelMaster.getStatus();
            if (!Objects.equals("ok", status)) {
                reason.append(masterName).append(" status: ").append(status).append(".\n");
            }
            alertRecordList.add(buildClusterAlertRecord(group, cluster, alertRule, node, reason.toString()));
        });
        return alertRecordList;
    }

    /**
     * 获取 group 下没有冻结(valid==true)且满足时间周期(nowTime - lastCheckTime >= cycleTime)的规则
     *
     * @param groupId
     * @return
     */
    private List<AlertRule> getValidAlertRule(Integer groupId) {
        List<AlertRule> validAlertRuleList = alertRuleService.getAlertRuleByGroupId(groupId);
        if (validAlertRuleList == null || validAlertRuleList.isEmpty()) {
            return null;
        }
        validAlertRuleList.removeIf(alertRule -> !isRuleValid(alertRule));
        return validAlertRuleList;
    }

    private List<Integer> getRuleIdList(String ruleIds) {
        return idsToIntegerList(ruleIds);
    }

    private List<Integer> getAlertChannelIdList(String channelIds) {
        return idsToIntegerList(channelIds);
    }

    private Multimap<Integer, AlertChannel> getAlertChannelByIds(List<AlertChannel> validAlertChannelList, List<Integer> channelIdList) {
        List<AlertChannel> alertChannelList = new ArrayList<>();
        if (validAlertChannelList == null || validAlertChannelList.isEmpty()) {
            return null;
        }
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
        List<Integer> idList = new ArrayList<>();
        if (Strings.isNullOrEmpty(ids)) {
            return idList;
        }
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
    private boolean isRuleValid(AlertRule alertRule) {
        // 规则状态是否有效
        if (!alertRule.getValid()) {
            return false;
        }
        // 检测时间
        long lastCheckTime = alertRule.getLastCheckTime().getTime();
        long checkCycle = alertRule.getCheckCycle() * ONE_MINUTE;
        long duration = System.currentTimeMillis() - lastCheckTime;
        return duration >= checkCycle;
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

    private AlertRecord buildNodeInfoAlertRecord(Group group, Cluster cluster, NodeInfo nodeInfo, AlertRule rule) {
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
        record.setClusterAlert(rule.getClusterAlert());
        return record;
    }

    private AlertRecord buildClusterAlertRecord(Group group, Cluster cluster, AlertRule rule, String nodes, String reason) {
        AlertRecord record = new AlertRecord();
        record.setGroupId(group.getGroupId());
        record.setGroupName(group.getGroupName());
        record.setClusterId(cluster.getClusterId());
        record.setClusterName(cluster.getClusterName());
        record.setRuleId(rule.getRuleId());
        record.setRedisNode(nodes);
        record.setAlertRule("Cluster Alert");
        record.setActualData(reason);
        record.setCheckCycle(rule.getCheckCycle());
        record.setRuleInfo(rule.getRuleInfo());
        record.setClusterAlert(rule.getClusterAlert());
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

    /**
     * 给各通道发送消息
     * 由于部分通道对于消息长度、频率等有限制，此处做了分批、微延迟处理
     *
     * @param channelMultimap
     * @param alertRecordList
     */
    private void distribution(Multimap<Integer, AlertChannel> channelMultimap, List<AlertRecord> alertRecordList) {
        alert(emailAlert, channelMultimap.get(EMAIL), alertRecordList);
        List<List<AlertRecord>> partition = Lists.partition(alertRecordList, ALERT_RECORD_LIMIT);
        int size = partition.size();
        long waitSecond = size > 10 ? FIVE_SECONDS : ONE_SECOND;
        partition.forEach(partAlertRecordList -> {
            alert(wechatWebHookAlert, channelMultimap.get(WECHAT_WEB_HOOK), partAlertRecordList);
            alert(dingDingWebHookAlert, channelMultimap.get(DINGDING_WEB_HOOK), partAlertRecordList);
            alert(wechatAppAlert, channelMultimap.get(WECHAT_APP), partAlertRecordList);
            try {
                Thread.sleep(waitSecond);
            } catch (InterruptedException ignored) {
            }
        });
    }

    private void alert(IAlertService alertService, Collection<AlertChannel> alertChannelCollection, List<AlertRecord> alertRecordList) {
        alertChannelCollection.forEach(alertChannel -> alertService.alert(alertChannel, alertRecordList));
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
            List<Integer> ruleIdList = validAlertRuleList.stream().map(AlertRule::getRuleId).collect(Collectors.toList());
            alertRuleService.updateAlertRuleLastCheckTime(ruleIdList);
        } catch (Exception e) {
            logger.error("Update alert rule last check time, " + validAlertRuleList, e);
        }
    }

}
