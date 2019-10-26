package com.newegg.ec.redis.client;

import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.*;
import com.newegg.ec.redis.util.RedisUtil;
import com.newegg.ec.redis.util.SignUtil;
import javafx.util.Pair;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.params.MigrateParams;
import redis.clients.jedis.util.Slowlog;

import java.util.*;

import static com.newegg.ec.redis.client.RedisURI.TIMEOUT;
import static com.newegg.ec.redis.util.RedisUtil.*;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public class RedisClient implements IRedisClient {

    private static final String OK = "OK";

    private static final String PONG = "pong";

    /**
     * info subkey
     */
    public static final String SERVER = "server";

    public static final String KEYSPACE = "keyspace";

    public static final String REPLICATION = "replication";

    private Jedis jedis;

    private RedisURI redisURI;

    public RedisClient(RedisURI redisURI) {
        this.redisURI = redisURI;
        String redisPassword = redisURI.getRequirePass();
        String clientName = redisURI.getClientName();
        Set<HostAndPort> hostAndPortSet = redisURI.getHostAndPortSet();
        for (HostAndPort hostAndPort : hostAndPortSet) {
            try {
                jedis = new Jedis(hostAndPort.getHost(), hostAndPort.getPort(), TIMEOUT, TIMEOUT);
                if (!Strings.isNullOrEmpty(redisPassword)) {
                    jedis.auth(redisPassword);
                }
                if (!Strings.isNullOrEmpty(clientName)) {
                    jedis.clientSetname(clientName);
                }
                if (ping()) {
                    break;
                }
            } catch (JedisConnectionException e) {
                // try next nodes
            }
        }
    }

    @Override
    public Jedis getJedisClient() {
        return jedis;
    }

    @Override
    public Map<String, String> getInfo() throws Exception {
        return RedisUtil.parseInfoToMap(jedis.info());
    }

    @Override
    public Map<String, String> getInfo(String section) throws Exception {
        return RedisUtil.parseInfoToMap(jedis.info(section));
    }

    @Override
    public Map<String, String> getClusterInfo() throws Exception {
        return RedisUtil.parseInfoToMap(jedis.clusterInfo());
    }

    @Override
    public Set<String> scan(AutoCommandParam autoCommandParam) {
        ScanParams scanParams = autoCommandParam.buildScanParams();
        jedis.select(autoCommandParam.getDatabase());
        ScanResult<String> scanResult = jedis.scan(autoCommandParam.getCursor(), scanParams);
        return new LinkedHashSet<>(scanResult.getResult());
    }

    /**
     * redis 4, 4+
     * memory info
     *
     * @return
     */
    @Override
    public String getMemory() {
        return null;
    }

    /**
     * redis 4, 4+
     * memory info
     *
     * @return
     */
    @Override
    public String getMemory(String subKey) {
        return null;
    }

    /**
     * standalone nodes
     * <p>
     * role:master
     * connected_slaves:2
     * slave0:ip=127.0.0.1,port=8801,state=online,offset=152173185,lag=1
     * slave1:ip=127.0.0.1,port=8802,state=online,offset=152173185,lag=1
     * <p>
     * role:slave
     * master_host:127.0.0.1
     * master_port:8800
     * master_link_status:up
     *
     * @return
     */
    @Override
    public List<RedisNode> nodes() throws Exception {
        List<RedisNode> nodeList = new ArrayList<>();
        Map<String, String> infoMap = getInfo(REPLICATION);
        String role = infoMap.get(ROLE);
        String masterHost = null;
        Integer masterPort = null;
        // 使用 master node 进行连接
        if (Objects.equals(role, NodeRole.SLAVE.getValue())) {
            close();
            masterHost = infoMap.get(MASTER_HOST);
            masterPort = Integer.parseInt(infoMap.get(MASTER_PORT));
            RedisURI masterURI = new RedisURI(new HostAndPort(masterHost, masterPort), redisURI.getRequirePass());
            RedisClient redisClient = RedisClientFactory.buildRedisClient(masterURI);
            infoMap = redisClient.getInfo(REPLICATION);
            redisClient.close();
        } else {
            Set<HostAndPort> hostAndPortSet = redisURI.getHostAndPortSet();
            HostAndPort hostAndPort = hostAndPortSet.iterator().next();
            masterHost = hostAndPort.getHost();
            masterPort = hostAndPort.getPort();
        }
        for (Map.Entry<String, String> node : infoMap.entrySet()) {
            String infoKey = node.getKey();
            String infoValue = node.getValue();
            if (!infoKey.contains(NodeRole.SLAVE.getValue())) {
                continue;
            }
            String[] keyAndValues = SignUtil.splitByCommas(infoValue);
            if (keyAndValues.length < 2) {
                continue;
            }
            String slaveIp = null;
            String slavePort = null;
            for (String keyAndValue : keyAndValues) {
                String[] keyAndValueArray = SignUtil.splitByEqualSign(keyAndValue);
                String key = keyAndValueArray[0];
                String val = keyAndValueArray[1];
                if (Objects.equals(key, IP)) {
                    slaveIp = val;
                } else if (Objects.equals(key, PORT)) {
                    slavePort = val;
                }
            }
            if (!Strings.isNullOrEmpty(slaveIp) && !Strings.isNullOrEmpty(slavePort)) {
                RedisNode redisNode = new RedisNode(slaveIp, Integer.parseInt(slavePort), NodeRole.SLAVE);
                redisNode.setLinkState("connected");
                redisNode.setFlags(NodeRole.SLAVE.getValue());
                nodeList.add(redisNode);
            }
        }
        if (!Strings.isNullOrEmpty(masterHost)) {
            RedisNode masterNode = new RedisNode();
            masterNode.setNodeRole(NodeRole.MASTER);
            masterNode.setHost(masterHost);
            masterNode.setPort(masterPort);
            masterNode.setLinkState("connected");
            masterNode.setFlags(NodeRole.MASTER.getValue());
            nodeList.add(masterNode);
        }
        return nodeList;
    }

    @Override
    public boolean exists(String key) {
        return jedis.exists(key);
    }

    @Override
    public String type(String key) {
        return jedis.type(key);
    }

    @Override
    public long ttl(String key) {
        return jedis.ttl(key);
    }

    @Override
    public Long del(String key) {
        return jedis.del(key);
    }

    @Override
    public AutoCommandResult query(AutoCommandParam autoCommandParam) {
        String key = autoCommandParam.getKey();
        // for future
        int count = autoCommandParam.getCount();
        int database = autoCommandParam.getDatabase();
        String type = type(key);
        long ttl = ttl(key);
        jedis.select(database);
        Object value = null;
        switch (type) {
            case TYPE_STRING:
                value = jedis.get(key);
                break;
            case TYPE_HASH:
                value = jedis.hgetAll(key);
                break;
            case TYPE_LIST:
                value = jedis.lrange(key, 0, -1);
                break;
            case TYPE_SET:
                value = jedis.srandmember(key, -1);
                break;
            case TYPE_ZSET:
                value = jedis.zrangeWithScores(key, 0, -1);
                break;
            default:
                break;
        }
        return new AutoCommandResult(ttl, type, value);
    }

    @Override
    public Object string(DataCommandsParam dataCommandsParam) {
        jedis.select(dataCommandsParam.getDatabase());
        String command = dataCommandsParam.getCommand();
        String[] list = SignUtil.splitBySpace(command);
        String cmd = command.toUpperCase();
        String key = list[1];
        Object result = null;
        if (cmd.startsWith(GET)) {
            result = jedis.get(key);
        } else if (cmd.startsWith(SET)) {
            result = jedis.set(key, list[2]);
        }
        return result;
    }

    @Override
    public Object hash(DataCommandsParam dataCommandsParam) {
        jedis.select(dataCommandsParam.getDatabase());
        AutoCommandResult autoCommandResult = new AutoCommandResult();
        String command = dataCommandsParam.getCommand();
        String[] list = SignUtil.splitBySpace(command);
        String cmd = command.toUpperCase();
        String key = list[1];
        Object result = null;
        if (cmd.startsWith(HGETALL)) {
            result = jedis.hgetAll(key);
        } else if (cmd.startsWith(HGET)) {
            result = jedis.hget(key, list[2]);
        } else if (cmd.startsWith(HMGET)) {
            String[] items = removeCommandAndKey(list);
            result = jedis.hmget(key, items);
        } else if (cmd.startsWith(HKEYS)) {
            result = jedis.hkeys(key);
        } else if (cmd.startsWith(HSET)) {
            Map<String, String> hash = new HashMap<>();
            String[] items = removeCommandAndKey(list);
            for (int i = 0; i < items.length; i += 2) {
                hash.put(items[i], items[i + 1]);
            }
            result = jedis.hset(key, hash);
        }
        autoCommandResult.setValue(result);
        return autoCommandResult;
    }

    @Override
    public Object list(DataCommandsParam dataCommandsParam) {
        jedis.select(dataCommandsParam.getDatabase());
        String command = dataCommandsParam.getCommand();
        String[] list = SignUtil.splitBySpace(command);
        String cmd = command.toUpperCase();
        String key = list[1];
        String[] items = removeCommandAndKey(list);
        Object result = null;
        if (cmd.startsWith(LPUSH)) {
            result = jedis.lpush(key, items);
        } else if (cmd.startsWith(RPUSH)) {
            result = jedis.rpush(key, items);
        } else if (cmd.startsWith(LINDEX)) {
            result = jedis.lindex(key, Integer.valueOf(list[2]));
        } else if (cmd.startsWith(LLEN)) {
            result = jedis.llen(key);
        } else if (cmd.startsWith(LRANGE)) {
            int start = Integer.valueOf(list[2]);
            int stop = Integer.valueOf(list[3]);
            result = jedis.lrange(key, start, stop);
        }
        return result;
    }

    @Override
    public Object set(DataCommandsParam dataCommandsParam) {
        jedis.select(dataCommandsParam.getDatabase());
        String command = dataCommandsParam.getCommand();
        String[] list = SignUtil.splitBySpace(command);
        String cmd = command.toUpperCase();
        String key = list[1];
        Object result = null;
        if (cmd.startsWith(SCARD)) {
            result = jedis.scard(key);
        } else if (cmd.startsWith(SADD)) {
            result = jedis.sadd(key, removeCommandAndKey(list));
        } else if (cmd.startsWith(SMEMBERS)) {
            result = jedis.smembers(key);
        } else if (cmd.startsWith(SRANDMEMBER)) {
            int count = 1;
            if (list.length > 2) {
                count = Integer.valueOf(list[2]);
            }
            result = jedis.srandmember(key, count);
        }
        return result;
    }

    @Override
    public Object zset(DataCommandsParam dataCommandsParam) {
        jedis.select(dataCommandsParam.getDatabase());
        String command = dataCommandsParam.getCommand();
        String[] list = SignUtil.splitBySpace(command);
        String cmd = command.toUpperCase();
        String key = list[1];
        String param1 = list[2];
        String param2 = list[3];
        Object result = null;
        if (cmd.startsWith(ZCARD)) {
            result = jedis.zcard(key);
        } else if (cmd.startsWith(ZSCORE)) {
            result = jedis.zscore(key, param1);
        } else if (cmd.startsWith(ZCOUNT)) {
            result = jedis.zcount(key, param1, param2);
        } else if (cmd.startsWith(ZRANGE)) {
            int start = Integer.valueOf(param1);
            int stop = Integer.valueOf(param2);
            if (list.length > 4) {
                result = jedis.zrangeWithScores(key, start, stop);
            } else {
                result = jedis.zrange(key, start, stop);
            }
        } else if (cmd.startsWith(ZADD)) {
            result = jedis.zadd(key, Double.valueOf(param1), param2);
        }
        return result;
    }

    @Override
    public Object type(DataCommandsParam dataCommandsParam) {
        jedis.select(dataCommandsParam.getDatabase());
        String command = dataCommandsParam.getCommand();
        String key = RedisUtil.getKey(command);
        return type(key);
    }

    @Override
    public Object del(DataCommandsParam dataCommandsParam) {
        jedis.select(dataCommandsParam.getDatabase());
        String command = dataCommandsParam.getCommand();
        String key = RedisUtil.getKey(command);
        return del(key);
    }

    @Override
    public Long dbSize() {
        return jedis.dbSize();
    }

    @Override
    public String bgSave() {
        return jedis.bgsave();
    }

    @Override
    public Long lastSave() {
        return jedis.lastsave();
    }

    @Override
    public String bgRewriteAof() {
        return jedis.bgrewriteaof();
    }

    @Override
    public NodeRole role() throws Exception {
        Map<String, String> infoMap = getInfo(REPLICATION);
        String role = infoMap.get(ROLE);
        return NodeRole.value(role);
    }

    @Override
    public boolean auth(String password) {
        return Objects.equals(jedis.auth(password), OK);
    }

    @Override
    public boolean shutdown() {
        return Strings.isNullOrEmpty(jedis.shutdown());
    }

    /**
     * addr=127.0.0.1:43143 fd=6 age=183 idle=0 flags=N db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=32768 obl=0 oll=0 omem=0 events=r cmd=client
     * addr=127.0.0.1:43163 fd=5 age=35 idle=15 flags=N db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=0 obl=0 oll=0 omem=0 events=r cmd=ping
     * addr=127.0.0.1:43167 fd=7 age=24 idle=6 flags=N db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=0 obl=0 oll=0 omem=0 events=r cmd=get
     *
     * @return
     */
    @Override
    public String clientList() {
        return jedis.clientList();
    }

    @Override
    public boolean setConfig(Pair<String, String> keyAndValue) {
        String result = jedis.configSet(keyAndValue.getKey(), keyAndValue.getValue());
        return Objects.equals(result, OK);
    }

    @Override
    public Map<String, String> getConfig() {
        return getConfig("*");
    }

    @Override
    public Map<String, String> getConfig(String pattern) {
        List<String> configList = jedis.configGet(pattern);
        Map<String, String> configMap = new LinkedHashMap<>();
        for (int i = 0, length = configList.size(); i < length; i += 2) {
            String key = configList.get(i);
            if (Strings.isNullOrEmpty(key)) {
                continue;
            }
            configMap.put(key, configList.get(i + 1));
        }
        return configMap;
    }

    @Override
    public boolean rewriteConfig() {
        return Objects.equals(jedis.configRewrite(), OK);
    }

    @Override
    public String clusterSaveConfig() {
        return jedis.clusterSaveConfig();
    }

    @Override
    public boolean ping() {
        return Objects.equals(jedis.ping(), PONG);
    }

    @Override
    public List<Slowlog> getSlowLog(int size) {
        return jedis.slowlogGet(size);
    }

    @Override
    public boolean clientSetName(String clientName) {
        return Objects.equals(jedis.clientSetname(clientName), OK);
    }

    @Override
    public String clusterMeet(String host, int port) {
        return jedis.clusterMeet(host, port);
    }

    @Override
    public boolean clusterReplicate(String nodeId) {
        return Objects.equals(jedis.clusterReplicate(nodeId), OK);
    }

    @Override
    public boolean clusterFailOver() {
        return Objects.equals(jedis.clusterFailover(), OK);
    }

    @Override
    public String clusterAddSlots(int... slots) {
        return jedis.clusterAddSlots(slots);
    }

    @Override
    public String clusterSetSlotNode(int slot, String nodeId) {
        return jedis.clusterSetSlotNode(slot, nodeId);
    }

    @Override
    public String clusterSetSlotImporting(int slot, String nodeId) {
        return jedis.clusterSetSlotImporting(slot, nodeId);
    }

    @Override
    public String clusterSetSlotMigrating(int slot, String nodeId) {
        return jedis.clusterSetSlotMigrating(slot, nodeId);
    }

    @Override
    public List<String> clusterGetKeysInSlot(int slot, int count) {
        return jedis.clusterGetKeysInSlot(slot, count);
    }

    @Override
    public String clusterSetSlotStable(int slot) {
        return jedis.clusterSetSlotStable(slot);
    }

    @Override
    public boolean clusterForget(String nodeId) {
        return Objects.equals(jedis.clusterForget(nodeId), OK);
    }

    @Override
    public String clusterReset(ClusterReset reset) {
        return jedis.clusterReset(reset);
    }

    @Override
    public String migrate(String host, int port, String key, int destinationDb, int timeout) {
        return jedis.migrate(host, port, key, 0, timeout);
    }

    @Override
    public String migrate(String host, int port, int destinationDB,
                          int timeout, MigrateParams params, String... keys) {
        return jedis.migrate(host, port, destinationDB, timeout, params, keys);
    }

    @Override
    public boolean replicaOf(String host, int port) {
        return Objects.equals(jedis.slaveof(host, port), OK);
    }

    @Override
    public String replicaNoOne() {
        return jedis.slaveofNoOne();
    }

    @Override
    public String memoryPurge() {
        return null;
    }

    @Override
    public void close() {
        if (jedis != null) {
            jedis.close();
        }
    }
}
