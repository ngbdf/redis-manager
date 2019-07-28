package com.newegg.ec.redis.schedule;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.INodeInfoService;
import com.newegg.ec.redis.service.IRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public class NodeInfoCollection implements IDataCollection, IDataCalculate, ApplicationListener<ContextRefreshedEvent> {

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
    @Scheduled(cron = "0 */1 * * * ?")
    @Override
    public void collectData() {
        try {

        } catch (Exception e) {

        }
    }

    /**
     * 一个小时跑一次，获取DB数据，计算所有节点以 HOUR 为单位的 avg, max, min
     */
    @Scheduled(cron = "0 0 * * *  ?")
    @Override
    public void calculate() {

    }
}
