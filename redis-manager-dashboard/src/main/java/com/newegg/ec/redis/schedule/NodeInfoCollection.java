package com.newegg.ec.redis.schedule;

import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.INodeInfoService;
import com.newegg.ec.redis.service.IRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public class NodeInfoCollection implements IDataCollection {

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private IRedisService redisService;

    @Autowired
    private INodeInfoService nodeInfoService;

    @Scheduled(cron = "")
    @Override
    public void collectData() {

    }

}
