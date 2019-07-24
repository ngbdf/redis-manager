package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.NodeInfo;
import com.newegg.ec.redis.entity.NodeInfoQueryParam;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public interface INodeInfoService {

    List<NodeInfo> getAllNodeInfoList(NodeInfoQueryParam nodeInfoQueryParam);

    List<NodeInfo> getMonitorNodeInfoList(NodeInfoQueryParam nodeInfoQueryParam);

    int addNodeInfo(String clusterName, List<NodeInfo> nodeInfoList);

    int clearNodeInfo(String clusterName);
}
