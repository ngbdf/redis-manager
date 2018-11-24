package com.newegg.ec.cache.app.component;

import com.newegg.ec.cache.app.component.redis.IRedis;
import com.newegg.ec.cache.app.component.redis.JedisClusterClient;
import com.newegg.ec.cache.app.component.redis.JedisMasterSlaveClient;
import com.newegg.ec.cache.app.logic.ClusterLogic;
import com.newegg.ec.cache.app.model.*;
import com.newegg.ec.cache.app.util.JedisUtil;
import com.newegg.ec.cache.app.util.NetUtil;
import com.newegg.ec.cache.core.logger.CommonLogger;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lzz on 2018/4/20.
 */
@Component
public class RedisManager {
    public static CommonLogger logger = new CommonLogger(RedisManager.class);
    public static Map<String, AtomicInteger> importMap = new HashMap<>(); // 用于统计已经倒入多少数据

    @Resource
    private ClusterLogic clusterLogic;

    public static String getImportKey(String ownerIp, int ownerPort, String targetIp, int targetPort, String formatKey) {
        return ownerIp + "-" + ownerPort + "-" + targetIp + "-" + targetPort + "-" + formatKey;
    }

    public IRedis factory(int clusterId, String address) {
        IRedis redis;
        Host host = NetUtil.getHostPassAddress(address);
        Cluster cluster = clusterLogic.getCluster(clusterId);
        ConnectionParam param = new ConnectionParam(host.getIp(), host.getPort(), cluster.getRedisPassword());
        int version = JedisUtil.getRedisVersion(param);
        if (version > 2) {
            redis = new JedisClusterClient(param);
        } else {
            redis = new JedisMasterSlaveClient(param);
        }
        return redis;
    }

    public boolean importDataToCluster(int clusterId, String address, String targetAddress, String keyFormat) throws InterruptedException {
        Host host = NetUtil.getHostPassAddress(targetAddress);
        String targetIp = host.getIp();
        int targetPort = host.getPort();
        IRedis redis = factory(clusterId, address);
        if (StringUtils.isBlank(keyFormat)) {
            keyFormat = "*";
        }
        return redis.importDataToCluster(targetIp, targetPort, keyFormat);
    }

    public List<ClusterImportResult> getClusterImportResult() {
        List<ClusterImportResult> clusterImportResultList = new ArrayList<>();
        for (Map.Entry<String, AtomicInteger> item : importMap.entrySet()) {
            String key = item.getKey();
            String[] fields = key.split("-");
            AtomicInteger importCount = item.getValue();
            clusterImportResultList.add(new ClusterImportResult(fields[0], Integer.parseInt(fields[1]), fields[2], Integer.parseInt(fields[3]), fields[4], importCount));
        }
        return clusterImportResultList;
    }

    public Object query(RedisQueryParam redisQueryParam) {
        Object res = null;
        IRedis redis = factory(redisQueryParam.getClusterId(), redisQueryParam.getAddress());
        if (!redisQueryParam.getKey().equals("*")) {
            RedisValue redisValue = redis.getRedisValue(redisQueryParam.getDb(), redisQueryParam.getKey());
            res = redisValue.getResult();
        }
        if (null == res) {
            res = redis.scanRedis(redisQueryParam.getDb(), redisQueryParam.getKey());
        }
        redis.close();
        return res;
    }

    public Map<String, String> getClusterInfo(ConnectionParam param) {
        Map<String, String> res = JedisUtil.getClusterInfo(param);
        return res;
    }

    public int getDbSize(ConnectionParam param) {
        int allCount = 0;
        if (JedisUtil.getRedisVersion(param) == 2) {
            allCount = JedisUtil.dbSize(param);
        } else {
            Map<String, Map> masterNodes = JedisUtil.getMasterNodes(param);
            for (Map.Entry<String, Map> nodeItem : masterNodes.entrySet()) {
                Map node = nodeItem.getValue();
                String itemIp = String.valueOf(node.get("ip"));
                String itemPort = String.valueOf(node.get("port"));
                param.setIp(itemIp);
                param.setPort(JedisUtil.getPort(itemPort));
                allCount += JedisUtil.dbSize(param);
            }
        }
        return allCount;
    }

    public Map<String, Map> getClusterNodes(ConnectionParam param) {
        return JedisUtil.getClusterNodes(param);
    }

    public Map<String, String> getMapInfo(ConnectionParam param) {
        Map<String, String> res = JedisUtil.getMapInfo(param);
        return res;
    }

    public Map<String, String> getRedisConfig(ConnectionParam param) {
        Map<String, String> res = JedisUtil.getRedisConfig(param);
        return res;
    }

    public List<Map<String, String>> nodeList(ConnectionParam param) {
        return JedisUtil.nodeList(param);
    }

    public List<Map<String, String>> getRedisDBList(ConnectionParam param) {
        List<Map<String, String>> res = JedisUtil.dbInfo(param);
        return res;
    }

    public boolean beSlave(ConnectionParam param, String masterId) {
        boolean res = false;

        Jedis jedis = getJedisClient(param);

        try {
            jedis.clusterReplicate(masterId);
            res = true;
        } catch (Exception e) {

        } finally {

            jedis.close();
        }
        return res;
    }

