package com.newegg.ec.redis.schedule;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.newegg.ec.redis.dao.IClusterDao;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.NodeRole;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.entity.SentinelMaster;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.IRedisNodeService;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.service.ISentinelMastersService;
import com.newegg.ec.redis.util.RedisNodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.newegg.ec.redis.entity.NodeRole.MASTER;
import static com.newegg.ec.redis.entity.NodeRole.SLAVE;
import static com.newegg.ec.redis.entity.RedisNode.CONNECTED;
import static com.newegg.ec.redis.util.RedisUtil.REDIS_MODE_SENTINEL;

/**
 * 2020/03/08: 为了降低代码耦合，cluster, redis node, sentinel master 信息的更新在此进行
 * <p>
 * 1.cluster:
 * 1) cluster info 相关信息更新
 * 2) 根据节点状态（runState， inCluster, flags）设置健康状态
 * 2. redis node：
 * 1) 新增节点自动写入 database，如果写入失败，则忽略
 * 2) 更新节点状态， runState， inCluster, flags, linked
 *
 * @author Jay.H.Zou
 * @date 2020/3/8
 */
@Component
public class ClusterUpdateSchedule implements IDataCollection, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ClusterUpdateSchedule.class);

    @Autowired
    private IClusterService clusterService;

    @Autowired
    private IRedisService redisService;

    @Autowired
    private IRedisNodeService redisNodeService;

    @Autowired
    private ISentinelMastersService sentinelMastersService;


    private ExecutorService threadPool;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        int coreSize = Runtime.getRuntime().availableProcessors();
        threadPool = new ThreadPoolExecutor(coreSize, coreSize, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("cluster-update-pool-thread-%d").build());
    }

    @Async
    @Scheduled(cron = "30 0/1 * * * ?")
    @Override
    public void collect() {
        List<Cluster> clusterList = clusterService.getAllClusterList();
        if (clusterList == null || clusterList.isEmpty()) {
            return;
        }
        logger.info("Start update node status and cluster status...");
        clusterList.forEach(cluster -> threadPool.submit(new ClusterUpdateTask(cluster)));
    }


    private class ClusterUpdateTask implements Runnable {

        private Cluster cluster;

        public ClusterUpdateTask(Cluster cluster) {
            this.cluster = cluster;
        }

        @Override
        public void run() {
            try {
                addNewNodeToDB(cluster);
                Cluster.ClusterState clusterStateByNode = updateRedisNodeState(cluster);
                // update sentinel master
                if (Objects.equals(REDIS_MODE_SENTINEL, cluster.getRedisMode())) {
                    updateSentinelMasters(cluster);
                }
                updateCluster(clusterStateByNode, cluster);
            } catch (Exception e) {
                logger.error("Update cluster failed, cluster name = " + cluster.getClusterName(), e);
            }

        }
    }

    private void updateCluster(Cluster.ClusterState clusterState, Cluster cluster) {
        Cluster completedCluster = clusterService.completeClusterInfo(cluster);
        // 再次判断集群状态
        Cluster.ClusterState clusterStateByInfo = completedCluster.getClusterState();
        if (Objects.equals(clusterStateByInfo, Cluster.ClusterState.BAD)) {
            clusterState = clusterStateByInfo;
        }
        try {
            completedCluster.setClusterState(clusterState);
            clusterService.updateCluster(completedCluster);
        } catch (Exception e) {
            logger.error("Update cluster completed info failed, cluster name = " + cluster.getClusterName(), e);
        }
    }

    private void addNewNodeToDB(Cluster cluster) {
        Integer clusterId = cluster.getClusterId();
        List<RedisNode> realRedisNodeList = redisService.getRealRedisNodeList(cluster);
        List<RedisNode> dbRedisNodeList = redisNodeService.getRedisNodeList(clusterId);
        for (RedisNode dbRedisNode : dbRedisNodeList) {
            realRedisNodeList.removeIf(redisNode -> RedisNodeUtil.equals(redisNode, dbRedisNode));
        }
        if (!realRedisNodeList.isEmpty()) {
            RedisNodeUtil.setRedisRunStatus(cluster.getRedisMode(), realRedisNodeList);
            redisNodeService.addRedisNodeList(realRedisNodeList);
        }
    }

    /**
     * 更新所有 redis node 状态
     *
     * @param cluster
     * @return 如果节点有问题，则改变 cluster state 为 WARN
     */
    private Cluster.ClusterState updateRedisNodeState(Cluster cluster) {
        Cluster.ClusterState clusterState = Cluster.ClusterState.HEALTH;
        List<RedisNode> redisNodeList = redisNodeService.getMergedRedisNodeList(cluster.getClusterId());
        for (RedisNode redisNode : redisNodeList) {
            boolean runStatus = redisNode.getRunStatus();
            boolean inCluster = redisNode.getInCluster();
            String flags = redisNode.getFlags();
            boolean flagsNormal = Objects.equals(flags, SLAVE.getValue()) || Objects.equals(flags, MASTER.getValue());
            String linkState = redisNode.getLinkState();
            NodeRole nodeRole = redisNode.getNodeRole();
            // 节点角色为 UNKNOWN
            boolean nodeRoleNormal = Objects.equals(nodeRole, MASTER) || Objects.equals(nodeRole, SLAVE);
            if (!runStatus || !inCluster || !flagsNormal || !Objects.equals(linkState, CONNECTED) || !nodeRoleNormal) {
                clusterState = Cluster.ClusterState.WARN;
            }
            redisNodeService.updateRedisNode(redisNode);
        }
        return clusterState;
    }


    /**
     * @param cluster
     */
    private void updateSentinelMasters(Cluster cluster) {
        Integer clusterId = cluster.getClusterId();
        List<SentinelMaster> newSentinelMasters = new LinkedList<>();
        List<SentinelMaster> realSentinelMasterList = redisService.getSentinelMasters(clusterService.getClusterById(clusterId));
        Iterator<SentinelMaster> iterator = realSentinelMasterList.iterator();
        while (iterator.hasNext()) {
            SentinelMaster sentinelMaster = iterator.next();
            SentinelMaster sentinelMasterByMasterName = sentinelMastersService.getSentinelMasterByMasterName(clusterId, sentinelMaster.getName());
            sentinelMaster.setGroupId(cluster.getGroupId());
            sentinelMaster.setClusterId(clusterId);
            if (sentinelMasterByMasterName == null) {
                newSentinelMasters.add(sentinelMaster);
                iterator.remove();
            }
        }
        realSentinelMasterList.forEach(sentinelMaster -> sentinelMastersService.updateSentinelMaster(sentinelMaster));
        newSentinelMasters.forEach(sentinelMaster -> sentinelMastersService.addSentinelMaster(sentinelMaster));
    }

}
