package com.newegg.ec.redis.schedule;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.TimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
@Component
public class NodeInfoHourCollection extends NodeInfoCollectionAbstract {

    private static final Logger logger = LoggerFactory.getLogger(NodeInfoHourCollection.class);

    private ExecutorService threadPool;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        coreSize = Runtime.getRuntime().availableProcessors() ;
        threadPool = new ThreadPoolExecutor(coreSize, coreSize * 4, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("collect-node-info-pool-thread-%d").build(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 一个小时跑一次，获取DB数据，计算所有节点以 HOUR 为单位的 avg, max, min
     */
    @Async
    @Scheduled(cron = "0 0 0/1 * * ? ")
    @Override
    public void collect() {
        logger.info("Start collecting node info (hour)...");
        try {
            List<Cluster> allClusterList = clusterService.getAllClusterList();
            if (allClusterList == null || allClusterList.isEmpty()) {
                return;
            }
            for (Cluster cluster : allClusterList) {
                threadPool.submit(new CollectNodeInfoTask(cluster, TimeType.HOUR));
            }
        } catch (Exception e) {
            logger.error("Collect node info data failed, time type = " + TimeType.HOUR, e);
        }
    }
}
