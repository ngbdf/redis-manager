package com.newegg.ec.redis.schedule;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.Group;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.INodeInfoService;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import redis.clients.jedis.HostAndPort;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public class NodeInfoCollection implements IDataCollection, IDataCalculate, IDataCleanup, ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private IRedisService redisService;

    @Autowired
    private INodeInfoService nodeInfoService;

    private int coreSize;

    private static ExecutorService threadPool;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        coreSize = Runtime.getRuntime().availableProcessors();
        threadPool = new ThreadPoolExecutor(coreSize, coreSize * 2, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("collect-node-info-pool-thread-%d").build(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 一分钟收集一次RedisNode数据，并计算以 MINUTE 为单位的 avg, max, min
     */
    @Async
    @Scheduled(cron = "0 */1 * * * ?")
    @Override
    public void collectData() {
        try {
            List<Cluster> allClusterList = clusterService.getAllClusterList();
            if (allClusterList == null || allClusterList.isEmpty()) {
                return;
            }
            for (Cluster cluster : allClusterList) {

            }
        } catch (Exception e) {

        }
    }

    private class CollectNodeInfoTask implements Runnable {

        private Cluster cluster;

        public CollectNodeInfoTask(Cluster cluster) {
            this.cluster = cluster;
        }

        @Override
        public void run() {
            String nodes = cluster.getNodes();
            String redisPassword = cluster.getRedisPassword();
            Set<HostAndPort> hostAndPortSet = RedisUtil.nodesToSet(nodes);
            List<RedisNode> redisNodeList = redisService.getNodeList(hostAndPortSet, redisPassword);
            for (RedisNode redisNode : redisNodeList) {

            }
        }
    }

    /**
     * 一个小时跑一次，获取DB数据，计算所有节点以 HOUR 为单位的 avg, max, min
     */
    @Async
    @Scheduled(cron = "0 0 * * *  ?")
    @Override
    public void calculate() {

    }

    /**
     * 每周跑一次，清理数据
     */
    @Async
    @Scheduled(cron = "0 0 * * *  ?")
    @Override
    public void cleanup() {

    }
}
