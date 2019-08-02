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
