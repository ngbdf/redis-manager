package com.newegg.ec.cache.core.utils.redis;

import com.newegg.ec.cache.core.entity.redis.RedisConnectParam;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by lf52 on 2019/2/23.
 *
 * redis 部分常用操作封装，屏蔽下层redis version的差异
 */
public class RedisUtils {

    private static RedisClient client;

    public static Map<String, String> getInfo(RedisConnectParam param) {
        client = RedisClient.create(buildRedisURI(param));
        return RedisDataFormat.getMapInfo(client.connect().sync().info());
    }

    public static List<Map<String, String>> getAllNodes(RedisConnectParam param) {

        List<Map<String, String>> nodeList = new ArrayList<>();

        if ("cluster".equals(getRedisMode(param))) {
            nodeList = getRedisClusterNodes(param,false);
        } else if("standalone".equals(getRedisMode(param))) {
            nodeList = getRedisStandAloneNodes(param);
        }else {
             //todo
            throw  new RuntimeException("invalid redis mode type");
        }
        return nodeList;
    }


    public static List<Map<String, String>> getRedisStandAloneNodes(RedisConnectParam param) {
        List<Map<String, String>> resList = new ArrayList<>();
        String ip = param.getIp();
        int port = param.getPort();
        Map<String, String> infoMap = getInfo(param);
        if (infoMap.get("role").equals("slave")) {
            ip = infoMap.get("master_host");
            port = Integer.parseInt(infoMap.get("master_port"));
            param.setIp(ip);
            param.setPort(port);
            infoMap = getInfo(param);
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

    public static List<Map<String, String>> redisDBInfo(RedisConnectParam param) {
        List<Map<String, String>> resList = new ArrayList<>();
        Map<String, String> mapInfo = getInfo(param);
        for (Map.Entry<String, String> db : mapInfo.entrySet()) {
            String key = db.getKey();
            if (key.contains("db")) {
                key = key.substring(2);
                String value = db.getValue();
                Map<String, String> resMap = RedisDataFormat.changeStrToMap(value);
                if (null != resMap && !resMap.isEmpty()) {
                    resMap.put("db", key);
                    resList.add(resMap);
                }
            }
        }
        return resList;
    }

    public static Map<String, String> getRedisConfig(RedisConnectParam param) {
        client = RedisClient.create(buildRedisURI(param));
        return client.connect().sync().configGet("*");
    }

    /**
     * 区分redis是cluster模式还是standalone模式
     * standalone cluster
     * @param param
     * @return
     */
    public static String getRedisMode(RedisConnectParam param) {
        Map<String, String> resMap = getInfo(param);
        if (null != resMap && !resMap.isEmpty()) {
            return resMap.get("redis_mode");
        }
        return "";
    }

    public static Map<Integer, Integer> dbMap(RedisConnectParam param) {
        Map<Integer, Integer> resMap = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        Map<String, String> infoMap = getInfo(param);
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

    public static int dbSize(RedisConnectParam param) {
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

    public static Map<String, String> getClusterInfo(RedisConnectParam param) {

        Map<String, String> result = new HashMap<>();
        String version = getRedisMode(param);
        if ("cluster".equals(version)) {
            client = RedisClient.create(buildRedisURI(param));
            result =  RedisDataFormat.changeClusterInfoMap(client.connect().sync().clusterInfo());
        } else {
            result.put("cluster_state", "ok");
            result.put("cluster_size", String.valueOf(1));
            result.put("cluster_known_nodes", String.valueOf(getRedisStandAloneNodes(param).size()));
        }
        return result;
    }

    private static List<Map<String, String>> getRedisClusterNodes(RedisConnectParam param,boolean filterMaster) {

        Map<String, Map<String, String>> result  = RedisDataFormat.changeNodesInfoMap(client.connect().sync().clusterNodes(), filterMaster);
        List<Map<String, String>> nodeList = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> entry : result.entrySet()) {
            Map node = entry.getValue();
            Map temp = new HashMap<>();
            temp.put("ip", node.get("ip"));
            temp.put("port", String.valueOf(RedisDataFormat.getPort(String.valueOf(node.get("port")))));
            temp.put("role", "master");
            nodeList.add(temp);

            List<Map<String, String>> slaveList = (List<Map<String, String>>) node.get("slaveList");
            if (null != slaveList && !slaveList.isEmpty()) {
                for (int j = 0; j < slaveList.size(); j++) {
                    String slaveIp = slaveList.get(j).get("ip");
                    String slavePort = String.valueOf(RedisDataFormat.getPort(String.valueOf(slaveList.get(j).get("port"))));
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

    public static String getNodeId(RedisConnectParam param) {

        String nodeId = "";
        if ("cluster".equals(getRedisMode(param))) {
            client = RedisClient.create(buildRedisURI(param));
            String info = client.connect().sync().clusterNodes();
            String[] lines = info.split("\n");
            for (String line : lines) {
                if (line.contains("myself")) {
                    String[] tmps = line.split(" ");
                    nodeId = tmps[0];
                    break;
                }
            }
        } else if("standalone".equals(getRedisMode(param))) {
            //todo
            throw new RuntimeException("redis cannot support this command");
        }
        return nodeId;
    }

    public static List<Object> getSlowLog(RedisConnectParam param, int size) {
        client = RedisClient.create(buildRedisURI(param));
        List<Object> slowLog = client.connect().sync().slowlogGet(size);
        return slowLog;
    }


    private static RedisURI buildRedisURI(RedisConnectParam param) {
        if(StringUtils.isNotBlank(param.getRedisPassword())){
            return RedisURI.Builder.redis(param.getIp(), param.getPort()).withPassword(param.getRedisPassword()).build();
        }
        return RedisURI.Builder.redis(param.getIp(), param.getPort()).build();
    }
}
