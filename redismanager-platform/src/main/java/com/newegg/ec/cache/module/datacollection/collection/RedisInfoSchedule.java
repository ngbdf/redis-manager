package com.newegg.ec.cache.module.datacollection.collection;

import com.newegg.ec.cache.core.entity.annotation.mysql.MysqlField;
import com.newegg.ec.cache.core.entity.constants.Constants;
import com.newegg.ec.cache.core.entity.model.Cluster;
import com.newegg.ec.cache.core.entity.model.Host;
import com.newegg.ec.cache.core.entity.model.NodeInfo;
import com.newegg.ec.cache.core.entity.redis.RedisConnectParam;
import com.newegg.ec.cache.dao.IClusterDao;
import com.newegg.ec.cache.dao.INodeInfoDao;
import com.newegg.ec.cache.util.DateUtil;
import com.newegg.ec.cache.util.NetUtil;
import com.newegg.ec.cache.util.redis.RedisDataFormat;
import com.newegg.ec.cache.util.redis.RedisUtils;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lzz on 2018/4/20.
 * 定时收集所有接入平台redis 集群info信息日志 ：默认启动5min以后开始收集，每隔1min收集一次
 */
@Component
@PropertySource("classpath:config/schedule.properties")
public class RedisInfoSchedule {

    private static final int JEDIS_TIMEOUT = 1000;
    private static final Log logger = LogFactory.getLog(RedisInfoSchedule.class);
    private static ExecutorService threadPool = Executors.newFixedThreadPool(200);

    @Resource
    private IClusterDao clusterDao;
    @Resource
    private INodeInfoDao infoDao;


    /**
     * 5分钟后开始采集，以后一分钟采集一次
     */
    @Scheduled(fixedRateString = "${schedule.redisinfo.collection}", initialDelay = 1000 * 300)
    public void scheduledMetricRedisInfo() {
        List<Cluster> clusterList = clusterDao.getClusterList(null);
        for (Cluster cluster : clusterList) {
            try {
                String address = cluster.getAddress();
                int clusterId = cluster.getId();
                Host host = NetUtil.getHostPassAddress(address);
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        infoCollection(clusterId, host.getIp(), host.getPort());
                    }
                });
            } catch (Exception e) {

            }
        }
    }

    /**
     * node info日志收集逻辑
     * @param clusterId
     * @param ip
     * @param port
     */
    public void infoCollection(int clusterId, String ip, int port) {

        Cluster cluster = clusterDao.getCluster(clusterId);
        // 获取集群的所有节点
        List<Map<String, String>> nodeList = RedisUtils.getAllNodes(new RedisConnectParam(ip, port, cluster.getRedisPassword()));
        try {
            long pingTime = NetUtil.pingTime(ip.trim());
            for (Map<String, String> node : nodeList) {
                RedisClient client = null;
                try {
                    String nodeIp = node.get("ip");
                    int nodePort = RedisDataFormat.getPort(node.get("port"));
                    StopWatch stopWatch = new StopWatch();
                    stopWatch.start();

                    if(StringUtils.isNotBlank(cluster.getRedisPassword())){
                        String password = cluster.getRedisPassword();
                        client = RedisClient.create(RedisURI.Builder.redis(nodeIp, nodePort).withPassword(password).build());
                    }else {
                        client = RedisClient.create(RedisURI.Builder.redis(nodeIp, nodePort).build());
                    }
                    String strInfo = client.connect().sync().info();
                    stopWatch.stop();
                    long responTime = stopWatch.getTotalTimeMillis();
                    NodeInfo info = getNodeMonitorInfo(strInfo);
                    info.setResponseTime(redisPingTime(pingTime, responTime));
                    NodeInfo resInfo = changeNodeInfoTime(info);
                    resInfo.setHost(nodeIp + ":" + nodePort);
                    resInfo.setIp(nodeIp);
                    resInfo.setPort(nodePort);

                    infoDao.addNodeInfo(Constants.NODE_INFO_TABLE_FORMAT + clusterId, resInfo);
                } catch (Exception e) {
                    logger.error(node + " Get Momitor Data Error ,May RedisCluster IsNot Ready，Please Wait .");
                } finally {
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    /**
     * 获取node监控信息详情
     * @param strInfo
     * @return
     */
    public NodeInfo getNodeMonitorInfo(String strInfo) {
        NodeInfo info = null;
        try {
            info = new NodeInfo();
            Field[] fields = info.getClass().getDeclaredFields();
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(strInfo.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
            String line;
            while ((line = br.readLine()) != null) {
                String[] arr = line.split(":");
                if (arr.length == 2) {
                    if (arr[0].contains("db")) {
                        continue;
                    }
                    for (Field f : fields) {
                        String fieldName = f.getName();
                        f.setAccessible(true);
                        MysqlField mysqlField = f.getAnnotation(MysqlField.class);
                        if (mysqlField != null) {
                            fieldName = mysqlField.field();
                        }
                        if (arr[0].equals(fieldName)) {
                            String type = f.getGenericType().toString();
                            if (type.equals("class java.lang.String")) {
                                f.set(info, String.valueOf(arr[1]));
                            } else if ("int".equals(type)) {
                                f.set(info, Integer.valueOf(arr[1]));
                            } else if ("long".equals(type)) {
                                f.set(info, Long.valueOf(arr[1]));
                            } else if ("float".equals(type)) {
                                f.set(info, Float.valueOf(arr[1]));
                            }
                        }
                    }
                }
            }
            processDb(strInfo, info);
        } catch (Exception e) {
            logger.error("GetNodeMonitorInfo Error", e);
        }
        return info;
    }

    /**
     * 计算redis ping 耗时
     * @param pingTime
     * @param responTime
     * @return
     */
    private long redisPingTime(long pingTime, long responTime) {
        pingTime = 2 * pingTime;
        long redisTime = 0;
        if (responTime > pingTime) {
            redisTime = responTime - pingTime;
        } else {
            redisTime = 1;
        }
        // redis 调用往返
        redisTime = redisTime / 2;
        if (redisTime == 0) {
            redisTime = 1;
        }
        return redisTime;
    }

    /**
     * 组装nodeinfo数据
     * @param strInfo
     * @param info
     */
    private void processDb(String strInfo, NodeInfo info) {
        List<Map<String, String>> resDb = RedisDataFormat.dbInfo(strInfo);
        long keys = 0;
        long avg_ttl = 0;
        long expires = 0;
        int size = resDb.size();
        for (int i = 0; i < size; i++) {
            Map<String, String> item = resDb.get(i);
            long tempKeys = Long.parseLong(item.get("keys"));
            long tempAvgTTl = Long.parseLong(item.get("avg_ttl"));
            long tempExpires = Long.parseLong(item.get("expires"));
            keys += tempKeys;
            avg_ttl += tempAvgTTl;
            expires += tempExpires;
        }
        if (size > 0) {
            avg_ttl = avg_ttl / size;
            expires = expires / size;
        }
        info.setTotalKeys(keys);
        info.setExpires(expires);
        info.setAvgTtl(avg_ttl);
    }

    /**
     * 格式化info日志时间
     * @param info
     * @return
     */
    public NodeInfo changeNodeInfoTime(NodeInfo info) {
        info.setAddTime(DateUtil.getTime());
        info.setDay(DateUtil.getDay());
        info.setHour(DateUtil.getHour());
        info.setMinute(DateUtil.getMinute());
        return info;
    }

}
