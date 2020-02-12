package com.newegg.ec.redis.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.client.RedisClient;
import com.newegg.ec.redis.client.RedisClientFactory;
import com.newegg.ec.redis.dao.ISentinelMastersDao;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.SentinelMaster;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.service.ISentinelMastersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.HostAndPort;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.newegg.ec.redis.util.RedisUtil.nodesToHostAndPortSet;

/**
 * @author Jay.H.Zou
 * @date 1/22/2020
 */
@Service
public class SentinelMastersService implements ISentinelMastersService {

    private static final Logger logger = LoggerFactory.getLogger(SentinelMastersService.class);

    @Autowired
    private ISentinelMastersDao sentinelMastersDao;

    @Autowired
    private IRedisService redisService;

    @Override
    public List<SentinelMaster> getSentinelMasterByClusterId(Integer clusterId) {
        try {
            return sentinelMastersDao.selectSentinelMasterByClusterId(clusterId);
        } catch (Exception e) {
            logger.error("Get sentinel masters by cluster id failed.", e);
            return null;
        }
    }

    @Override
    public SentinelMaster getSentinelMasterByMasterName(Integer clusterId, String masterName) {
        try {
            if (Strings.isNullOrEmpty(masterName)) {
                logger.warn("master name can't be null.");
                return null;
            }
            return sentinelMastersDao.selectSentinelMasterByMasterName(clusterId, masterName);
        } catch (Exception e) {
            logger.info("Get sentinel master by master name failed.", e);
            return null;
        }
    }

    @Override
    public SentinelMaster getSentinelMasterById(Integer sentinelMasterId) {
        try {
            return sentinelMastersDao.selectSentinelMasterById(sentinelMasterId);
        } catch (Exception e) {
            logger.error("Get sentinel master by id failed.", e);
            return null;
        }
    }

    @Override
    public boolean updateSentinelMaster(SentinelMaster sentinelMaster) {
        try {
            return sentinelMastersDao.updateSentinelMaster(sentinelMaster) > 0;
        } catch (Exception e) {
            logger.error("Update sentinel master failed, " + sentinelMaster, e);
            return false;
        }
    }

    @Override
    public boolean addSentinelMaster(Cluster cluster) {
        try {
            // TODO: get sentinel masters info
            Set<HostAndPort> hostAndPorts = nodesToHostAndPortSet(cluster.getNodes());
            RedisClient redisClient = RedisClientFactory.buildRedisClient(hostAndPorts);
            List<Map<String, String>> sentinelMasters = redisClient.getSentinelMasters();
            for (Map<String, String> sentinel : sentinelMasters) {
                SentinelMaster sentinelMaster = new SentinelMaster();
                String flags = sentinel.get("flags");
                String numSlaves = sentinel.get("num-slaves");
                String host = sentinel.get("ip");
                String port = sentinel.get("port");
                String masterName = sentinel.get("name");
                String quorum = sentinel.get("quorum");
                String downAfterMilliseconds = sentinel.get("down-after-milliseconds");
                String failoverTimeout = sentinel.get("failover-timeout");
                String parallelSync = sentinel.get("parallel-syncs");
                sentinelMaster.setFlags(flags);
                sentinelMaster.setNumSlaves(Integer.parseInt(numSlaves));
                sentinelMaster.setMasterHost(host);
                sentinelMaster.setMasterPort(Integer.parseInt(port));
                sentinelMaster.setMasterName(masterName);
                sentinelMaster.setQuorum(Integer.parseInt(quorum));
                sentinelMaster.setDownAfterMilliseconds(Long.parseLong(downAfterMilliseconds));
                sentinelMaster.setFailoverTimeout(Long.parseLong(failoverTimeout));
                sentinelMaster.setParallelSync(Integer.parseInt(parallelSync));
                addSentinelMaster(sentinelMaster);
            }
            return true;
        } catch (Exception e) {
            logger.error("Add sentinel master host and port failed, " + cluster.getClusterName(), e);
            return false;
        }
    }

    @Override
    public boolean addSentinelMaster(SentinelMaster sentinelMaster) {
        try {
            return sentinelMastersDao.insertSentinelMaster(sentinelMaster) > 0;
        } catch (Exception e) {
            logger.error("Add sentinel master failed. ", e);
            return false;
        }
    }

    @Override
    public boolean deleteSentinelMaster(SentinelMaster sentinelMaster) {
        return false;
    }

    @Override
    public boolean deleteSentinelMasterById(Integer sentinelMasterId) {
        try {
            return sentinelMastersDao.deleteSentinelMasterById(sentinelMasterId) > 0;
        } catch (Exception e) {
            logger.error("Delete sentinel master failed. ", e);
            return false;
        }

    }

    @Override
    public boolean deleteSentinelMasterByClusterId(Integer clusterId) {
        try {
            return sentinelMastersDao.deleteSentinelMasterByClusterId(clusterId) > 0;
        } catch (Exception e) {
            logger.error("Delete sentinel master failed. ", e);
            return false;
        }
    }
}
