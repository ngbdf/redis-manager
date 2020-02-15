package com.newegg.ec.redis.service;

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

    boolean addSentinelMaster(SentinelMaster sentinelMaster);

    boolean deleteSentinelMasterByName(Integer clusterId, String masterName);

    boolean deleteSentinelMasterByClusterId(Integer clusterId);

}
