package com.newegg.ec.redis.service.impl;

import com.google.common.base.Strings;
import com.newegg.ec.redis.dao.IRedisNodeDao;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.NodeRole;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.IRedisNodeService;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.util.NetworkUtil;
import com.newegg.ec.redis.util.RedisUtil;
import com.newegg.ec.redis.util.SignUtil;
import com.newegg.ec.redis.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.newegg.ec.redis.entity.NodeRole.MASTER;
import static com.newegg.ec.redis.entity.NodeRole.UNKNOWN;

/**
 * @author Jay.H.Zou
 * @date 10/9/2019
 */
@Service
public class RedisNodeService implements IRedisNodeService {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    private IRedisNodeDao redisNodeDao;

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private IRedisService redisService;

    @Override
    public List<RedisNode> getRedisNodeListByClusterId(Integer clusterId) {
        try {
            Cluster cluster = clusterService.getClusterById(clusterId);
            List<RedisNode> realRedisNodeList = redisService.getRedisNodeList(cluster);
            List<RedisNode> dbRedisNodeList = redisNodeDao.selectRedisNodeListByClusterId(clusterId);
            List<RedisNode> redisNodeList = mergeRedisNode(realRedisNodeList, dbRedisNodeList);
            List<RedisNode> redisNodes = sortRedisNodeList(redisNodeList);
            return redisNodes;
        } catch (Exception e) {
            logger.error("Get redis node list failed.", e);
            return null;
        }
    }

    @Override
    public RedisNode getRedisNodeById(Integer redisNodeId) {
        try {
            RedisNode redisNode = redisNodeDao.selectRedisNodeById(redisNodeId);
            return redisNode;
        } catch (Exception e) {
            logger.error("Get redis node by id failed.", e);
            return null;
        }
    }

    @Override
    public boolean existRedisNode(RedisNode redisNode) {
        try {
            RedisNode existRedisNode = redisNodeDao.existRedisNode(redisNode);
            return existRedisNode != null;
        } catch (Exception e) {
            logger.error("Get redis node by id failed.", e);
            return true;
        }
    }

    /**
     * merge redis node info
     * TODO: 未完成
     *
     * @param realRedisNodeList
     * @param dbRedisNodeList
     * @return
     */
    public List<RedisNode> mergeRedisNode(List<RedisNode> realRedisNodeList, List<RedisNode> dbRedisNodeList) {
        List<RedisNode> redisNodeList = new ArrayList<>();
        realRedisNodeList.forEach(realRedisNode -> {
            realRedisNode.setInCluster(true);
            realRedisNode.setRunStatus(true);
            if (dbRedisNodeList != null && !dbRedisNodeList.isEmpty()) {
                dbRedisNodeList.forEach(dbRedisNode -> {
                    if (RedisUtil.equals(dbRedisNode, realRedisNode)) {
                        realRedisNode.setContainerId(dbRedisNode.getContainerId());
                        realRedisNode.setContainerName(dbRedisNode.getContainerName());
                    }
                });
            }
            realRedisNode.setInsertTime(TimeUtil.getCurrentTimestamp());
            redisNodeList.add(realRedisNode);
        });
        Iterator<RedisNode> dbIterator = dbRedisNodeList.iterator();
        while (dbIterator.hasNext()) {
            RedisNode dbRedisNode = dbIterator.next();
            Iterator<RedisNode> realIterator = realRedisNodeList.iterator();
            while (realIterator.hasNext()) {
                RedisNode realRedisNode = realIterator.next();
                if (RedisUtil.equals(dbRedisNode, realRedisNode)) {
                    realIterator.remove();
                    dbIterator.remove();
                    continue;
                }
            }
        }
        if (dbRedisNodeList.isEmpty()) {
            return redisNodeList;
        }
        dbRedisNodeList.forEach(redisNode -> {
            redisNode.setInCluster(false);
            boolean run = NetworkUtil.telnet(redisNode.getHost(), redisNode.getPort());
            redisNode.setRunStatus(run);
            redisNode.setLinkState("unknown");
            redisNodeList.add(redisNode);
        });
        return redisNodeList;
    }

    @Override
    public boolean addRedisNode(RedisNode redisNode) {
        List<RedisNode> redisNodeList = new ArrayList<>();
        redisNodeList.add(redisNode);
        return addRedisNodeList(redisNodeList);
    }

    @Override
    public boolean addRedisNodeList(List<RedisNode> redisNodeList) {
        try {
            redisNodeList.forEach(redisNode -> {
                if (Strings.isNullOrEmpty(redisNode.getNodeId())) {
                    redisNode.setNodeId(redisNode.getHost() + SignUtil.COLON + redisNode.getPort());
                }
            });
            redisNodeDao.insertRedisNodeList(redisNodeList);
            return true;
        } catch (Exception e) {
            logger.error("Add redis node list failed.", e);
            return false;
        }
    }

    @Override
    public boolean updateRedisNode(RedisNode redisNode) {
        try {
            return redisNodeDao.updateRedisNode(redisNode) > 0;
        } catch (Exception e) {
            logger.error("Update redis node failed.", e);
            return false;
        }
    }

    @Override
    public boolean deleteRedisNodeListByClusterId(Integer clusterId) {
        try {
            redisNodeDao.deleteRedisNodeListByClusterId(clusterId);
            return true;
        } catch (Exception e) {
            logger.error("Delete redis node list failed.", e);
            return false;
        }
    }

    @Override
    public boolean deleteRedisNodeById(Integer redisNodeId) {
        try {
            return redisNodeDao.deleteRedisNodeById(redisNodeId) > 0;
        } catch (Exception e) {
            logger.error("Delete redid node failed.", e);
            return false;
        }
    }

    @Override
    public List<RedisNode> sortRedisNodeList(List<RedisNode> redisNodeList) {
        Map<String, Set<RedisNode>> masterAndReplicaMap = new LinkedHashMap<>();
        redisNodeList.forEach(redisNode -> {
            String nodeId = redisNode.getNodeId();
            NodeRole nodeRole = redisNode.getNodeRole();
            String masterId = redisNode.getMasterId();
            if (Objects.equals(nodeRole, MASTER)) {
                masterId = nodeId;
            }
            Set<RedisNode> replicaSet = masterAndReplicaMap.get(masterId);
            if (replicaSet == null) {
                replicaSet = new TreeSet<>((o1, o2) -> {
                    int compareRole = o1.getNodeRole().compareTo(o2.getNodeRole());
                    if (compareRole != 0) {
                        return compareRole;
                    }
                    int compareHost = o1.getHost().compareTo(o2.getHost());
                    if (compareHost != 0) {
                        return compareHost;
                    }
                    return o1.getPort() - o2.getPort();
                });
            }
            replicaSet.add(redisNode);
            masterAndReplicaMap.put(masterId, replicaSet);
        });
        List<RedisNode> redisNodeSorted = new ArrayList<>();
        masterAndReplicaMap.values().forEach(redisNodeSet -> {
            redisNodeSet.forEach(redisNode -> redisNodeSorted.add(redisNode));
        });
        return redisNodeSorted;
    }

}
