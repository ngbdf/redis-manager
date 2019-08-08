package com.newegg.ec.redis.client;

import com.newegg.ec.redis.entity.AutoCommandResult;
import com.newegg.ec.redis.entity.DataCommandsParam;
import com.newegg.ec.redis.entity.NodeInfo;
import com.newegg.ec.redis.entity.NodeRole;
import com.newegg.ec.redis.util.RedisNodeInfoUtil;
import org.junit.Test;
import redis.clients.jedis.HostAndPort;

import java.util.Map;

public class RedisClientTest {

    private static RedisURI redisURI = new RedisURI(new HostAndPort("10.16.50.223", 8018), null);

    private static RedisClient redisClient = RedisClientFactory.buildRedisClient(redisURI);

    @Test
    public void getJedisClient() {
    }

    @Test
    public void getInfo() throws Exception {
        Map<String, String> infoMap = redisClient.getInfo();
    }

    @Test
    public void getInfo1() {
    }

    @Test
    public void getClusterInfo() throws Exception {
        Map<String, String> info = redisClient.getInfo();
        NodeInfo nodeInfo = RedisNodeInfoUtil.parseInfoToObject(info, null);
        NodeRole role = nodeInfo.getRole();
        System.err.println(role);
    }

    @Test
    public void getMemory() {
    }

    @Test
    public void getMemory1() {
    }

    @Test
    public void nodes() {
    }

    @Test
    public void exists() {
    }

    @Test
    public void type() {
    }

    @Test
    public void del() {
    }

    @Test
    public void query() {
    }

    /**
     * *, ? ,[]
     */
    @Test
    public void scan() {
    }

    @Test
    public void dbSize() {
    }

    @Test
    public void bgSave() {
    }

    @Test
    public void lastSave() {
    }

    @Test
    public void bgRewriteAof() {
    }

    @Test
    public void slaveOf() {
    }

    @Test
    public void role() {
    }

    @Test
    public void auth() {
    }

    @Test
    public void shutdown() {
    }

    @Test
    public void clientList() {
    }

    @Test
    public void getConfig() {

    }


    @Test
    public void rewriteConfig() {
    }

    @Test
    public void ping() {
    }

    @Test
    public void object1() {
    }

    @Test
    public void getSlowLog() {
    }

    @Test
    public void clientSetName() {
    }

    @Test
    public void clusterMeet() {
    }

    @Test
    public void clusterReplicate() {
    }

    @Test
    public void clusterFailOver() {
    }

    @Test
    public void clusterAddSlots() {
    }

    @Test
    public void clusterForget() {
    }

    @Test
    public void clusterSlaves() {
    }
}