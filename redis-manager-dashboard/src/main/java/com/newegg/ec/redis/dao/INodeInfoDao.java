package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.NodeInfo;
import com.newegg.ec.redis.entity.NodeInfoQueryParam;

import java.util.List;

/**
 * Manage node info
 *
 * @author Jay.H.Zou
 * @date 2019/7/18
 */
public interface INodeInfoDao {

    List<NodeInfo> selectAllNodeInfoList(NodeInfoQueryParam nodeInfoQueryParam);

    List<NodeInfo> selectMonitorNodeInfoList(NodeInfoQueryParam nodeInfoQueryParam);

    int insertNodeInfo(String table, List<NodeInfo> nodeInfoList);

    int deleteNodeInfoByTime(NodeInfoQueryParam nodeInfoQueryParam);

    int deleteAllNodeInfo(NodeInfoQueryParam nodeInfoQueryParam);

}
