package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.SentinelMaster;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 1/22/2020
 */
public interface ISentinelMastersService {

    List<SentinelMaster> getSentinelMasterByClusterId(Integer clusterId);

    SentinelMaster getSentinelMasterByMasterName(Integer clusterId, String masterName);

    SentinelMaster getSentinelMasterById(Integer sentinelMasterId);

    boolean updateSentinelMaster(SentinelMaster sentinelMaster);

    boolean addSentinelMaster(Cluster cluster);

    boolean addSentinelMaster(SentinelMaster sentinelMaster);

    boolean deleteSentinelMaster(SentinelMaster sentinelMaster);

    boolean deleteSentinelMasterById(Integer sentinelMasterId);

    boolean deleteSentinelMasterByClusterId(Integer clusterId);

}
