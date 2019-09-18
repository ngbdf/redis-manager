package com.newegg.ec.redis.schedule;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.NodeInfoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public class NodeInfoHourCollection extends NodeInfoCollectionAbstract {

    private static final Logger logger = LoggerFactory.getLogger(NodeInfoHourCollection.class);

    /**
     * 一个小时跑一次，获取DB数据，计算所有节点以 HOUR 为单位的 avg, max, min
     */
    @Async
    @Scheduled(cron = "0 0 0/1 * * ? ")
    @Override
    public void collect() {
        try {
            List<Cluster> allClusterList = clusterService.getAllClusterList();
            if (allClusterList == null || allClusterList.isEmpty()) {
                return;
            }
            for (Cluster cluster : allClusterList) {
                threadPool.submit(new CollectNodeInfoTask(cluster, NodeInfoType.TimeType.HOUR));
            }
        } catch (Exception e) {
            logger.error("Collect node info data failed.", e);
        }
    }
}
