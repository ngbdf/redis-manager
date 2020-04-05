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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.newegg.ec.redis.entity.NodeRole.MASTER;
import static com.newegg.ec.redis.entity.RedisNode.CONNECTED;
import static com.newegg.ec.redis.entity.RedisNode.UNCONNECTED;

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
    public List<RedisNode> getRedisNodeList(Integer clusterId) {
        try {
            return redisNodeDao.selectRedisNodeListByCluster(clusterId);
        } catch (Exception e) {
            logger.error("Get redis node by cluster id failed.", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean existRedisNode(RedisNode redisNode) {
        try {
            return redisNodeDao.existRedisNode(redisNode) != null;
        } catch (Exception e) {
            logger.error("Get redis node by id failed.", e);
            return true;
        }
    }

    /**
     * merge redis node info
     *
     * @param realRedisNodeList
     * @param dbRedisNodeList
     * @return
     */
    @Override
    public List<RedisNode> mergeRedisNode(List<RedisNode> realRedisNodeList, List<RedisNode> dbRedisNodeList) {
        List<RedisNode> redisNodeList = new ArrayList<>();
        realRedisNodeList.forEach(realRedisNode -> {
            realRedisNode.setInCluster(true);
            dbRedisNodeList.forEach(dbRedisNode -> {
                if (RedisUtil.equals(dbRedisNode, realRedisNode)) {
                    realRedisNode.setContainerId(dbRedisNode.getContainerId());
                    realRedisNode.setContainerName(dbRedisNode.getContainerName());
                    realRedisNode.setUpdateTime(dbRedisNode.getUpdateTime());
                }
            });
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
                }
            }
        }
        dbRedisNodeList.forEach(redisNode -> {
            redisNode.setInCluster(false);
            redisNodeList.add(redisNode);
        });
        // set state
        for (RedisNode redisNode : realRedisNodeList) {
            boolean telnet = NetworkUtil.telnet(redisNode.getHost(), redisNode.getPort());
            redisNode.setRunStatus(telnet);
            redisNode.setLinkState(telnet ? CONNECTED : UNCONNECTED);
        }
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
                    redisNode.setNodeId(RedisUtil.getNodeString(redisNode));
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
            if (Strings.isNullOrEmpty(redisNode.getNodeId())) {
                redisNode.setNodeId(RedisUtil.getNodeString(redisNode));
            }
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
        masterAndReplicaMap.values().forEach(redisNodeSorted::addAll);
        return redisNodeSorted;
    }

}
