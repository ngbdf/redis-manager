package com.newegg.ec.redis.schedule;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.INodeInfoService;
import com.newegg.ec.redis.service.IRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.*;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public class NodeInfoCollection implements IDataCollection, ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private IRedisService redisService;

    @Autowired
    private INodeInfoService nodeInfoService;

    @Value("${redis-manager.monitor.core-size:}")
    private int coreSize = Runtime.getRuntime().availableProcessors();

    private static ExecutorService threadPool;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        threadPool = new ThreadPoolExecutor(coreSize, coreSize * 2, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("collect-node-info-pool-thread-%d").build(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    @Scheduled(cron = "")
    @Override
    public void collectData() {
        try {

        } catch (Exception e) {

        }
    }


}
