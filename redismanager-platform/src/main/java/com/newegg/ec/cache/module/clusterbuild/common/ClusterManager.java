package com.newegg.ec.cache.module.clusterbuild.common;

import com.newegg.ec.cache.core.entity.model.Cluster;
import com.newegg.ec.cache.core.entity.model.ClusterImportResult;
import com.newegg.ec.cache.core.entity.model.Host;
import com.newegg.ec.cache.core.entity.redis.RedisConnectParam;
import com.newegg.ec.cache.core.entity.redis.RedisNode;
import com.newegg.ec.cache.core.entity.redis.RedisQueryParam;
import com.newegg.ec.cache.core.entity.redis.RedisValue;
import com.newegg.ec.cache.core.logger.RMException;
import com.newegg.ec.cache.module.clusterbuild.common.redis.IRedis;
import com.newegg.ec.cache.module.clusterbuild.common.redis.RedisClusterClient;
import com.newegg.ec.cache.module.clusterbuild.common.redis.RedisExtendClient;
import com.newegg.ec.cache.module.clusterbuild.common.redis.RedisSingleClient;
import com.newegg.ec.cache.util.NetUtil;
import com.newegg.ec.cache.util.SlotBalanceUtil;
import com.newegg.ec.cache.util.redis.RedisDataFormat;
import com.newegg.ec.cache.util.redis.RedisUtils;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

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


    public static String getImportKey(String ownerIp, int ownerPort, String targetIp, int targetPort, String formatKey) {
        return ownerIp + "-" + ownerPort + "-" + targetIp + "-" + targetPort + "-" + formatKey;
    }

    public IRedis factory(Cluster cluster, String address) {
        IRedis redis;
        Host host = NetUtil.getHostPassAddress(address);
        RedisConnectParam param = new RedisConnectParam(host.getIp(), host.getPort(), cluster.getRedisPassword());
        String mode = RedisUtils.getRedisMode(param);
        if ("cluster".equals(mode)) {
            redis = new RedisClusterClient(param);
        } else if("standalone".equals(mode)) {
            redis = new RedisSingleClient(param);
        }else {
            throw  new RMException("invalid redis mode type");
        }
        return redis;
    }

    public boolean importDataToCluster(Cluster cluster, String address, String targetAddress, String keyFormat) throws InterruptedException {
        Host host = NetUtil.getHostPassAddress(targetAddress);
        String targetIp = host.getIp();
        int targetPort = host.getPort();
        IRedis redis = factory(cluster, address);
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

    public RedisValue query(RedisQueryParam redisQueryParam,Cluster cluster) {
        RedisValue redisValue = new RedisValue();
        IRedis redis = factory(cluster, redisQueryParam.getAddress());
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

    public Map<String, String> getClusterInfo(RedisConnectParam param) {
        Map<String, String> res = RedisUtils.getClusterInfo(param);
        return res;
    }

    public int getDbSize(RedisConnectParam param) {
        return RedisUtils.dbSize(param);
    }

    public Map<String, Map> getClusterNodes(RedisConnectParam param) {
        RedisClient client = RedisClient.create(buildRedisURI(param));
        return  RedisDataFormat.changeNodesInfoMap(client.connect().sync().clusterNodes(), false);
    }

    public Map<String, String> getMapInfo(RedisConnectParam param) {
        Map<String, String> res = RedisUtils.getInfo(param);
        return res;
    }

    public Map<String, String> getRedisConfig(RedisConnectParam param) {
        Map<String, String> res = RedisUtils.getRedisConfig(param);
        return res;
    }

    public List<Map<String, String>> nodeList(RedisConnectParam param) {
        return RedisUtils.getAllNodes(param);
    }

    public List<Map<String, String>> getRedisDBList(RedisConnectParam param) {
        List<Map<String, String>> res = RedisUtils.redisDBInfo(param);
        return res;
    }

    public boolean beSlave(RedisConnectParam param, String masterId) {
        boolean res = false;
        RedisClient client = RedisClient.create(buildRedisURI(param));
        try {
            client.connect().sync().clusterReplicate(masterId);
            res = true;
        } catch (Exception e) {
            logger.error("Be Slave error", e);
        } finally {
            client.shutdown();
        }
        return res;
    }

    public boolean beMaster(RedisConnectParam param) {
        boolean res = false;
        RedisClient client = RedisClient.create(buildRedisURI(param));
        try {
            /**
             * 强制master failover
             */
            client.connect().sync().clusterFailover(true);
            res = true;
        } catch (Exception e) {
            logger.error("Be master error", e);
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
        return res;
    }

    public boolean forget(RedisConnectParam param, String nodeId) {

        List<Map<String, String>> masterList = RedisUtils.getAllNodes(param);
        String ip = param.getIp();
        int port = param.getPort();

        RedisClient myselef = RedisClient.create(buildRedisURI(param));

        try {
            for (int i = 0; i < masterList.size(); i++) {
                String nodeIp = masterList.get(i).get("ip").trim();
                String nodePort = masterList.get(i).get("port");
                if ((StringUtils.isBlank(nodeIp) || StringUtils.isBlank(nodePort)) ||
                        (nodeIp.equals(ip) && nodePort.equals(String.valueOf(port)))) {
                    continue;
                }
                RedisClient redis = null;
                try {
                    redis = RedisClient.create(buildRedisURI(nodeIp,Integer.parseInt(nodePort)));
                    String password = param.getRedisPassword();
                    RedisCommands<String, String> command = redis.connect().sync();
                    if (StringUtils.isNotBlank(password)) {
                        command.auth(password);
                    }
                    command.clusterForget(nodeId);
                } catch (Exception e) {
                    logger.error("forget node error", e);
                } finally {
                    if (null != redis) {
                        redis.shutdown();
                    }
                }
                // forget 自己的信息
                myselef.connect().sync().clusterReset(true);
            }
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            myselef.shutdown();
        }
        return true;
    }

    public boolean clusterMeet(RedisConnectParam slaveParam, String masterIp, int masterPort) {
        boolean res = false;
        RedisClient redis = RedisClient.create(buildRedisURI(slaveParam));
        try {
            redis.connect().sync().clusterMeet(masterIp, masterPort);
            res = true;
        } catch (Exception e) {
            logger.error("cluster meet error", e);
        } finally {
            redis.shutdown();
        }
        return res;
    }

    public Map<RedisNode, List<RedisNode>> buildClusterMeet(Cluster cluster, Map<RedisNode, List<RedisNode>> ipMap,boolean isExtends) {
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

                    RedisConnectParam param = new RedisConnectParam(masterIp, masterPort, cluster.getRedisPassword());
                    // 如果扩容有密码的集群，meet的时候不需要认证密码
                    if (isExtends) {
                        param = new RedisConnectParam(masterIp, masterPort);
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

    public boolean buildClusterBeSlave(Cluster cluster, Map<RedisNode, List<RedisNode>> ipMap,boolean isExtends) {
        String password = cluster.getRedisPassword();
        for (Map.Entry<RedisNode, List<RedisNode>> nodeItem : ipMap.entrySet()) {
            try {
                RedisNode master = nodeItem.getKey();
                String masterIp = master.getIp();
                int masterPort = master.getPort();
                if (NetUtil.checkIpAndPort(masterIp, masterPort)) {
                    // 如果扩容有密码的集群，beSlave的时候不需要认证密码
                    if (isExtends){
                        String nodeId = RedisUtils.getNodeId(new RedisConnectParam(masterIp, masterPort));
                        List<RedisNode> slaveList = nodeItem.getValue();
                        for (RedisNode redisNode : slaveList) {
                            logger.info(redisNode.getIp() + ":" + redisNode.getPort() + " is be slave to " + masterIp + ":" + masterPort);
                            RedisConnectParam param = new RedisConnectParam(redisNode.getIp(), redisNode.getPort());
                            beSlave(param, nodeId);
                            Thread.sleep(500);
                        }
                    }else{
                        String nodeId = RedisUtils.getNodeId(new RedisConnectParam(masterIp, masterPort, password));
                        List<RedisNode> slaveList = nodeItem.getValue();
                        for (RedisNode redisNode : slaveList) {
                            logger.info(redisNode.getIp() + ":" + redisNode.getPort() + " is be slave to " + masterIp + ":" + masterPort);
                            RedisConnectParam param = new RedisConnectParam(redisNode.getIp(), redisNode.getPort(), password);
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
     * @param cluster
     * @param ipMap
     * @param isExtends true： 扩容操作建立集群 false ：初始建立集群
     * @return
     */
    public boolean buildCluster(Cluster cluster, Map<RedisNode, List<RedisNode>> ipMap,boolean isExtends) {
        try{
            logger.info("start meet all node to cluster");
            buildClusterMeet(cluster, ipMap,isExtends);
            logger.info("start set slave for cluster");
            buildClusterBeSlave(cluster, ipMap,isExtends);
        }catch (Exception e){
            logger.error("Build Cluster error", e);
            return false;
        }
        return true;
    }

    public boolean batchConfig(Cluster cluster, String myIp, int myPort, String configName, String configValue){

        boolean res = true;
        /**
         * 不让通过UI修改redis密码
         */
        if ("requirepass".equalsIgnoreCase(configName)) {
            return false;
        }
        RedisConnectParam param = new RedisConnectParam(myIp, myPort);
        String password = cluster.getRedisPassword();
        param.setRedisPassword(password);

        List<Map<String, String>> nodeList = RedisUtils.getAllNodes(param);
        for (Map<String, String> node : nodeList) {
            String ip = node.get("ip");
            int port = Integer.parseInt(node.get("port"));
            RedisClient client = RedisClient.create(buildRedisURI(ip, port));
            RedisCommands<String, String> command = client.connect().sync();
            if (org.apache.commons.lang.StringUtils.isNotBlank(password)) {
                command.auth(password);
            }
            try {
                //Edit Truman for support save ""or save "600 30000" or save 600 3000 start
                if (configValue.indexOf("\"") >= 0) {
                    configValue = configValue.replace("\"", "");
                }
                //Edit Truman for support save ""or save "600 30000" or save 600 3000 end

                command.configSet(configName, configValue);
                command.clusterSaveconfig();

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
                client.shutdown();
            }
        }

        return res;
    }

    public void addRedisPassd(String ip, int port, String password) {

        RedisClient client = RedisClient.create(buildRedisURI(ip, port));
        RedisCommands<String, String> command = client.connect().sync();
        try {
            //先判断是否是3.0 or 4.0的集群
            RedisConnectParam param = new RedisConnectParam(ip, port);
            Map<String, String> nodeInfo = RedisUtils.getInfo(param);
            String redisVersion = nodeInfo.get("redis_version");
            if (!redisVersion.startsWith("1.0.")) {
                //注意masterauth要在前
                command.configSet("masterauth", password);
                command.configSet("requirepass", password);
                command.auth(password);
                command.clusterSaveconfig();
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
            client.shutdown();
        }
    }

    public boolean initSlot(Cluster cluster, String address) {
        boolean res = true;
        Host host = NetUtil.getHost(address);
        String ip = host.getIp();
        int port = host.getPort();
        String password = cluster.getRedisPassword();
        RedisConnectParam param = new RedisConnectParam(ip, port, password);
        List<Map<String, String>> masterList = RedisUtils.getMasterNodes(param, true);
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
            RedisClient client = RedisClient.create(buildRedisURI(itemIp, intItemPort));
            RedisCommands<String, String> command = client.connect().sync();
            if (org.apache.commons.lang.StringUtils.isNotBlank(password)) {
                command.auth(password);
            }
            try {
                for (int slot = start; slot <= end; slot++) {
                    try {
                        String resstr = command.clusterAddSlots(slot);
                        if (!resstr.equals("OK")) {
                            command.clusterAddSlots(slot);
                        }
                    } catch (Exception e) {
                        if(e instanceof RedisConnectionException){
                            logger.error(e.getMessage() + " Can Not Init Slot");
                        }else{
                            logger.error("", e);
                        }
                        res = false;
                    }
                }
            } catch (Exception e) {
                if(e instanceof  RedisConnectionException){
                    logger.error(e.getMessage() + " Can Not Init Slot");
                }else{
                    logger.error("", e);
                }
                res = false;
            } finally {
                client.shutdown();
            }
        }
        return res;
    }


    public boolean reShard(Cluster cluster, String ip, int port, int startKey, int endKey) {
        boolean res = false;
        String password = cluster.getRedisPassword();
        RedisConnectParam param = new RedisConnectParam(ip, port, password);
        Map<String, Map> masterNodes = RedisUtils.getMasterNodes(param);
        RedisClient client = null;
        try {
            client = RedisClient.create(buildRedisURI(ip, port));
            RedisCommands<String, String> command = client.connect().sync();
            if (org.apache.commons.lang.StringUtils.isNotBlank(password)) {
                command.auth(password);
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
                    String resstr = command.clusterAddSlots(slot);
                    if (!resstr.equals("OK")) {
                        command.clusterAddSlots(slot);
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
            client.shutdown();
        }
        return res;
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
        RedisCommands<String, String> myself = RedisClient.create(buildRedisURI(ip, port)).connect().sync();
        RedisCommands<String, String> source = RedisClient.create(buildRedisURI(sourceIP, sourcePort)).connect().sync();
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
        }
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


    private static RedisURI buildRedisURI(RedisConnectParam param) {
        if(StringUtils.isNotBlank(param.getRedisPassword())){
            return RedisURI.Builder.redis(param.getIp(), param.getPort()).withPassword(param.getRedisPassword()).build();
        }
        return RedisURI.Builder.redis(param.getIp(), param.getPort()).build();
    }

    private static RedisURI buildRedisURI(String ip, int port) {
        return RedisURI.Builder.redis(ip, port).build();
    }

}
