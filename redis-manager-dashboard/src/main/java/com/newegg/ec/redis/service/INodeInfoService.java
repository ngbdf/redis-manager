package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.NodeInfo;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public interface INodeInfoService {

    List<NodeInfo> getAllNodeInfoList();

    List<NodeInfo> getMonitorNodeInfoList();

    int addNodeInfo();

}
