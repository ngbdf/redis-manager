package com.newegg.ec.redis.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.dao.INodeInfoDao;
import com.newegg.ec.redis.dao.IRedisNodeDao;
import com.newegg.ec.redis.entity.NodeInfo;
import com.newegg.ec.redis.entity.NodeInfoParam;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.exception.ConfigurationException;
import com.newegg.ec.redis.exception.ParameterException;
import com.newegg.ec.redis.service.INodeInfoService;
import com.newegg.ec.redis.util.RedisUtil;
import com.newegg.ec.redis.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 8/3/2019
 */
@Service
public class NodeInfoService implements INodeInfoService, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(NodeInfoService.class);

    @Value("${redis-manager.monitor.data-keep-days:15}")
    private int dataKeepDays;

    @Value("${spring.datasource.database}")
    private String database;

    private static final String NODE_INFO_TABLE = "node_info_%d";

    private static final int MAX_KEEP_DAYS = 365;

    public static final String ALL = "ALL";

    public static final String ALL_MASTER = "ALL_MASTER";

    @Autowired
    private INodeInfoDao nodeInfoDao;

    @Autowired
    private IRedisNodeDao redisNodeDao;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (dataKeepDays <= 0 || dataKeepDays > MAX_KEEP_DAYS) {
            throw new ConfigurationException("keep-days parameter is invalid, the value must be between 1 and 30.");
        }
        if (Strings.isNullOrEmpty(database)) {
            throw new ConfigurationException("database parameter can't be empty.");
        }
    }

    @Override
    public void createNodeInfoTable(Integer clusterId) {
        if (clusterId == null) {
            throw new ParameterException("Create `node_info_${clusterId}` failed, cause cluster id null.");
        }
        if (isNodeInfoTableExist(clusterId)) {
            throw new RuntimeException("Create `node_info_${clusterId}` failed, "
                    + String.format(NODE_INFO_TABLE, clusterId) + " exist.");
        }
        nodeInfoDao.createNodeInfoTable(clusterId);
    }

    @Override
    public void deleteNodeInfoTable(Integer clusterId) {
        if (clusterId == null) {
            throw new ParameterException("Delete `node_info_${clusterId}` failed, cause cluster id null.");
        }
        if (isNodeInfoTableExist(clusterId)) {
            nodeInfoDao.deleteNodeInfoTable(clusterId);
        }
    }

    @Override
    public boolean isNodeInfoTableExist(Integer clusterId) {
        String tableName = String.format(NODE_INFO_TABLE, clusterId);
        int row = nodeInfoDao.existNodeInfoTable(database, tableName);
        return row > 0;
    }

    /**
     * 传入不同的 param 获取相应的数据
     *
     * @param nodeInfoParam
     * @return
     */
    @Override
    public List<NodeInfo> getNodeInfoList(NodeInfoParam nodeInfoParam) {
        if (!verifyParam(nodeInfoParam)) {
            return null;
        }
        NodeInfoParam parameterCorrection = parameterCorrection(nodeInfoParam);
        try {
            if (!Strings.isNullOrEmpty(parameterCorrection.getInfoItem())) {
                return nodeInfoDao.selectNodeInfoListWithInfoItem(parameterCorrection);
            }
            return nodeInfoDao.selectNodeInfoList(parameterCorrection);
        } catch (Exception e) {
            logger.error("Get node info failed, " + parameterCorrection, e);
            return null;
        }
    }

    @Override
    public NodeInfo getLastTimeNodeInfo(NodeInfoParam nodeInfoParam) {
        List<NodeInfo> lastTimeNodeInfoList = getLastTimeNodeInfoList(nodeInfoParam);
        if (lastTimeNodeInfoList == null || lastTimeNodeInfoList.isEmpty()) {
            return null;
        }
        return lastTimeNodeInfoList.get(0);
    }

    @Override
    public List<NodeInfo> getLastTimeNodeInfoList(NodeInfoParam nodeInfoParam) {
        if (!verifyParam(nodeInfoParam)) {
            return null;
        }
        List<NodeInfo> nodeInfoList = null;
        try {
            String node = nodeInfoParam.getNode();
            nodeInfoParam.setNode(Strings.isNullOrEmpty(node) ? null : node);
            nodeInfoList = nodeInfoDao.selectLastTimeNodeInfo(nodeInfoParam);
        } catch (Exception e) {
            logger.error("Get last time node info failed, " + nodeInfoParam, e);
        }
        return nodeInfoList;
    }

    @Override
    public boolean addNodeInfo(NodeInfoParam nodeInfoParam, List<NodeInfo> nodeInfoList) {
        if (!verifyParam(nodeInfoParam) || nodeInfoList == null || nodeInfoList.isEmpty()) {
            return false;
        }
        int clusterId = nodeInfoParam.getClusterId();
        try {
            // 移除掉上一次集群记录
            nodeInfoDao.updateLastTimeStatus(clusterId, nodeInfoParam.getTimeType());
            int row = nodeInfoDao.insertNodeInfo(clusterId, nodeInfoList);
            if (row == nodeInfoList.size()) {
                return true;
            }
        } catch (Exception e) {
            logger.error("Add node info list failed, " + nodeInfoParam, e);
        }
        return false;
    }

    @Override
    public boolean cleanupNodeInfo(Integer clusterId) {
        Timestamp oldestTime = TimeUtil.getTime(dataKeepDays * TimeUtil.ONE_DAY);
        try {
            nodeInfoDao.deleteNodeInfoByTime(clusterId, oldestTime);
            return true;
        } catch (Exception e) {
            logger.error("Clean up node info data failed, cluster id = " + clusterId, e);
            return false;
        }
    }

    /**
     * 校验参数
     *
     * @param nodeInfoParam
     * @return true: param right
     */
    private boolean verifyParam(NodeInfoParam nodeInfoParam) {
        return nodeInfoParam != null;
    }

    /**
     * 参数纠正，防止查询不到数据或数据过多
     *
     * @param nodeInfoParam
     * @return
     */
    private NodeInfoParam parameterCorrection(NodeInfoParam nodeInfoParam) {
        Timestamp startTime = nodeInfoParam.getStartTime();
        Timestamp endTime = nodeInfoParam.getEndTime();
        if (endTime == null) {
            endTime = TimeUtil.getCurrentTimestamp();
            nodeInfoParam.setEndTime(endTime);
        }
        if (startTime == null) {
            startTime = TimeUtil.getDefaultLastTimestamp();
            nodeInfoParam.setStartTime(startTime);
        }
        // endTime <= startTime
        if (endTime.getTime() - startTime.getTime() <= 0) {
            endTime = TimeUtil.getCurrentTimestamp();
            startTime = TimeUtil.getDefaultLastTimestamp();
            nodeInfoParam.setEndTime(endTime);
            nodeInfoParam.setStartTime(startTime);
        }
        List<String> nodeList = nodeInfoParam.getNodeList();
        if (nodeList == null || nodeList.isEmpty()) {
            List<RedisNode> redisNodes = redisNodeDao.selectRedisNodeListByCluster(nodeInfoParam.getClusterId());
            nodeList = new LinkedList<>();
            for (RedisNode redisNode : redisNodes) {
                nodeList.add(RedisUtil.getNodeString(redisNode));
            }
            nodeInfoParam.setNodeList(nodeList);
        }
        return nodeInfoParam;
    }

}
