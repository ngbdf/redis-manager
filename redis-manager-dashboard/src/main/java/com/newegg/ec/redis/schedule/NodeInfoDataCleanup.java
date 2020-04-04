package com.newegg.ec.redis.schedule;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.INodeInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 9/18/2019
 */
@Component
public class NodeInfoDataCleanup implements IDataCleanup {

    private static final Logger logger = LoggerFactory.getLogger(NodeInfoDataCleanup.class);

    @Autowired
    protected IClusterService clusterService;

    @Autowired
    protected INodeInfoService nodeInfoService;

    /**
     * 每天凌晨0点实行一次，清理数据
     */
    @Async
    @Scheduled(cron = "0 0 0 * * ?")
    @Override
    public void cleanup() {
        try {
            List<Cluster> allClusterList = clusterService.getAllClusterList();
            if (allClusterList == null || allClusterList.isEmpty()) {
                return;
            }
            for (Cluster cluster : allClusterList) {
                nodeInfoService.cleanupNodeInfo(cluster.getClusterId());
            }
        } catch (Exception e) {
            logger.error("Cleanup node info schedule failed, cause by getting cluster list", e);
        }
    }

}
