package com.newegg.ec.cache.app.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gl49 on 2018/4/21.
 */
public class RedisMsgUtil {
    private static final String SLAVE_LIST =  "slaveList";

    private RedisMsgUtil(){
        //ignore
    }

    /**
     * 将 cluster info 转换成 map
     * @param info
     * @return
     */
    public static Map<String, String> changeClusterInfoMap(String info){
        Map<String,String> map = new HashMap();
        String[] lines = info.split("\n");
        for (String line : lines) {
            String[] tmp = line.split(":");
            map.put(tmp[0].trim(), tmp[1].trim());
        }
        return map;
    }

    /**
     * 将节点信息转换为 map 类型
     * @param info
     * @return
     */
    public static Map<String,Map> changeNodesInfoMap (String info, boolean filterMaster) {
        Map<String, Map> map = new HashMap<>();
        String[] lines = info.split("\n");
        for (String line : lines) {
            String[] tmp = line.split(" ");
            if(StringUtils.contains( line, "master")){
                String masterId = tmp[0].trim();
                Map masterMap = new HashMap<>();
                if( map.containsKey( masterId ) ){
                    masterMap = map.get( masterId );
                }
                setIpAndPort(masterMap, tmp[1]);
                masterMap.put("host", masterMap.get("ip") + ":" + masterMap.get("port") );
                masterMap.put("slot", ArrayUtils.subarray(tmp, 8, tmp.length) );
                masterMap.put("role", "master");
                masterMap.put("nodeId", masterId);
                masterMap.put("status", tmp[2]);
                map.put( masterId, masterMap );
            }else{
                if( !filterMaster ){
                    String masterId = tmp[3];
                    Map masterMap = new HashMap<>();
                    if( map.containsKey( masterId ) ){
                        masterMap = map.get( masterId );
                    }
                    List slaveList = new ArrayList<>();
                    if( masterMap.containsKey( SLAVE_LIST ) ){
                        slaveList = (List) masterMap.get( SLAVE_LIST );
                    }
                    Map slaveMap = new HashMap<>();
                    setIpAndPort(slaveMap, tmp[1]);
                    slaveMap.put("host", slaveMap.get("ip") + ":" + slaveMap.get("port") );
                    slaveMap.put("role", "slave" );
                    slaveMap.put("masterId", masterId);
                    slaveMap.put("nodeId", tmp[0] );
                    slaveMap.put("status", tmp[2]);
                    slaveList.add( slaveMap );
                    masterMap.put("slaveList", slaveList);
                    map.put( masterId, masterMap );
                }
            }
        }
        Map<String, Map> result = new HashMap<>();
        // 过滤掉非法 master
        for(Map.Entry<String, Map> item : map.entrySet()){
            String key = item.getKey();
            Map tmp = item.getValue();
            if( tmp.containsKey("role") ){
                result.put( key, tmp );
            }
        }
        return result;
    }

    public static Map<String, String> changeStrToMap(String info){
        Map<String, String> resMap = new HashMap<>();
        if( !StringUtils.isBlank( info ) ){
            String[] infoArr = info.split(",");
            for(String item : infoArr){
                String[] temps = item.split("=");
                if( temps.length == 2 ){
                    resMap.put( temps[0], temps[1] );
                }
            }
        }
        return resMap;
    }

    /**
     * 通过引用设置 ip 和端口号
     * @param masterMap
     * @param host
     */
    private static void setIpAndPort (Map masterMap, String host) {
        String[] tmp = host.split(":");
        if( 2 == tmp.length ){
            masterMap.put("ip", tmp[0]);
            masterMap.put("port", JedisUtil.getPort(tmp[1]));
        }
    }
}
