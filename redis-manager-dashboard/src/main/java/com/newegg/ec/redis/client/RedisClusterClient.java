package com.newegg.ec.redis.client;

import com.newegg.ec.redis.entity.NodeRole;
import com.newegg.ec.redis.entity.RedisNode;
import com.newegg.ec.redis.entity.RedisQueryParam;
import com.newegg.ec.redis.entity.RedisQueryResult;
import com.newegg.ec.redis.util.RedisUtil;
import com.newegg.ec.redis.util.SplitUtil;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.newegg.ec.redis.client.RedisURI.MAX_ATTEMPTS;
import static com.newegg.ec.redis.client.RedisURI.TIMEOUT;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public class RedisClusterClient implements IRedisClusterClient {

    private JedisCluster jedisCluster;

    private RedisClient redisClient;

    public RedisClusterClient(RedisURI redisURI) {
        Set<HostAndPort> hostAndPortSet = redisURI.getHostAndPortSet();
        String redisPassword = redisURI.getRequirePass();
        jedisCluster = new JedisCluster(hostAndPortSet, TIMEOUT, TIMEOUT, MAX_ATTEMPTS, redisPassword, new GenericObjectPoolConfig());
        redisClient = RedisClientFactory.buildRedisClient(redisURI);
    }

    @Override
    public JedisCluster getRedisClusterClient() {
        return jedisCluster;
    }

    /**
     * <id> <ip:port> <flags> <master> <ping-sent> <pong-recv> <config-epoch> <link-state> <slot> ... <slot>
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<RedisNode> clusterNodes() throws Exception {
        Jedis jedis = redisClient.getJedisClient();
        String nodes = jedis.clusterNodes();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(nodes.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
        String line;
        List<RedisNode> redisNodeList = new ArrayList<>();
        while ((line = bufferedReader.readLine()) != null) {
            List<String> item = SplitUtil.splitBySpace(line);
            String nodeId = item.get(0).trim();
            String ipPort = item.get(1);
            Set<HostAndPort> hostAndPortSet = RedisUtil.nodesToHostAndPortSet(SplitUtil.splitByAite(ipPort).get(0));
            HostAndPort hostAndPort = hostAndPortSet.iterator().next();
            String flags = item.get(2);
            String masterId = item.get(3);
            String linkState = item.get(7);
            RedisNode redisNode = new RedisNode(nodeId, hostAndPort.getHost(), hostAndPort.getPort(), null);
            redisNode.setFlags(flags);
            redisNode.setMasterId(masterId);
            redisNode.setLinkState(linkState);
            if (item.size() > 8) {
                String slotRang = item.get(8);
                redisNode.setSlotRange(slotRang);
            }
            if (flags.contains(NodeRole.MASTER.getValue())) {
                redisNode.setNodeRole(NodeRole.MASTER);
            } else if (flags.contains(NodeRole.SLAVE.getValue())) {
                redisNode.setNodeRole(NodeRole.SLAVE);
            } else {
                redisNode.setNodeRole(NodeRole.UNKNOWN);
            }
            redisNodeList.add(redisNode);
        }
        return redisNodeList;
    }

    @Override
    public boolean exists(String key) {
        return jedisCluster.exists(key);
    }

    @Override
    public String type(String key) {
        return jedisCluster.type(key);
    }

    @Override
    public long ttl(String key) {
        return jedisCluster.ttl(key);
    }

    @Override
    public Long del(String key) {
        return jedisCluster.del(key);
    }

    @Override
    public RedisQueryResult query(RedisQueryParam redisQueryParam) {
        String key = redisQueryParam.getKey();
        int count = redisQueryParam.getCount();
        String type = type(key);
        long ttl = ttl(key);
        Object value = null;
        switch (type) {
            case STRING:
                value = jedisCluster.get(key);
                break;
            case HASH:
                value = jedisCluster.hgetAll(key);
                break;
            case LIST:
                value = jedisCluster.lrange(key, 0, count);
                break;
            case SET:
                value = jedisCluster.srandmember(key, count);
                break;
            case ZSET:
                value = jedisCluster.zrange(key, 0, count);
                break;
            default:
                break;
        }
        return new RedisQueryResult(ttl, type, value);
    }

    @Override
    public RedisQueryResult scan(RedisQueryParam redisQueryParam) {
        ScanParams scanParams = redisQueryParam.buildScanParams();
        ScanResult<String> scanResult = jedisCluster.scan(redisQueryParam.getCursor(), scanParams);
        return new RedisQueryResult(scanResult);
    }

    @Override
    public void close() {
        if (redisClient != null) {
            redisClient.close();
        }
        if (jedisCluster != null) {
            jedisCluster.close();
        }
    }

}
