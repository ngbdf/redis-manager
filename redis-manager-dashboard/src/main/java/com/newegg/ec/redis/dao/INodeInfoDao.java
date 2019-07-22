package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.NodeInfo;

import java.util.List;

/**
 * Manage node info
 *
 * @author Jay.H.Zou
 * @date 2019/7/18
 */
public interface INodeInfoDao {

    List<NodeInfo> selectNodeInfoByTime(String table);

    List<NodeInfo> selectMonitorNodeInfoByTime(String table);

    int insertNodeInfo(String table, List<NodeInfo> nodeInfoList);

    int deleteNodeInfoByTime(String table);

    int deleteAllNodeInfo(String table);

}