    public boolean beMaster(ConnectionParam param) {
        boolean res = false;
        Jedis jedis = getJedisClient(param);
        try {
            jedis.clusterFailover();
            res = true;
        } catch (Exception e) {
            logger.error("Be master error", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return res;
    }

    public boolean forget(ConnectionParam param, String nodeId) {
        List<Map<String, String>> masterList = JedisUtil.getNodeList(param);
        String ip = param.getIp();
        int port = param.getPort();
        Jedis myselef = getJedisClient(param);
        try {
            for (int i = 0; i < masterList.size(); i++) {
                String nodeIp = masterList.get(i).get("ip").trim();
                String nodePort = masterList.get(i).get("port");
                if ((StringUtils.isBlank(nodeIp) || StringUtils.isBlank(nodePort)) ||
                        (nodeIp.equals(ip) && nodePort.equals(String.valueOf(port)))) {
                    continue;
                }
                Jedis jedis = null;
                try {
                    jedis = new Jedis(nodeIp, Integer.parseInt(nodePort));
                    String password = param.getRedisPassword();
                    if (StringUtils.isNotBlank(password)) {
                        jedis.auth(password);
                    }
                    jedis.clusterForget(nodeId);
                } catch (Exception e) {
                    logger.error("", e);
                } finally {
                    if (null != jedis) {
                        jedis.close();
                    }
                }
                // forget 自己的信息
                myselef.clusterReset(JedisCluster.Reset.HARD);
            }
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            myselef.close();
        }
        return true;
    }

    public boolean clusterMeet(ConnectionParam slaveParam, String masterIp, int masterPort) {
        boolean res = false;
        Jedis jedis = getJedisClient(slaveParam);
        try {
            jedis.clusterMeet(masterIp, masterPort);
            res = true;
        } catch (Exception e) {

        } finally {
            jedis.close();
        }
        return res;
    }

    public Map<RedisNode, List<RedisNode>> buildClusterMeet(int clusterId, Map<RedisNode, List<RedisNode>> ipMap) {
        Cluster cluster = clusterLogic.getCluster(clusterId);
        Host host = NetUtil.getHostPassAddress(cluster.getAddress());
        Map<RedisNode, List<RedisNode>> ipMapRes = new HashedMap();
        String currentAliableIp = host.getIp();
        Integer currentAliablePort = host.getPort();
        for (Map.Entry<RedisNode, List<RedisNode>> nodeItem : ipMap.entrySet()) {
            try {
                RedisNode master = nodeItem.getKey();
                String masterIp = master.getIp();
                int masterPort = master.getPort();
                if (NetUtil.checkIpAndPort(masterIp, masterPort)) {
                    List<RedisNode> slaveList = nodeItem.getValue();
                    ipMapRes.put(master, slaveList);

                    ConnectionParam param = new ConnectionParam(masterIp, masterPort, cluster.getRedisPassword());
                    clusterMeet(param, currentAliableIp, currentAliablePort);
                    for (RedisNode redisNode : slaveList) {
                        logger.websocket(redisNode.getIp() + ":" + redisNode.getPort() + " is meet cluster");
                        param.setIp(redisNode.getIp());
                        param.setPort(redisNode.getPort());
                        clusterMeet(param, masterIp, masterPort);
                        Thread.sleep(500);
                    }
                } else {
                    logger.websocket(masterIp + ":" + masterPort + " master is install fail");
                }
            } catch (Exception e) {

            }
        }
        return ipMapRes;
    }

    public boolean buildClusterBeSlave(int clusterId, Map<RedisNode, List<RedisNode>> ipMap) {
        Cluster cluster = clusterLogic.getCluster(clusterId);
        String password = cluster.getRedisPassword();
        for (Map.Entry<RedisNode, List<RedisNode>> nodeItem : ipMap.entrySet()) {
            try {
                RedisNode master = nodeItem.getKey();
                String masterIp = master.getIp();
                int masterPort = master.getPort();
                if (NetUtil.checkIpAndPort(masterIp, masterPort)) {
                    String nodeId = JedisUtil.getNodeid(masterIp, masterPort);
                    List<RedisNode> slaveList = nodeItem.getValue();
                    for (RedisNode redisNode : slaveList) {
                        logger.websocket(redisNode.getIp() + ":" + redisNode.getPort() + " is be slave to " + masterIp + ":" + masterPort);
                        ConnectionParam param = new ConnectionParam(redisNode.getIp(), redisNode.getPort(), password);
                        beSlave(param, nodeId);
                        Thread.sleep(500);
                    }
                }
            } catch (Exception e) {

            }
        }
        return true;
    }

    public boolean buildCluster(int clusterId, Map<RedisNode, List<RedisNode>> ipMap) {
        logger.websocket("start meet all node to cluster");
        buildClusterMeet(clusterId, ipMap);
        logger.websocket("start set slave for cluster");
        buildClusterBeSlave(clusterId, ipMap);
        return true;
    }

    private Jedis getJedisClient(ConnectionParam param) {
        Jedis jedis = new Jedis(param.getIp(), param.getPort());
        String password = param.getRedisPassword();
        if (StringUtils.isNotBlank(password)) {
            jedis.auth(password);
        }
        return jedis;
    }
}
