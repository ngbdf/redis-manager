package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.NodeInfo;
import com.newegg.ec.redis.entity.NodeInfoParam;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public interface INodeInfoService {

    void createNodeInfoTable(Integer clusterId);

    void deleteNodeInfoTable(Integer clusterId);

    boolean isNodeInfoTableExist(Integer clusterId);

    List<NodeInfo> getNodeInfoList(NodeInfoParam nodeInfoParam);

    NodeInfo getLastTimeNodeInfo(NodeInfoParam nodeInfoParam);

    List<NodeInfo> getLastTimeNodeInfoList(NodeInfoParam nodeInfoParam);

    boolean addNodeInfo(NodeInfoParam nodeInfoParam, List<NodeInfo> nodeInfoList);

    boolean cleanupNodeInfo(int clusterId);
}
