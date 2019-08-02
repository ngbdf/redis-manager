package com.newegg.ec.redis.service.impl;

import com.newegg.ec.redis.client.ClientFactory;
import com.newegg.ec.redis.client.RedisClient;
import com.newegg.ec.redis.client.RedisClusterClient;
import com.newegg.ec.redis.client.RedisURI;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.RedisQueryParam;
import com.newegg.ec.redis.entity.RedisQueryResult;
import com.newegg.ec.redis.plugin.install.entity.RedisNode;
import com.newegg.ec.redis.service.IRedisService;
import com.newegg.ec.redis.util.RedisClusterInfoUtil;
import com.newegg.ec.redis.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.newegg.ec.redis.util.RedisInfoUtil.OS;
import static com.newegg.ec.redis.util.RedisInfoUtil.REDIS_MODE;

/**
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
public class RedisService implements IRedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Override
    public List<String> getDBList(Set<HostAndPort> hostAndPortSet, String redisPassword) {
        return null;
    }

    @Override
    public List<RedisNode> getNodeList(Set<HostAndPort> hostAndPortSet, String redisPassword) {
        RedisURI redisURI = new RedisURI(hostAndPortSet, redisPassword);
        RedisClusterClient redisClusterClient = ClientFactory.buildRedisClusterClient(redisURI);
        redisClusterClient.getNodeList();
        return null;
    }

    public static void main(String[] args) {
        // cluster:{172.16.35.77:8204=redis.clients.jedis.JedisPool@5fb759d6, 172.16.35.77:8203=redis.clients.jedis.JedisPool@4b8d604b, 172.16.35.88:8200=redis.clients.jedis.JedisPool@5e7cd6cc, 172.16.35.77:8202=redis.clients.jedis.JedisPool@68c9d179, 172.16.35.77:8201=redis.clients.jedis.JedisPool@d554c5f, 172.16.35.77:8205=redis.clients.jedis.JedisPool@2dfaea86, 172.16.35.88:8205=redis.clients.jedis.JedisPool@15888343, 172.16.35.88:8203=redis.clients.jedis.JedisPool@33ecda92, 172.16.35.77:8200=redis.clients.jedis.JedisPool@14fc5f04, 172.16.35.88:8204=redis.clients.jedis.JedisPool@6e2829c7, 172.16.35.88:8201=redis.clients.jedis.JedisPool@3feb2dda, 172.16.35.88:8202=redis.clients.jedis.JedisPool@6a8658ff, 172.16.35.87:8202=redis.clients.jedis.JedisPool@1c742ed4, 172.16.35.87:8203=redis.clients.jedis.JedisPool@333d4a8c, 172.16.35.87:8200=redis.clients.jedis.JedisPool@55de24cc, 172.16.35.76:8204=redis.clients.jedis.JedisPool@dc7df28, 172.16.35.76:8205=redis.clients.jedis.JedisPool@30f842ca, 172.16.35.87:8201=redis.clients.jedis.JedisPool@69c81773, 172.16.35.76:8202=redis.clients.jedis.JedisPool@4d14b6c2, 172.16.35.76:8203=redis.clients.jedis.JedisPool@7e990ed7, 172.16.35.76:8200=redis.clients.jedis.JedisPool@c05fddc, 172.16.35.87:8204=redis.clients.jedis.JedisPool@25df00a0, 172.16.35.76:8201=redis.clients.jedis.JedisPool@4d15107f, 172.16.35.87:8205=redis.clients.jedis.JedisPool@7b4c50bc, 172.16.35.75:8201=redis.clients.jedis.JedisPool@5884a914, 172.16.35.75:8202=redis.clients.jedis.JedisPool@50378a4, 172.16.35.75:8200=redis.clients.jedis.JedisPool@60f00693, 172.16.35.75:8205=redis.clients.jedis.JedisPool@79207381, 172.16.35.75:8203=redis.clients.jedis.JedisPool@491b9b8, 172.16.35.75:8204=redis.clients.jedis.JedisPool@1a4927d6}
        HostAndPort hostAndPortSet = new HostAndPort("2", 8800);
        RedisURI redisURI = new RedisURI(hostAndPortSet, "12345678");
        RedisClusterClient redisClusterClient = ClientFactory.buildRedisClusterClient(redisURI);
        redisClusterClient.getNodeList();
    }
    @Override
    public Cluster getClusterInfo(Set<HostAndPort> hostAndPortSet, String redisPassword) {
        Cluster cluster = null;
        RedisURI redisURI = new RedisURI(hostAndPortSet, redisPassword);
        RedisClusterClient redisClusterClient = ClientFactory.buildRedisClusterClient(redisURI);
        String serverInfo = redisClusterClient.getInfo(RedisClient.SERVER);
        try {
            Map<String, String> serverInfoMap = RedisUtil.parseInfoToMap(serverInfo);
            cluster = infoMapToCluster(serverInfoMap);
        } catch (IOException e) {
            logger.error("Parse server info failed.", e);
        }
        return cluster;
    }

    @Override
    public Cluster getServerInfo(Set<HostAndPort> hostAndPortSet, String redisPassword) {
        Cluster cluster = null;
        RedisURI redisURI = new RedisURI(hostAndPortSet, redisPassword);
        RedisClusterClient redisClusterClient = ClientFactory.buildRedisClusterClient(redisURI);
        String clusterInfo = redisClusterClient.getClusterInfo();
        try {
            cluster = RedisClusterInfoUtil.parseClusterInfoToObject(clusterInfo);
        } catch (IOException e) {
            logger.error("Parse cluster info failed.", e);
        }
        return cluster;
    }

    @Override
    public List<String> scan(RedisQueryParam redisQueryParam) {
        return null;
    }

    @Override
    public RedisQueryResult query(RedisQueryParam redisQueryParam) {
        return null;
    }

    @Override
    public boolean forget() {
        return false;
    }

    private Set<HostAndPort> splitNodes(String nodes) {
        String[] nodesArr = nodes.split(",");
        int length = nodesArr.length;
        Set<HostAndPort> hostAndPortSet = new HashSet<>(length);
        if (length > 0) {
            for (String node : nodesArr) {
                String[] ipAndPort = node.split(":");
                HostAndPort hostAndPort = new HostAndPort(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
                hostAndPortSet.add(hostAndPort);
            }
        }
        return hostAndPortSet;
    }

    private Cluster infoMapToCluster(Map<String, String> infoMap) {
        Cluster cluster = new Cluster();
        cluster.setRedisMode(infoMap.get(REDIS_MODE));
        cluster.setOs(infoMap.get(OS));
        return cluster;
    }

}
