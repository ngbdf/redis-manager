package com.newegg.ec.redis.service.impl;

import com.newegg.ec.redis.dao.INodeInfoDao;
import com.newegg.ec.redis.entity.NodeInfo;
import com.newegg.ec.redis.entity.NodeInfoParam;
import com.newegg.ec.redis.entity.NodeInfoType;
import com.newegg.ec.redis.exception.ConfigurationException;
import com.newegg.ec.redis.service.INodeInfoService;
import com.newegg.ec.redis.util.TimeRangeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 8/3/2019
 */
public class NodeInfoService implements INodeInfoService, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(NodeInfoService.class);

    @Value("redis-manager.monitor.data-keep-days")
    private int dataKeepDays;

    private static final int MAX_KEEP_DAYS = 30;

    @Autowired
    private INodeInfoDao nodeInfoDao;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (dataKeepDays <= 0 || dataKeepDays > MAX_KEEP_DAYS) {
            throw new ConfigurationException("keep-days parameter is invalid, the value must be between 1 and 30.");
        }
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
        List<NodeInfo> nodeInfoList = null;
        try {
            nodeInfoList = nodeInfoDao.selectNodeInfoList(parameterCorrection);
        } catch (Exception e) {
            logger.error("Get node info failed, " + parameterCorrection, e);
        }
        return nodeInfoList;
    }


    @Override
    public NodeInfo getLastTimeNodeInfo(NodeInfoParam nodeInfoParam) {
        if (!verifyParam(nodeInfoParam)) {
            return null;
        }
        NodeInfo nodeInfo = null;
        try {
            nodeInfo = nodeInfoDao.selectLastTimeNodeInfo(nodeInfoParam);
        } catch (Exception e) {
            logger.error("Get last time node info failed, " + nodeInfoParam, e);
        }
        return nodeInfo;
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
            NodeInfoType.TimeType timeType = nodeInfoParam.getTimeType();
            for (NodeInfo nodeInfo : nodeInfoList) {
                nodeInfo.setTimeType(timeType);
            }
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
    public boolean cleanupNodeInfo(int clusterId) {
        Timestamp oldestTime = TimeRangeUtil.getTime(dataKeepDays * TimeRangeUtil.ONE_DAY);
        try {
            nodeInfoDao.deleteNodeInfoByTime(clusterId, oldestTime);
            return true;
        } catch (Exception e) {
            logger.error("Clean up node info data failed, cluster id = " + clusterId, e);
        }
        return false;
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
            endTime = TimeRangeUtil.getCurrentTimestamp();
            nodeInfoParam.setEndTime(endTime);
        }
        if (startTime == null) {
            startTime = TimeRangeUtil.getLastHourTimestamp();
            nodeInfoParam.setStartTime(startTime);
        }
        if (TimeRangeUtil.moreThanTwoDays(startTime, endTime)) {
            nodeInfoParam.setTimeType(NodeInfoType.TimeType.HOUR);
        }
        return nodeInfoParam;
    }

}
