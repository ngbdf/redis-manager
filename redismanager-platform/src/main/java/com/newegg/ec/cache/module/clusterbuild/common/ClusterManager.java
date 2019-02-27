package com.newegg.ec.cache.module.clusterbuild.common;

import com.newegg.ec.cache.core.entity.model.Cluster;
import com.newegg.ec.cache.core.entity.model.ClusterImportResult;
import com.newegg.ec.cache.core.entity.model.Host;
import com.newegg.ec.cache.core.entity.redis.ConnectionParam;
import com.newegg.ec.cache.core.entity.redis.RedisNode;
import com.newegg.ec.cache.core.entity.redis.RedisQueryParam;
import com.newegg.ec.cache.core.entity.redis.RedisValue;
import com.newegg.ec.cache.module.clusterbuild.build.ClusterService;
import com.newegg.ec.cache.module.clusterbuild.common.redis.IRedis;
import com.newegg.ec.cache.module.clusterbuild.common.redis.RedisClusterClient;
import com.newegg.ec.cache.module.clusterbuild.common.redis.RedisExtendClient;
import com.newegg.ec.cache.module.clusterbuild.common.redis.RedisStandAloneClient;
import com.newegg.ec.cache.util.JedisUtil;
import com.newegg.ec.cache.util.NetUtil;
import com.newegg.ec.cache.util.SlotBalanceUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lzz on 2018/4/20.
 */
@Component
public class ClusterManager {
    public static final Log logger = LogFactory.getLog(ClusterManager.class);

    /**
     * 用于统计已经倒入多少数据
     */
    public static Map<String, AtomicInteger> importMap = new HashMap<>();

    @Resource
    private ClusterService clusterService;

    public static String getImportKey(String ownerIp, int ownerPort, String targetIp, int targetPort, String formatKey) {
        return ownerIp + "-" + ownerPort + "-" + targetIp + "-" + targetPort + "-" + formatKey;
    }

