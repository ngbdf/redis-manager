package com.newegg.ec.redis.client;

import com.google.common.base.Strings;
import com.newegg.ec.redis.entity.*;
import com.newegg.ec.redis.util.RedisUtil;
import com.newegg.ec.redis.util.SignUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.params.MigrateParams;
import redis.clients.jedis.util.Slowlog;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.newegg.ec.redis.client.RedisURI.TIMEOUT;
import static com.newegg.ec.redis.util.RedisUtil.*;

/**
 * @author Jay.H.Zou
 * @date 2019/7/22
 */
public class RedisClient implements IRedisClient {
    private static final Logger logger = LoggerFactory.getLogger(RedisClient.class);
    private static final String OK = "OK";

    private static final String PONG = "PONG";

    /**
     * info subkey
     */
    public static final String SERVER = "server";

    public static final String KEYSPACE = "keyspace";

    public static final String MEMORY = "memory";

    public static final String REPLICATION = "replication";

    public static final String SENTINEL = "sentinel";

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
                close();
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
        String cursor = autoCommandParam.getCursor();
        boolean isComplete = false;
        Set<String> scanKeys = new LinkedHashSet<>();
        do {
        	ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
        	cursor = scanResult.getCursor();
        	isComplete = scanResult.isCompleteIteration();
        	scanKeys.addAll(scanResult.getResult());
        } 
        while (!"0".equals(cursor) && !isComplete);
        return scanKeys;
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
        String masterHost;
        Integer masterPort;
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
                redisNode.setFlags(NodeRole.SLAVE.getValue());
                nodeList.add(redisNode);
            }
        }
        if (!Strings.isNullOrEmpty(masterHost)) {
            RedisNode masterNode = new RedisNode();
            masterNode.setNodeRole(NodeRole.MASTER);
            masterNode.setHost(masterHost);
            masterNode.setPort(masterPort);
            masterNode.setFlags(NodeRole.MASTER.getValue());
            nodeList.add(masterNode);
        }
        return nodeList;
    }

    /**
     * <id> <ip:port> <flags> <master> <ping-sent> <pong-recv> <config-epoch> <link-state> <slot> ... <slot>
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<RedisNode> clusterNodes() throws Exception {
        String nodes = jedis.clusterNodes();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(nodes.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        String line;
        List<RedisNode> redisNodeList = new ArrayList<>();
        while ((line = bufferedReader.readLine()) != null) {
            String[] item = SignUtil.splitBySpace(line);
            String nodeId = item[0].trim();
            String ipPort = item[1];
            // noaddr 可知有此标记的节点属于无用节点
            if (line.contains("noaddr")){
                logger.warn("find a useless node: {}", line);
                continue;
            }
            Set<HostAndPort> hostAndPortSet = RedisUtil.nodesToHostAndPortSet(SignUtil.splitByAite(ipPort)[0]);
            HostAndPort hostAndPort = hostAndPortSet.iterator().next();
            String flags = item[2];
            String masterId = item[3];
            String linkState = item[7];
            RedisNode redisNode = new RedisNode(nodeId, hostAndPort.getHost(), hostAndPort.getPort(), null);
            if (flags.contains("myself")) {
                flags = flags.substring(7);
            }
            redisNode.setFlags(flags);
            redisNode.setMasterId(masterId);
            redisNode.setLinkState(linkState);
            int length = item.length;
            if (length > 8) {
                int slotNumber = 0;
                StringBuilder slotRang = new StringBuilder();
                for (int i = 8; i < length; i++) {
                    String slotRangeItem = item[i];
                    String[] startAndEnd = SignUtil.splitByMinus(slotRangeItem);
                    if (startAndEnd.length == 1) {
                        slotNumber += 1;
                    } else {
                        slotNumber += Integer.parseInt(startAndEnd[1]) - Integer.parseInt(startAndEnd[0]) + 1;
                    }
                    slotRang.append(slotRangeItem);
                    if (i < length - 1) {
                        slotRang.append(SignUtil.COMMAS);
                    }
                }
                redisNode.setSlotRange(slotRang.toString());
                redisNode.setSlotNumber(slotNumber);
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
    public List<RedisNode> sentinelNodes(Set<HostAndPort> hostAndPorts) {
        List<RedisNode> redisNodeList = new ArrayList<>(hostAndPorts.size());
        hostAndPorts.forEach(hostAndPort -> {
            RedisNode redisNode = RedisNode.masterRedisNode(hostAndPort);
            redisNode.setNodeId(hostAndPort.toString());
            redisNode.setFlags(NodeRole.MASTER.getValue());
            redisNode.setNodeRole(NodeRole.MASTER);
            redisNodeList.add(redisNode);
        });
        return redisNodeList;
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
        jedis.select(database);
        String type = type(key);
        long ttl = ttl(key);
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
                value = jedis.smembers(key);
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
            result = jedis.lindex(key, Integer.parseInt(list[2]));
        } else if (cmd.startsWith(LLEN)) {
            result = jedis.llen(key);
        } else if (cmd.startsWith(LRANGE)) {
            int start = Integer.parseInt(list[2]);
            int stop = Integer.parseInt(list[3]);
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
                count = Integer.parseInt(list[2]);
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
            int start = Integer.parseInt(param1);
            int stop = Integer.parseInt(param2);
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
    public boolean setConfig(String configKey, String configValue) {
        String result = jedis.configSet(configKey, configValue);
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
        String ping = jedis.ping();
        return !Strings.isNullOrEmpty(ping) && Objects.equals(ping.toUpperCase(), PONG);
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
    public List<Map<String, String>> getSentinelMasters() {
        return jedis.sentinelMasters();
    }

    @Override
    public List<String> getMasterAddrByName(String masterName) {
        return jedis.sentinelGetMasterAddrByName(masterName);
    }

    @Override
    public List<Map<String, String>> sentinelSlaves(String masterName) {
        return jedis.sentinelSlaves(masterName);
    }

    @Override
    public boolean monitorMaster(String masterName, String ip, int port, int quorum) {
        return Objects.equals(jedis.sentinelMonitor(masterName, ip, port, quorum), OK);
    }

    @Override
    public boolean failoverMaster(String masterName) {
        return Objects.equals(jedis.sentinelFailover(masterName), OK);
    }

    @Override
    public boolean sentinelRemove(String masterName) {
        return Objects.equals(jedis.sentinelRemove(masterName), OK);
    }

    @Override
    public Long resetConfig(String pattern) {
        return jedis.sentinelReset(pattern);
    }

    @Override
    public boolean sentinelSet(String masterName, Map<String, String> parameterMap) {
        return Objects.equals(jedis.sentinelSet(masterName, parameterMap), OK);
    }

    @Override
    public void close() {
        try {
            if (jedis != null) {
                jedis.close();
            }
        } catch (Exception ignored) {
        }
    }
    /**
     * 获取该Redis所有节点IP信息
     *
     * @return
     */
    public  Map<String, String> nodesIP(Cluster cluster) {
        Map<String, String> nodesIP = new HashMap<>();
        if ("cluster".equals(cluster.getRedisMode())) {
            nodesIP = this.clusterNodesIP(cluster);
        } else {
            nodesIP = this.standAloneNodesIP(cluster);
        }
        return nodesIP;
    }

    /**
     * 获取该stand alone Redis所有节点IP信息
     *
     * @return
     */
    public Map<String, String> standAloneNodesIP(Cluster cluster) {
        Map<String, String> nodesIPs = new HashMap<>();
        List<String> nodesList = this.standAloneRedisNodes(cluster);
        for(String node : nodesList) {
            String host = node.split(":")[0];
            nodesIPs.put(host, host);
        }
        return nodesIPs;
    }

    /**
     * 获取该RedisCluster所有节点IP信息
     *
     * @return
     */
    public Map<String, String> clusterNodesIP(Cluster cluster) {
        String redisUrl = cluster.getNodes().split(",")[0];
        String redisHost = redisUrl.split(":")[0];
        int redisPort = Integer.parseInt(redisUrl.split(":")[1]);
        JedisCluster jedisCluster = new JedisCluster(new HostAndPort(redisHost,redisPort));
        Map<String, JedisPool> nodes = jedisCluster.getClusterNodes();
        Map<String, String> clusterNodesIP = new HashMap<>();
        nodes.forEach((k, v) -> {
            String host = k.split(":")[0];
            clusterNodesIP.put(host, host);
        });
        return clusterNodesIP;
    }

    public List<String> standAloneRedisNodes(Cluster cluster) {
        String redisUrl = cluster.getNodes().split(",")[0];
        String redisHost = redisUrl.split(":")[0];
        int redisPort = Integer.parseInt(redisUrl.split(":")[1]);
        Jedis satandAloneJedis = new Jedis(redisHost,redisPort);
        if (StringUtils.isNotBlank(cluster.getRedisPassword())) {
            satandAloneJedis.auth(cluster.getRedisPassword());
        }
        Set<String> allNodes = new HashSet<>();
        allNodes.add(redisUrl);
        String res = satandAloneJedis.info("Replication");
        String[] resArr = res.split("\n");
        // master or slave
        String role = resArr[1].split(":")[1];
        if("slave".equals(role.trim())){
            String masterHost = resArr[2].split(":")[1].trim();
            String masterPort = resArr[3].split(":")[1].trim();
            allNodes.add(masterHost+":"+masterPort);
            satandAloneJedis = new Jedis(masterHost,Integer.parseInt(masterPort));
            if (StringUtils.isNotBlank(cluster.getRedisPassword())) {
                satandAloneJedis.auth(cluster.getRedisPassword());
            }
            res= satandAloneJedis.info("Replication");
            resArr = res.split("\n");
        }

        for(String str : resArr) {
            if(str.contains("ip") && str.contains("port")) {
                String[] hostAndportArr = str.split(":")[1].split(",");
                String host = hostAndportArr[0].substring(hostAndportArr[0].indexOf("=")+1);;
                String port = hostAndportArr[1].substring(hostAndportArr[1].indexOf("=")+1);

                if(!allNodes.contains(host+":"+port)) {
                    allNodes.add(host+":"+port);
                }

                if(!allNodes.contains(host+":"+redisPort)) {
                    allNodes.add(host+":"+redisPort);
                }
            }
        }
        List<String>nodes = new ArrayList<>(allNodes);
        return nodes;
    }

    public Map<String, List<String>> nodesMap(Cluster cluster) {
        Map<String, List<String>> nodes = new HashMap<>();
        if ("cluster".equals(cluster.getRedisMode())) {
            nodes = this.clusterNodesMap(cluster);
        } else {
            nodes = this.standAloneNodesMap(cluster);
        }
        return nodes;
    }

    public Map<String, List<String>> standAloneNodesMap(Cluster cluster) {
        Map<String, List<String>> nodes = new HashMap<>();
        String redisUrl = cluster.getNodes().split(",")[0];
        String redisHost = redisUrl.split(":")[0];
        int redisPort = Integer.parseInt(redisUrl.split(":")[1]);
        Jedis satandAloneJedis = new Jedis(redisHost,redisPort);
        if (StringUtils.isNotBlank(cluster.getRedisPassword())) {
            satandAloneJedis.auth(cluster.getRedisPassword());
        }
        String res = satandAloneJedis.info("Replication");
        String[] resArr = res.split("\n");
        // master or slave
        String role = resArr[1].split(":")[1];

        // slave前提下的master信息
        String masterHost = "";
        String masterPort = "";

        int flag = 0;

        if("slave".equals(role.trim())) {
            masterHost = resArr[2].split(":")[1].trim();
            masterPort = resArr[3].split(":")[1].trim();
            satandAloneJedis = new Jedis(masterHost,Integer.parseInt(masterPort));
            if (StringUtils.isNotBlank(cluster.getRedisPassword())) {
                satandAloneJedis.auth(cluster.getRedisPassword());
            }
            res= satandAloneJedis.info("Replication");
            resArr = res.split("\n");
            flag = 1;
        }
        List<String> slaves = new ArrayList<>();
        for(String str : resArr) {
            if(str.contains("ip") && str.contains("port")) {
                String[] hostAndportArr = str.split(":")[1].split(",");
                String host = hostAndportArr[0].substring(hostAndportArr[0].indexOf("=")+1);;
                String port = hostAndportArr[1].substring(hostAndportArr[1].indexOf("=")+1);
                slaves.add(host+":"+port);
                if(flag == 0) {
                    nodes.put(redisHost+":"+redisPort, slaves);
                } else {
                    nodes.put(masterHost+":"+masterPort, slaves);
                }
            }
        }
        return nodes;
    }

    /**
     * 获取该RedisCluster所有Master对应slave 信息 <k,v> k=master ip:port v=[{slave
     * ip:port},{slave ip:port}]
     *
     * @return
     */
    public Map<String, List<String>> clusterNodesMap(Cluster cluster) {
        String redisUrl = cluster.getNodes().split(",")[0];
        String redisHost = redisUrl.split(":")[0];
        int redisPort = Integer.parseInt(redisUrl.split(":")[1]);
        JedisCluster jedisCluster = new JedisCluster(new HostAndPort(redisHost,redisPort));
        Map<String, JedisPool> nodes = jedisCluster.getClusterNodes();
        Map<String, List<String>> clusterNodes = new HashMap<>();

        String nodesStr = "";
        for (String key : nodes.keySet()) {
            JedisPool jedisPool = nodes.get(key);
            Jedis jedisTemp = jedisPool.getResource();
            nodesStr = jedisTemp.clusterNodes();
            jedisTemp.close();
            break;
        }

        String[] nodesArray = nodesStr.split("\n");
        List<RedisNodeTemp> redisNodes = new ArrayList<>();

        Map<String, String> temp = new HashMap<>();
        for (String node : nodesArray) {
            if (node.indexOf("fail") > 0) {
                continue;
            }
            RedisNodeTemp redisNodeTemp = new RedisNodeTemp();
            String[] detail = node.split(" ");
            if (node.contains("master")) {
                temp.put(detail[0], detail[1]);
                redisNodeTemp.setRole(0);
                redisNodeTemp.setPid("0");
            } else {
                redisNodeTemp.setRole(1);
                redisNodeTemp.setPid(detail[3]);
            }

            redisNodeTemp.setHostAndPort(detail[1]);
            redisNodeTemp.setId(detail[0]);
            redisNodes.add(redisNodeTemp);

        }

        for (RedisNodeTemp node : redisNodes) {
            if (node.getRole() == 0) {
                continue;
            }
            if (temp.containsKey(node.getPid())) {
                String key = temp.get(node.getPid());
                if (clusterNodes.containsKey(key)) {
                    List<String> slaves = clusterNodes.get(key);
                    List<String> slaves2 = new ArrayList<>();
                    slaves.add(node.getHostAndPort());
                    key = key.split("@")[0];
                    for (int i = 0;i<slaves.size();i++){
                        slaves2.add(slaves.get(i).split("@")[0]);
                    }
                    clusterNodes.put(key, slaves2);
                } else {
                    List<String> slaves = new ArrayList<>();
                    List<String> slaves2 = new ArrayList<>();
                    slaves.add(node.getHostAndPort());
                    key = key.split("@")[0];
                    for (int i = 0;i<slaves.size();i++){
                        slaves2.add(slaves.get(i).split("@")[0]);
                    }
                    clusterNodes.put(key, slaves2);
                }
            }
        }
        return clusterNodes;
    }

    /**
     * 根据<master:slaves>获取执行分析任务ports规则
     * 即获取其中一个slave,尽量保持均衡在不同机器上
     *
     * @param clusterNodesMap
     * @return <ip:ports>
     */
    public static Map<String, Set<String>> generateAnalyzeRule(Map<String, List<String>> clusterNodesMap) {

        // 通过该map存储不同IP分配的数量，按照规则，优先分配数量最小的IP
        Map<String, Integer> staticsResult = new HashMap<>();
        Map<String, Set<String>> generateRule = new HashMap<>();

        // 此处排序是为了将slave数量最小的优先分配
        List<Map.Entry<String, List<String>>> sortList = new LinkedList<>(clusterNodesMap.entrySet());
        Collections.sort(sortList, new Comparator<Map.Entry<String, List<String>>>() {
            @Override
            public int compare(Map.Entry<String, List<String>> o1, Map.Entry<String, List<String>> o2) {
                return o1.getValue().size() - o2.getValue().size();
            }
        });

        for (Map.Entry<String, List<String>> entry : sortList) {
            List<String> slaves = entry.getValue();
            boolean isSelected = false;
            String tempPort = null;
            String tempIP = null;
            int num = 0;
            for (String slave : slaves) {
                String ip = slave.split(":")[0];
                String port = slave.split(":")[1];
                // 统计组里面不存在的IP优先分配
                if (!staticsResult.containsKey(ip)) {
                    staticsResult.put(ip, 1);
                    Set<String> generatePorts = generateRule.get(ip);
                    if (generatePorts == null) {
                        generatePorts = new HashSet<>();
                    }
                    generatePorts.add(port);
                    generateRule.put(ip, generatePorts);
                    isSelected = true;
                    break;
                } else {
                    // 此处是为了求出被使用最少的IP
                    Integer staticsNum = staticsResult.get(ip);
                    if (num == 0) {
                        num = staticsNum;
                        tempPort = port;
                        tempIP = ip;
                        continue;
                    }
                    if (staticsNum < num) {
                        tempPort = port;
                        tempIP = ip;
                        num = staticsNum;
                    }
                }

            }

            // 如果上面未分配,则选择staticsResult中数值最小的那个slave
            if (!isSelected) {
                if (slaves != null && slaves.size() > 0) {
                    if (tempPort != null) {
                        Set<String> generatePorts = generateRule.get(tempIP);
                        if (generatePorts == null) {
                            generatePorts = new HashSet<>();
                        }
                        generatePorts.add(tempPort);
                        generateRule.put(tempIP, generatePorts);
                        staticsResult.put(tempIP, staticsResult.get(tempIP) + 1);
                    }
                }
            }
        }
        return generateRule;
    }
    class RedisNodeTemp {
        private String id;
        private String pid;
        private String hostAndPort;
        // 0:master 1:slave
        private int role;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPid() {
            return pid;
        }

        public void setPid(String pid) {
            this.pid = pid;
        }

        public String getHostAndPort() {
            return hostAndPort;
        }

        public void setHostAndPort(String hostAndPort) {
            this.hostAndPort = hostAndPort;
        }

        public int getRole() {
            return role;
        }

        public void setRole(int role) {
            this.role = role;
        }
    }
}
