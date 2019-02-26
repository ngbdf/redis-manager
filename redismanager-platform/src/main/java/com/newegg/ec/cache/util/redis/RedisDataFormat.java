package com.newegg.ec.cache.util.redis;

import com.newegg.ec.cache.core.logger.RMException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by lf52 on 2019/2/23.
 */
public class RedisDataFormat {

    private static final String SLAVE_LIST = "slaveList";

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
            throw new RMException("getMapInfo error",e);
        }
        resMap.put("detail", strInfo);
        return resMap;
    }

    public static List<Map<String, String>> dbInfo(String info) {
        List<Map<String, String>> resList = new ArrayList<>();
        if (null != info && !info.isEmpty()) {
            Map<String, String> mapInfo = getMapInfo(info);
            for (Map.Entry<String, String> db : mapInfo.entrySet()) {
                String key = db.getKey();
                if (key.contains("db")) {
                    key = key.substring(2);
                    String value = db.getValue();
                    Map<String, String> resMap = changeStrToMap(value);
                    if (null != resMap && !resMap.isEmpty()) {
                        resMap.put("db", key);
                        resList.add(resMap);
                    }
                }
            }
        }
        return resList;
    }

    public static Map<String, String> changeStrToMap(String info) {
        Map<String, String> resMap = new HashMap<>();
        if (!StringUtils.isBlank(info)) {
            String[] infoArr = info.split(",");
            for (String item : infoArr) {
                String[] temps = item.split("=");
                if (temps.length == 2) {
                    resMap.put(temps[0], temps[1]);
                }
            }
        }
        return resMap;
    }

    /**
     * 将节点信息转换为 map 类型
     *
     * @param info
     * @return
     */
    public static Map<String, Map<String,String>> changeNodesInfoMap(String info, boolean filterMaster) {
        Map<String, Map> map = new HashMap<>();
        String[] lines = info.split("\n");
        for (String line : lines) {
            String[] tmp = line.split(" ");
            if (StringUtils.contains(line, "master")) {
                String masterId = tmp[0].trim();
                Map masterMap = new HashMap<>();
                if (map.containsKey(masterId)) {
                    masterMap = map.get(masterId);
                }
                setIpAndPort(masterMap, tmp[1]);
                masterMap.put("host", masterMap.get("ip") + ":" + masterMap.get("port"));
                masterMap.put("slot", ArrayUtils.subarray(tmp, 8, tmp.length));
                masterMap.put("role", "master");
                masterMap.put("nodeId", masterId);
                masterMap.put("status", tmp[2]);
                map.put(masterId, masterMap);
            } else {
                if (!filterMaster) {
                    String masterId = tmp[3];
                    Map masterMap = new HashMap<>();
                    if (map.containsKey(masterId)) {
                        masterMap = map.get(masterId);
                    }
                    List slaveList = new ArrayList<>();
                    if (masterMap.containsKey(SLAVE_LIST)) {
                        slaveList = (List) masterMap.get(SLAVE_LIST);
                    }
                    Map slaveMap = new HashMap<>();
                    setIpAndPort(slaveMap, tmp[1]);
                    slaveMap.put("host", slaveMap.get("ip") + ":" + slaveMap.get("port"));
                    slaveMap.put("role", "slave");
                    slaveMap.put("masterId", masterId);
                    slaveMap.put("nodeId", tmp[0]);
                    slaveMap.put("status", tmp[2]);
                    slaveList.add(slaveMap);
                    masterMap.put("slaveList", slaveList);
                    map.put(masterId, masterMap);
                }
            }
        }
        Map<String, Map<String,String>> result = new HashMap<>();
        // 过滤掉非法 master
        for (Map.Entry<String, Map> item : map.entrySet()) {
            String key = item.getKey();
            Map tmp = item.getValue();
            if (tmp.containsKey("role")) {
                result.put(key, tmp);
            }
        }
        return result;
    }

    /**
     * 将 cluster info 转换成 map
     *
     * @param info
     * @return
     */
    public static Map<String, String> changeClusterInfoMap(String info) {
        Map<String, String> map = new HashMap();
        String[] lines = info.split("\n");
        for (String line : lines) {
            String[] tmp = line.split(":");
            map.put(tmp[0].trim(), tmp[1].trim());
        }
        return map;
    }


    /**
     * 通过引用设置 ip 和端口号
     *
     * @param masterMap
     * @param host
     */
    private static void setIpAndPort(Map masterMap, String host) {
        String[] tmp = host.split(":");
        if (2 == tmp.length) {
            masterMap.put("ip", tmp[0]);
            masterMap.put("port", getPort(tmp[1]));
        }
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

}
