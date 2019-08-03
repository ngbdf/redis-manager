package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.NodeInfo;
import com.newegg.ec.redis.entity.NodeInfoParam;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public interface INodeInfoService {

    List<NodeInfo> getNodeInfoList(NodeInfoParam nodeInfoParam);

    NodeInfo getLastTimeNodeInfo(NodeInfoParam nodeInfoParam);

    List<NodeInfo> getLastHourNodeInfoList(int clusterId);

    boolean addNodeInfo(NodeInfoParam nodeInfoParam, List<NodeInfo> nodeInfoList);

    boolean cleanupNodeInfo(int clusterId);
}
