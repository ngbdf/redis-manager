package com.newegg.ec.cache.app.util;

import com.newegg.ec.cache.app.model.ConnectionParam;
import com.newegg.ec.cache.app.model.Host;
import com.newegg.ec.cache.app.model.RedisNode;
import com.newegg.ec.cache.app.model.RedisNodeType;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.util.Slowlog;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by gl49 on 2018/4/21.
 */
public class JedisUtil {

    public static final Log logger = LogFactory.getLog(JedisUtil.class);

    private JedisUtil() {
        //ignore
    }

    /**
     * 获取 redis info
     *
     * @param param
     * @return
     */
    public static String getInfo(ConnectionParam param) {
        String res = "";
        Jedis jedis = null;
        try {
            jedis = getJedisClient(param);
            res = jedis.info();
        } finally {
            closeJedis(jedis);
        }
        return res;
    }

    public static Map<String, String> getMapInfo(String strInfo) {
        // LinkedHashMap for db sort
        Map<String, String> resMap = new LinkedHashMap<>();
        if (StringUtils.isBlank(strInfo)) {
            return resMap;
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(strInfo.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
            String line;
            while ((line = br.readLine()) != null) {
                String[] arr = line.split(":");
                if (2 == arr.length) {
                    resMap.put(arr[0], arr[1]);
                }
            }
        } catch (IOException e) {
            logger.error("getMapInfo error",e);
        }
        resMap.put("detail", strInfo);
        return resMap;
    }

    public static Map<String, String> getMapInfo(ConnectionParam param) {
        String strInfo = JedisUtil.getInfo(param);
        return getMapInfo(strInfo);
    }

    public static Map<String, String> getRedisConfig(ConnectionParam param) {
        Jedis jedis = getJedisClient(param);
        Map<String, String> configMap = new HashMap();
        try {
            List<String> list = jedis.configGet("*");
            String field = "";
            for (String item : list) {
                if ("".equals(field)) {
                    field = item;
                } else {
                    configMap.put(field, item);
                    field = "";
                }
            }
        } catch (Exception e) {
            logger.error("getRedisConfig error",e);
        } finally {
            closeJedis(jedis);
        }
        return configMap;
    }

    public static int getRedisVersion(ConnectionParam param) {
        int res = 0;
        Map<String, String> resMap = getMapInfo(param);
        if (null != resMap && !resMap.isEmpty()) {
            String model = resMap.get("redis_mode");
            if (model.equals("cluster")) {
                res = 3;
            } else {
                res = 2;
            }
        }
        return res;
    }

    public static List<Map<String, String>> dbInfo(ConnectionParam param) {
        String info = getInfo(param);
        List<Map<String, String>> dbInfo = dbInfo(info);

        //fix bug on query mode when first create cluster
        if(dbInfo.size() == 0){
            Map<String, Map> nodes = JedisUtil.getMasterNodes(param);
            for (Map.Entry<String, Map> node : nodes.entrySet()) {
                Map map = node.getValue();
                List<Map<String, String>> templist = dbInfo(JedisUtil.getInfo(new ConnectionParam((String) map.get("ip"), (Integer) map.get("port"), param.getRedisPassword())));
                if(templist.size() > 0) {
                    return templist;
                }
            }

            Map<String, String> resMap = new HashMap<>();
            resMap.put("expires","0");
            resMap.put("keys","1");
            resMap.put("db","0");
            resMap.put("avg_ttl","0");
            dbInfo.add(resMap);
        }
        return dbInfo;
    }

    public static List<Map<String, String>> dbInfo(String info) {
        List<Map<String, String>> resList = new ArrayList<>();
        if (null != info && !info.isEmpty()) {
            Map<String, String> mapInfo = getMapInfo(info);
            for (Map.Entry<String, String> db : mapInfo.entrySet()) {
                String key = db.getKey();
                if (key.startsWith("db")) {
                    key = key.substring(2);
                    String value = db.getValue();
                    Map<String, String> resMap = RedisMsgUtil.changeStrToMap(value);
                    if (null != resMap && !resMap.isEmpty()) {
                        resMap.put("db", key);
                        resList.add(resMap);
                    }
                }
            }
        }
        return resList;
    }

    public static Map<Integer, Integer> dbMap(ConnectionParam param) {
        Map<Integer, Integer> resMap = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        Map<String, String> infoMap = getMapInfo(param);
        if (null != infoMap && !infoMap.isEmpty()) {
            for (Map.Entry<String, String> item : infoMap.entrySet()) {
                String key = item.getKey();
                String value = item.getValue();
                if (key.substring(0, 2).equals("db")) {
                    String[] tmps = value.split(",");
                    int dbkeys = 0;
                    if (tmps.length > 0) {
                        String[] tempArr = tmps[0].split("=");
                        dbkeys = Integer.parseInt(tempArr[1]);
                        resMap.put(getDbIndex(key), dbkeys);
                    }
                }
            }
        }
        return resMap;
    }


    public static int getDbIndex(String db) {
        int index = 0;
        if (db.contains("db")) {
            String tmp = db.substring(2);
            index = Integer.parseInt(tmp);
        } else {
            index = Integer.parseInt(db);
        }
        return index;
    }

    public static int dbSize(ConnectionParam param) {
        Map<Integer, Integer> alldb = dbMap(param);
        int size = 0;
        if (null != alldb && !alldb.isEmpty()) {
            for (Map.Entry<Integer, Integer> item : alldb.entrySet()) {
                int value = item.getValue();
                size += value;
            }
        }
        return size;
    }


    /**
     * 获取集群信息
     *
     * @param param
     * @return
     */
    public static Map<String, String> getClusterInfo(ConnectionParam param) {
        Jedis jedis = getJedisClient(param);
        Map<String, String> result = new HashMap<>();
        try {
            int version = JedisUtil.getRedisVersion(param);
            if (version == 2) {
                List nodeList = nodeList(param);
                result.put("cluster_state", "ok");
                result.put("cluster_size", String.valueOf(1));
                result.put("cluster_known_nodes", String.valueOf(nodeList.size()));
            } else {
                String info = jedis.clusterInfo();
                result = RedisMsgUtil.changeClusterInfoMap(info);
            }

            // add by jay
            String info = jedis.info();
            Map<String, String> mapInfo = getMapInfo(info);
            result.putAll(mapInfo);
        } catch (Exception e) {
            logger.error("getClusterInfo error", e);
        } finally {
            jedis.close();
        }
        return result;
    }

    public static List<Map<String, String>> nodeList(ConnectionParam param) {
        List<Map<String, String>> nodeList = new ArrayList<>();
        if (JedisUtil.getRedisVersion(param) > 2) {
            nodeList = JedisUtil.getNodeList(param);
        } else {
            nodeList = JedisUtil.getRedis2Nodes(param);
        }
        return nodeList;
    }

    public static List<Map<String, String>> getNodeList(ConnectionParam param) {
        return getNodeList(param, false);
    }

    /**
     * 获取节点列表
     *
     * @param param
     * @return
     */
    public static List<Map<String, String>> getNodeList(ConnectionParam param, boolean filterMaster) {
        Map<String, Map> result = new HashMap<>();
        if (filterMaster) {
            result = JedisUtil.getMasterNodes(param);
        } else {
            result = JedisUtil.getClusterNodes(param);
        }

        List<Map<String, String>> nodeList = new ArrayList<>();
        for (Map.Entry<String, Map> entry : result.entrySet()) {
            Map node = entry.getValue();
            Map temp = new HashMap<>();
            temp.put("ip", node.get("ip"));
            temp.put("port", String.valueOf(getPort(String.valueOf(node.get("port")))));
            temp.put("role", "master");
            nodeList.add(temp);

            List<Map<String, String>> slaveList = (List<Map<String, String>>) node.get("slaveList");
            if (null != slaveList && !slaveList.isEmpty()) {
                for (int j = 0; j < slaveList.size(); j++) {
                    String slaveIp = slaveList.get(j).get("ip");
                    String slavePort = String.valueOf(getPort(String.valueOf(slaveList.get(j).get("port"))));
                    Map temp2 = new HashMap<>();
                    temp2.put("ip", slaveIp);
                    temp2.put("port", slavePort);
                    temp2.put("role", "slave");
                    nodeList.add(temp2);
                }
            }
        }
        return nodeList;
    }

    public static List<Map<String, String>> getRedis2Nodes(ConnectionParam param) {
        List<Map<String, String>> resList = new ArrayList<>();
        String ip = param.getIp();
        int port = param.getPort();
        Map<String, String> infoMap = getMapInfo(param);
        if (infoMap.get("role").equals("slave")) {
            ip = infoMap.get("master_host");
            port = Integer.parseInt(infoMap.get("master_port"));
            param.setIp(ip);
            param.setPort(port);
            infoMap = getMapInfo(param);
        }
        for (Map.Entry<String, String> item : infoMap.entrySet()) {
            String key = item.getKey();
            String value = item.getValue();
            Map<String, String> node = new HashMap<>();
            if (key.contains("slave")) {
                String[] tmpArr = value.split(",");
                String tmpip = null;
                String tmpport = null;
                for (String tmp : tmpArr) {
                    String[] tmpInnArr = tmp.split("=");
                    if (tmpInnArr[0].equals("ip")) {
                        tmpip = tmpInnArr[1];
                    } else if (tmpInnArr[0].equals("port")) {
                        tmpport = tmpInnArr[1];
                    }
                }
                if (!StringUtils.isBlank(tmpip) && !StringUtils.isBlank(tmpport)) {
                    node.put("ip", tmpip);
                    node.put("port", tmpport);
                    node.put("role", "slave");
                }
            }
            if (null != node && !node.isEmpty()) {
                resList.add(node);
            }
        }
        Map<String, String> master = new HashMap<>();
        master.put("ip", ip);
        master.put("port", String.valueOf(port));
        master.put("role", "master");
        resList.add(master);
        return resList;
    }

    /**
     * 获取集群节点信息
     *
     * @param param
     * @return
     */
    public static Map<String, Map> getClusterNodes(ConnectionParam param) {
        Jedis jedis = getJedisClient(param);
        Map<String, Map> result = new HashMap<>();
        try {
            String info = jedis.clusterNodes();
            result = RedisMsgUtil.changeNodesInfoMap(info, false);
        } catch (Exception e) {
            if(!(e instanceof JedisDataException && "ERR This instance has cluster support disabled".equals(e.getMessage()))){
                logger.error("getClusterNodes error", e);
            }
        } finally {
            closeJedis(jedis);
        }
        return result;
    }

    public static Map<String, Map> getMasterNodes(ConnectionParam param) {
        Jedis jedis = getJedisClient(param);
        Map<String, Map> result = new HashMap<>();
        try {
            String info = jedis.clusterNodes();
            result = RedisMsgUtil.changeNodesInfoMap(info, true);
        } catch (Exception e) {
            logger.error("getMasterNodes error", e);
        } finally {
            jedis.close();
        }
        return result;
    }

    public static String getNodeId(ConnectionParam param) {
        Jedis jedis = getJedisClient(param);
        String nodeid = null;
        try {
            String info = jedis.clusterNodes();
            String[] lines = info.split("\n");
            for (String line : lines) {
                if (line.contains("myself")) {
                    String[] tmps = line.split(" ");
                    nodeid = tmps[0];
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("getNodeid error", e);
        } finally {
            closeJedis(jedis);
        }
        return nodeid;
    }

    /**
     * 发送 failover 信号
     *
     * @param param
     * @return
     */
    private static String clusterFailover(ConnectionParam param) {
        Jedis jedis = getJedisClient(param);
        String result = "";
        try {
            result = jedis.clusterFailover();
        } catch (Exception e) {
            logger.error("clusterFailover error", e);
        } finally {
            closeJedis(jedis);
        }
        return result;
    }

    public static int getPort(String port) {
        String strPort;
        if (port.contains("@")) {
            String[] tmp = port.split("@");
            strPort = tmp[0];
        } else {
            strPort = port;
        }
        return Integer.parseInt(strPort);
    }

    public static List<Slowlog> getSlowLog(ConnectionParam param, long size) {
        Jedis jedis = getJedisClient(param);
        List<Slowlog> list = new ArrayList((int) size);
        try {
            list = jedis.slowlogGet(size);
        } finally {
            closeJedis(jedis);
        }
        return list;
    }

    public static Set<String> getIPList(String nodeStr) {
        Set<String> ipSet = new HashSet<>();
        String[] nodeArr = nodeStr.split("\n");
        for (String node : nodeArr) {
            try {
                String[] tmpArr = node.split("\\s+");
                String host = tmpArr[0];
                String ip = host.split(":")[0];
                ipSet.add(ip);
            } catch (Exception e) {
                logger.error("getIPList error", e);
            }
        }
        return ipSet;
    }

    public static Map<RedisNode, List<RedisNode>> getInstallNodeMap(String nodeStr) {
        Map<RedisNode, List<RedisNode>> resMap = new HashedMap();
        RedisNode currentMaster = new RedisNode();
        String[] nodeArr = nodeStr.split("\n");
        for (String node : nodeArr) {
            try {
                RedisNode nodeItem = new RedisNode();
                String[] tmpArr = node.split("\\s+");
                if (tmpArr.length >= 1) {
                    String hostStr = tmpArr[0];
                    Host host = NetUtil.getHost(hostStr);
                    nodeItem.setIp(host.getIp());
                    nodeItem.setPort(host.getPort());
                    nodeItem.setRole(RedisNodeType.slave);
                }
                if (tmpArr.length >= 2) {
                    if (tmpArr[1].equals("master")) {
                        nodeItem.setRole(RedisNodeType.master);
                        currentMaster = nodeItem;
                    }
                }
                if (StringUtils.isBlank(currentMaster.getIp())) {
                    currentMaster = nodeItem;
                    currentMaster.setRole(RedisNodeType.master);
                }
                if (resMap.get(currentMaster) == null) {
                    resMap.put(currentMaster, new ArrayList<>());
                }
                List<RedisNode> slaveList = resMap.get(currentMaster);
                if (nodeItem.getRole() == RedisNodeType.slave) {
                    slaveList.add(nodeItem);
                }
            } catch (Exception e) {
                logger.error("getInstallNodeMap error", e);
            }
        }
        return resMap;
    }

    public static List<RedisNode> getInstallNodeList(String nodeStr) {

        List<RedisNode> list = new LinkedList();
        String[] nodeArr = nodeStr.split("\n");
        for (String node : nodeArr) {
            RedisNode nodeItem = new RedisNode();
            String[] tmpArr = node.split("\\s+");
            String hostStr = tmpArr[0];
            Host host = NetUtil.getHost(hostStr);
            nodeItem.setIp(host.getIp());
            nodeItem.setPort(host.getPort());
            if (tmpArr.length >= 1) {
                nodeItem.setRole(RedisNodeType.slave);
            }
            if (tmpArr.length >= 2) {
                if (tmpArr[1].equals("master")) {
                    nodeItem.setRole(RedisNodeType.master);
                }
            }
            list.add(nodeItem);
        }
        return list;
    }

    public static Jedis getJedisClient(ConnectionParam param) {
        Jedis jedis = new Jedis(param.getIp(), param.getPort());
        String password = param.getRedisPassword();
        if (StringUtils.isNotBlank(password)) {
            jedis.auth(password);
        }
        return jedis;
    }

    public static void closeJedis(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }
}