    public IRedis factory(int clusterId, String address) {
        IRedis redis;
        Host host = NetUtil.getHostPassAddress(address);
        Cluster cluster = clusterService.getCluster(clusterId);
        ConnectionParam param = new ConnectionParam(host.getIp(), host.getPort(), cluster.getRedisPassword());
        int version = JedisUtil.getRedisVersion(param);
        if (version > 2) {
            redis = new RedisClusterClient(param);
        } else {
            redis = new RedisStandAloneClient(param);
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

    public RedisValue query(RedisQueryParam redisQueryParam) {
        RedisValue redisValue = new RedisValue();
        IRedis redis = factory(redisQueryParam.getClusterId(), redisQueryParam.getAddress());
        if (!redisQueryParam.getKey().equals("*")) {
            redisValue  = redis.getRedisValue(redisQueryParam.getDb(), redisQueryParam.getKey());
        }
        if (null == redisValue.getRedisValue()) {
            Object res = redis.scanRedis(redisQueryParam.getDb(), redisQueryParam.getKey());
            redisValue.setRedisValue(res);
        }
        redis.close();
        return redisValue;
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

        Jedis jedis = JedisUtil.getJedisClient(param);

        try {
            jedis.clusterReplicate(masterId);
            res = true;
        } catch (Exception e) {
            logger.error("Be Slave error", e);
        } finally {

            jedis.close();
        }
        return res;
    }

    public boolean beMaster(ConnectionParam param) {
        boolean res = false;
        Jedis jedis = JedisUtil.getJedisClient(param);
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
        Jedis myselef = JedisUtil.getJedisClient(param);
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
                    logger.error("forget node error", e);
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
        Jedis jedis = JedisUtil.getJedisClient(slaveParam);
        try {
            jedis.clusterMeet(masterIp, masterPort);
            res = true;
        } catch (Exception e) {
            logger.error("cluster meet error", e);
        } finally {
            jedis.close();
        }
        return res;
    }

    public Map<RedisNode, List<RedisNode>> buildClusterMeet(int clusterId, Map<RedisNode, List<RedisNode>> ipMap,boolean isExtends) {
        Cluster cluster = clusterService.getCluster(clusterId);
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
                    // 如果扩容有密码的集群，meet的时候不需要认证密码
                    if (isExtends) {
                        param = new ConnectionParam(masterIp, masterPort);
                    }
                    clusterMeet(param, currentAliableIp, currentAliablePort);
                    for (RedisNode redisNode : slaveList) {
                        logger.info(redisNode.getIp() + ":" + redisNode.getPort() + " is meet cluster");
                        param.setIp(redisNode.getIp());
                        param.setPort(redisNode.getPort());
                        clusterMeet(param, masterIp, masterPort);
                        Thread.sleep(500);
                    }
                } else {
                    logger.error(masterIp + ":" + masterPort + " master is install fail");
                }
            } catch (Exception e) {
                throw new RuntimeException("Cluster meet error.",e);
            }
        }
        return ipMapRes;
    }

    public boolean buildClusterBeSlave(int clusterId, Map<RedisNode, List<RedisNode>> ipMap,boolean isExtends) {
        Cluster cluster = clusterService.getCluster(clusterId);
        String password = cluster.getRedisPassword();
        for (Map.Entry<RedisNode, List<RedisNode>> nodeItem : ipMap.entrySet()) {
            try {
                RedisNode master = nodeItem.getKey();
                String masterIp = master.getIp();
                int masterPort = master.getPort();
                if (NetUtil.checkIpAndPort(masterIp, masterPort)) {
                    // 如果扩容有密码的集群，beSlave的时候不需要认证密码
                    if (isExtends){
                        String nodeId = JedisUtil.getNodeId(new ConnectionParam(masterIp, masterPort));
                        List<RedisNode> slaveList = nodeItem.getValue();
                        for (RedisNode redisNode : slaveList) {
                            logger.info(redisNode.getIp() + ":" + redisNode.getPort() + " is be slave to " + masterIp + ":" + masterPort);
                            ConnectionParam param = new ConnectionParam(redisNode.getIp(), redisNode.getPort());
                            beSlave(param, nodeId);
                            Thread.sleep(500);
                        }
                    }else{
                        String nodeId = JedisUtil.getNodeId(new ConnectionParam(masterIp, masterPort, password));
                        List<RedisNode> slaveList = nodeItem.getValue();
                        for (RedisNode redisNode : slaveList) {
                            logger.info(redisNode.getIp() + ":" + redisNode.getPort() + " is be slave to " + masterIp + ":" + masterPort);
                            ConnectionParam param = new ConnectionParam(redisNode.getIp(), redisNode.getPort(), password);
                            beSlave(param, nodeId);
                            Thread.sleep(500);
                        }
                    }

                }
            } catch (Exception e) {
                throw new RuntimeException("Cluster be Slave error",e);
            }
        }
        return true;
    }

    /**
     * 建立集群
     * @param clusterId
     * @param ipMap
     * @param isExtends true： 扩容操作建立集群 false ：初始建立集群
     * @return
     */
    public boolean buildCluster(int clusterId, Map<RedisNode, List<RedisNode>> ipMap,boolean isExtends) {
        try{
            logger.info("start meet all node to cluster");
            buildClusterMeet(clusterId, ipMap,isExtends);
            logger.info("start set slave for cluster");
            buildClusterBeSlave(clusterId, ipMap, isExtends);
        }catch (Exception e){
            logger.error("Build Cluster error", e);
            return false;
        }
        return true;
    }

    public boolean batchConfig(Cluster cluster, String myIp, int myPort, String configName, String configValue) {
        boolean res = true;
        if ("requirepass".equalsIgnoreCase(configName)) {
            return false;
        }
        ConnectionParam param = new ConnectionParam(myIp, myPort);
        String password = cluster.getRedisPassword();
        param.setRedisPassword(password);
        List<Map<String, String>> nodeList = JedisUtil.getNodeList(param);
        for (Map<String, String> node : nodeList) {
            String ip = node.get("ip");
            int port = Integer.parseInt(node.get("port"));
            Jedis jedis = new Jedis(ip, port);
            if (org.apache.commons.lang.StringUtils.isNotBlank(password)) {
                jedis.auth(password);
            }
            try {
                List<String> configList = jedis.configGet(configName);
                if (configList.size() != 2) {
                    break;
                }

                //Edit Truman for support save ""or save "600 30000" or save 600 3000 start
                if (configValue.indexOf("\"") >= 0) {
                    configValue = configValue.replace("\"", "");
                }
                //Edit Truman for support save ""or save "600 30000" or save 600 3000 end

                jedis.configSet(configName, configValue);
                jedis.clusterSaveConfig();

                // 同步一下配置文件
                RedisExtendClient redisClient = new RedisExtendClient(ip, port);
                try {
                    if (org.apache.commons.lang.StringUtils.isNotBlank(password)) {
                        redisClient.redisCommandOpt(password, RedisExtendClient.REWRITE);
                    } else {
                        redisClient.redisCommandOpt(RedisExtendClient.REWRITE);
                    }
                } catch (IOException e) {
                    logger.error("rewrite conf error", e);
                } finally {
                    redisClient.closeClient();
                }
            } catch (Exception e) {
                res = false;
                logger.error("Update config error, ip: " + ip + ", port: " + port, e);
            } finally {
                jedis.close();
            }
        }
        return res;
    }

    public void addRedisPassd(String ip, int port, String password) {

        Jedis jedis = new Jedis(ip, port);
        try {
            //先判断是否是3.0 or 4.0的集群
            ConnectionParam param = new ConnectionParam(ip, port);
            Map<String, String> nodeInfo = JedisUtil.getMapInfo(param);
            String redisVersion = nodeInfo.get("redis_version");
            if (!redisVersion.startsWith("1.0.")) {
                //注意masterauth要在前
                jedis.configSet("masterauth", password);
                jedis.configSet("requirepass", password);
                jedis.auth(password);
                jedis.clusterSaveConfig();
                // 同步一下配置文件
                RedisExtendClient redisClient = new RedisExtendClient(ip, port);
                try {
                    redisClient.redisCommandOpt(password, RedisExtendClient.REWRITE);
                } catch (IOException e) {
                    logger.error("rewrite conf error", e);
                } finally {
                    redisClient.closeClient();
                }
                logger.info("Node " + ip + ":" + port + " Install success");
            }


        } catch (Exception e) {
            logger.error("add RedisPassd error, ip: " + ip + ", port: " + port, e);
        } finally {
            jedis.close();
        }
    }

    public boolean initSlot(Cluster cluster, String address) {
        boolean res = true;
        Host host = NetUtil.getHost(address);
        String ip = host.getIp();
        int port = host.getPort();
        String password = cluster.getRedisPassword();
        ConnectionParam param = new ConnectionParam(ip, port, password);
        List<Map<String, String>> masterList = JedisUtil.getNodeList(param, true);
        int masterSize = masterList.size();
        List<SlotBalanceUtil.Shade> balanceSlots = SlotBalanceUtil.balanceSlot(masterSize);
        for (int i = 0; i < balanceSlots.size(); i++) {
            SlotBalanceUtil.Shade shade = balanceSlots.get(i);
            int start = shade.getStartSlot();
            int end = shade.getEndSlot();
            Map<String, String> hostMap = masterList.get(i);
            String itemIp = hostMap.get("ip");
            String itemPort = hostMap.get("port");
            int intItemPort = Integer.parseInt(itemPort);
            Jedis jedis = new Jedis(itemIp, intItemPort);
            if (org.apache.commons.lang.StringUtils.isNotBlank(password)) {
                jedis.auth(password);
            }
            try {
                for (int slot = start; slot <= end; slot++) {
                    try {
                        String resstr = jedis.clusterAddSlots(slot);
                        if (!resstr.equals("OK")) {
                            jedis.clusterAddSlots(slot);
                        }
                    } catch (Exception e) {
                        if(e instanceof JedisConnectionException){
                            logger.error(e.getMessage() + " Can Not Init Slot");
                        }else{
                            logger.error("", e);
                        }
                        res = false;
                    }
                }
            } catch (Exception e) {
                if(e instanceof  JedisConnectionException){
                    logger.error(e.getMessage() + " Can Not Init Slot");
                }else{
                    logger.error("", e);
                }
                res = false;
            } finally {
                jedis.close();
            }
        }
        return res;
    }

    public boolean reShard(Cluster cluster, String ip, int port, int startKey, int endKey) {
        boolean res = false;
        String password = cluster.getRedisPassword();
        ConnectionParam param = new ConnectionParam(ip, port, password);
        Map<String, Map> masterNodes = JedisUtil.getMasterNodes(param);
        Jedis jedis = null;
        try {
            jedis = new Jedis(ip, port);
            if (org.apache.commons.lang.StringUtils.isNotBlank(password)) {
                jedis.auth(password);
            }
            // 迁移必须要知道自己的 nodeid 和 source 的ip port nodeid
            for (int slot = startKey; slot <= endKey; slot++) {
                Map<String, String> slotObjmap = fillMoveSlotObject(masterNodes, ip, port, slot);
                String myselfId = slotObjmap.get("myselfId");
                String sourceId = slotObjmap.get("sourceId");
                String sourceIP = slotObjmap.get("sourceIP");
                String strSourcePort = slotObjmap.get("sourcePort");
                // 如果 strSourcePort 为空，则进行集群初始化
                if (org.apache.commons.lang.StringUtils.isBlank(strSourcePort)) {
                    String resstr = jedis.clusterAddSlots(slot);
                    if (!resstr.equals("OK")) {
                        jedis.clusterAddSlots(slot);
                    }
                    continue;
                }
                int sourcePort = Integer.parseInt(strSourcePort);
                try {
                    moveSlot(myselfId, ip, port, password, sourceId, sourceIP, sourcePort, slot);
                } catch (Exception e) {
                    logger.error("Move Slot Error : " + e.getMessage());
                }
            }
            res = true;
        } catch (Exception e) {
            logger.error("Cluster Add Slots Error : " + e.getMessage());
        } finally {
            jedis.close();
        }
        return res;
    }

    /**
     * 移动 slot
     *
     * @param myselfId
     * @param ip
     * @param port
     * @param sourceId
     * @param sourceIP
     * @param sourcePort
     * @param slot
     */
    private void moveSlot(String myselfId, String ip, int port, String password, String sourceId, String sourceIP, int sourcePort, int slot) {
        Jedis myself = new Jedis(ip, port);
        Jedis source = new Jedis(sourceIP, sourcePort);
        if (org.apache.commons.lang.StringUtils.isNotBlank(password)) {
            myself.auth(password);
            source.auth(password);
        }
        try {
            // 设置导入导出状态
            myself.clusterSetSlotImporting(slot, sourceId);
            source.clusterSetSlotMigrating(slot, myselfId);
            List<String> keys;
            // 真正迁移
            do {
                keys = source.clusterGetKeysInSlot(slot, 100);
                for (String key : keys) {
                    source.migrate(ip, port, key, 0, 600000);
                }
            } while (keys.size() > 0);
        } catch (Exception e) {
            logger.error(e.getMessage());
            // 出现异常就恢复 slot
            myself.clusterSetSlotStable(slot);
            source.clusterSetSlotStable(slot);
        } finally {
            // 设置 slot 给 myself 节点
            myself.clusterSetSlotNode(slot, myselfId);
            //？ 这个很奇怪如果没有设置 stable 它的迁移状态会一直在的
            source.clusterSetSlotStable(slot);
            source.close();
            myself.close();
        }
    }

    /**
     * 返回 slot
     *
     * @param
     * @param port
     */
    private Map<String, String> fillMoveSlotObject(Map<String, Map> masterNodes, String ip, int port, int slot) {
        Map<String, String> resultMap = new HashMap<>();
        String myselfId = "";
        String sourceIP = "";
        String sourcePort = "";
        String sourceId = "";
        for (Map.Entry<String, Map> masterNode : masterNodes.entrySet()) {
            Map<String, Object> master = masterNode.getValue();
            String masterHost = master.get("ip") + ":" + master.get("port");
            String nodeHost = ip + ":" + port;
            if (masterHost.equals(nodeHost)) {
                myselfId = (String) master.get("nodeId");
            }
            if (slotInMaster(master, slot)) {
                sourceId = (String) master.get("nodeId");
                sourceIP = (String) master.get("ip");
                sourcePort = String.valueOf(master.get("port"));
            }
            if (!"".equals(myselfId) && !"".equals(sourceIP) && "".equals(sourcePort) && !"".equals(sourceId)) {
                break;
            }
        }

        resultMap.put("myselfId", myselfId);
        resultMap.put("sourceIP", sourceIP);
        resultMap.put("sourcePort", sourcePort);
        resultMap.put("sourceId", sourceId);
        return resultMap;
    }

    private boolean slotInMaster(Map<String, Object> master, int slot) {
        String[] slots = (String[]) master.get("slot");
        for (Object itemSlot : slots) {
            String slotRange = (String) itemSlot;
            String[] slotArr = slotRange.split("-");
            if (2 == slotArr.length) {
                if (slot >= Integer.parseInt(slotArr[0]) && slot <= Integer.parseInt(slotArr[1])) {
                    return true;
                }
            } else if (1 == slotArr.length) {
                if (slot == Integer.parseInt(slotArr[0])) {
                    return true;
                }
            }
        }
        return false;
    }

}
