package com.newegg.ec.cache.app.component;

import com.newegg.ec.cache.app.component.redis.IRedis;
import com.newegg.ec.cache.app.component.redis.JedisClusterClient;
import com.newegg.ec.cache.app.component.redis.JedisMasterSlaveClient;
import com.newegg.ec.cache.app.logic.ClusterLogic;
import com.newegg.ec.cache.app.model.Cluster;
import com.newegg.ec.cache.app.model.Host;
import com.newegg.ec.cache.app.model.RedisNode;
import com.newegg.ec.cache.app.model.RedisQueryParam;
import com.newegg.ec.cache.app.util.JedisUtil;
import com.newegg.ec.cache.app.util.NetUtil;
import com.newegg.ec.cache.core.logger.CommonLogger;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by lzz on 2018/4/20.
 */
@Component
public class RedisManager {
    public static CommonLogger logger = new CommonLogger(RedisManager.class);
    @Resource
    private ClusterLogic clusterLogic;

    public IRedis factory(String address){
        IRedis redis;
        Host host = NetUtil.getHostPassAddress( address );
        String ip  = host.getIp();
        int port = host.getPort();
        int version = JedisUtil.getRedisVersion( ip, port );
        if( version > 2 ){
            redis = new JedisClusterClient( ip, port );
        }else{
            redis = new JedisMasterSlaveClient( ip, port );
        }
        return redis;
    }

    public Object query(RedisQueryParam redisQueryParam) {
        Object res = null;
        IRedis redis = factory( redisQueryParam.getAddress() );
        if( !redisQueryParam.getKey().equals("*") ){
            res = redis.getRedisValue( redisQueryParam.getDb(), redisQueryParam.getKey());
        }
        if( null == res ){
            res = redis.scanRedis( redisQueryParam.getDb(), redisQueryParam.getKey() );
        }
        redis.close();
        return res;
    }

    public Map<String, String> getClusterInfo(String ip, int port){
        Map<String, String> res = JedisUtil.getClusterInfo(ip, port);
        return res;
    }

    public Map<String,Map> getClusterNodes (String ip, int port) {
        return JedisUtil.getClusterNodes(ip, port);
    }

    public Map<String, String> getMapInfo(String ip, int port){
        Map<String, String> res = JedisUtil.getMapInfo(ip, port);
        return res;
    }

    public Map<String, String> getRedisConfig(String ip, int port){
        Map<String, String> res = JedisUtil.getRedisConfig(ip, port);
        return res;
    }

    public List<Map<String, String>> nodeList (String ip, int port) {
        return JedisUtil.nodeList(ip, port);
    }

    public List<Map<String, String>> getRedisDBList(String ip, int port){
        List<Map<String, String>> res = JedisUtil.dbInfo(ip, port);
        return res;
    }

    public boolean beSlave(String ip, int port, String masterId){
        boolean res = false;
        Jedis jedis = new Jedis(ip, port);
        try {
            jedis.clusterReplicate( masterId );
            res = true;
        }catch (Exception e){

        }finally {
            jedis.close();
        }
        return res;
    }

    public boolean beMaster(String ip, int port) {
        boolean res = false;
        Jedis jedis = new Jedis(ip, port);
        try {
            jedis.clusterFailover();
            res = true;
        } catch (Exception e){

        }finally {
            jedis.close();
        }
        return res;
    }

    public boolean forget(String ip, int port, String nodeId) {
        List<Map<String, String>> masterList = JedisUtil.getNodeList(ip, port);
        Jedis myselef = new Jedis(ip, port);
        try {
            for(int i = 0; i < masterList.size(); i++){
                String nodeIp = masterList.get(i).get("ip").trim();
                String nodePort = masterList.get(i).get("port");
                if( (StringUtils.isBlank(nodeIp) || StringUtils.isBlank( nodePort )) ||
                        (nodeIp.equals(ip) && nodePort.equals(String.valueOf(port))) ){
                    continue;
                }
                Jedis jedis = null;
                try {
                    jedis = new Jedis( nodeIp, Integer.parseInt(nodePort));
                    jedis.clusterForget( nodeId );
                }catch ( Exception e ){
                    logger.error("", e );
                }finally {
                    if( null != jedis ){
                        jedis.close();
                    }
                }
                // forget 自己的信息
                myselef.clusterReset(JedisCluster.Reset.HARD);
            }
        }catch ( Exception e ){
            logger.error("", e );
        }finally {
            myselef.close();
        }
        return true;
    }

    public boolean clusterMeet(String slaveIp, int slavePort, String masterIp, int masterPort){
        boolean res = false;
        Jedis jedis = new Jedis( slaveIp, slavePort );
        try {
            jedis.clusterMeet( masterIp, masterPort );
            res = true;
        }catch (Exception e){

        }finally {
            jedis.close();
        }
        return res;
    }

    public Map<RedisNode, List<RedisNode>>  buildClusterMeet(int clusterId, Map<RedisNode, List<RedisNode>> ipMap){
        Cluster cluster = clusterLogic.getCluster( clusterId );
        Host host = NetUtil.getHostPassAddress( cluster.getAddress() );
        Map<RedisNode, List<RedisNode>> ipMapRes = new HashedMap();
        String currentAliableIp = host.getIp();
        Integer currentAliablePort = host.getPort();
        for(Map.Entry< RedisNode, List<RedisNode> > nodeItem : ipMap.entrySet() ){
            try {
                RedisNode master = nodeItem.getKey();
                String masterIp = master.getIp();
                int masterPort = master.getPort();
                if( NetUtil.checkIpAndPort(masterIp, masterPort) ){
                    List<RedisNode> slaveList = nodeItem.getValue();
                    ipMapRes.put(master, slaveList);
                    clusterMeet( masterIp, masterPort, currentAliableIp, currentAliablePort );
                    for(RedisNode redisNode : slaveList){
                        logger.websocket( redisNode.getIp() + ":" + redisNode.getPort() + " is meet cluster");
                        clusterMeet( redisNode.getIp(), redisNode.getPort(), masterIp, masterPort );
                        Thread.sleep(500);
                    }
                }else{
                    logger.websocket( masterIp + ":" + masterPort + " master is install fail");
                }
            }catch (Exception e){

            }
        }
        return ipMapRes;
    }

    public boolean buildClusterBeSlave(Map<RedisNode, List<RedisNode>> ipMap){
        for(Map.Entry< RedisNode, List<RedisNode> > nodeItem : ipMap.entrySet() ){
            try {
                RedisNode master = nodeItem.getKey();
                String masterIp = master.getIp();
                int masterPort = master.getPort();
                if( NetUtil.checkIpAndPort(masterIp, masterPort) ){
                    String nodeId = JedisUtil.getNodeid(masterIp, masterPort);
                    List<RedisNode> slaveList = nodeItem.getValue();
                    for(RedisNode redisNode : slaveList){
                        logger.websocket( redisNode.getIp() + ":" + redisNode.getPort() + " is be slave to " + masterIp + ":" + masterPort);
                        beSlave( redisNode.getIp(), redisNode.getPort(), nodeId );
                        Thread.sleep(500);
                    }
                }
            }catch (Exception e){

            }
        }
        return true;
    }

    public boolean buildCluster(int clusterId, Map<RedisNode, List<RedisNode>> ipMap){
        logger.websocket("start meet all node to cluster");
        buildClusterMeet(clusterId, ipMap );
        logger.websocket("start set slave for cluster");
        buildClusterBeSlave( ipMap );
        return true;
    }


}
