package com.newegg.ec.cache.app.util;

import com.newegg.ec.cache.app.model.Host;
import com.newegg.ec.cache.app.model.RedisNode;
import com.newegg.ec.cache.app.model.RedisNodeType;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
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
    private JedisUtil(){
        //ignore
    }

    /**
     * 获取 redis info
     * @param ip
     * @param port
     * @return
     */
    public static String getInfo(String ip, int port){
        String res = "";
        Jedis jedis = null;
        try {
            jedis = new Jedis( ip, port);
            res = jedis.info();
        }finally {
            jedis.close();
        }
        return res;
    }

    public static Map<String, String> getMapInfo(String strInfo){
        Map<String, String> resMap = new HashMap<>();
        if( StringUtils.isBlank( strInfo ) ){
            return resMap;
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(strInfo.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
            String line;
            while ( (line = br.readLine()) != null ) {
                String[] arr = line.split(":");
                if(2 == arr.length){
                    resMap.put( arr[0], arr[1] );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resMap.put("detail", strInfo);
        return resMap;
    }

    public static Map<String, String> getMapInfo(String ip,int port){
        String strInfo = JedisUtil.getInfo(ip, port);
        return  getMapInfo(strInfo);
    }

    public static Map<String, String> getRedisConfig(String ip, int port){
        Jedis jedis = new Jedis( ip, port);
        Map<String, String> configMap = new HashMap();
        try {
            List<String> list = jedis.configGet("*");
            String field = "";
            for(String item : list){
                if( "".equals(field) ){
                    field = item;
                }else{
                    configMap.put( field, item );
                    field = "";
                }
            }
        }catch ( Exception e ){

        }finally {
            jedis.close();
        }
        return configMap;
    }

    public static int getRedisVersion(String ip, int port){
        int res = 0;
        Map<String, String> resMap = getMapInfo(ip, port);
        if( null != resMap && !resMap.isEmpty() ){
            String model = resMap.get("redis_mode");
            if( model.equals("cluster") ){
                res = 3;
            }else{
                res = 2;
            }
        }
        return res;
    }

    public static List<Map<String, String>> dbInfo(String ip, int port){
        String info = getInfo(ip, port);
        return  dbInfo(info);
    }
    public static List<Map<String, String>> dbInfo(String info){
        List<Map<String, String>> resList = new ArrayList<>();
        if( null != info && !info.isEmpty() ){
            Map<String, String> mapInfo = getMapInfo( info );
            for(Map.Entry<String, String> db : mapInfo.entrySet()){
                String key = db.getKey();
                if( key.contains("db") ){
                    key =  key.substring(2);
                    String value = db.getValue();
                    Map<String, String> resMap = RedisMsgUtil.changeStrToMap( value );
                    if( null != resMap && !resMap.isEmpty() ){
                        resMap.put("db", key);
                        resList.add(resMap);
                    }
                }
            }
        }
        return resList;
    }

    public static Map<Integer, Integer> dbMap(String ip, int port){
        Map<Integer, Integer> resMap = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        Map<String, String> infoMap = getMapInfo(ip, port);
        if( null != infoMap && !infoMap.isEmpty() ){
            for(Map.Entry<String, String> item : infoMap.entrySet()){
                String key = item.getKey();
                String value = item.getValue();
                if( key.substring(0, 2).equals("db") ){
                    String[] tmps = value.split(",");
                    int dbkeys = 0;
                    if( tmps.length > 0 ){
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
        if( db.contains("db") ){
            String tmp = db.substring(2);
            index = Integer.parseInt(tmp);
        }else{
            index = Integer.parseInt(db);
        }
        return index;
    }

    public static int dbSize(String ip, int port){
        Map<Integer, Integer> alldb = dbMap(ip, port);
        int size = 0;
        if( null != alldb && !alldb.isEmpty() ){
            for(Map.Entry<Integer ,Integer> item : alldb.entrySet()){
                int value = item.getValue();
                size += value;
            }
        }
        return size;
    }


    /**
     * 获取集群信息
     * @param ip
     * @param port
     * @return
     */
    public static Map<String, String> getClusterInfo(String ip, int port){
        Jedis jedis = new Jedis( ip, port);
        Map<String, String> result = new HashMap<>();
        try {
            int version = JedisUtil.getRedisVersion(ip, port);
            if( version == 2 ){
                List nodeList = nodeList(ip, port);
                result.put("cluster_state", "ok");
                result.put("cluster_size", String.valueOf(1));
                result.put("cluster_known_nodes", String.valueOf(nodeList.size()));
            }else{
                String info  = jedis.clusterInfo();
                result = RedisMsgUtil.changeClusterInfoMap( info );
            }
        }catch ( Exception e ){
            e.printStackTrace();
        }finally {
            jedis.close();
        }
        return result;
    }

    public static List<Map<String, String>> nodeList (String ip, int port){
        List<Map<String, String>> nodeList = new ArrayList<>();
        if( JedisUtil.getRedisVersion(ip, port) > 2 ){
            nodeList = JedisUtil.getNodeList(ip, port);
        }else{
            nodeList = JedisUtil.getRedis2Nodes(ip, port);
        }
        return nodeList;
    }

    public static List<Map<String, String>> getNodeList(String ip, int port) {
        return getNodeList (ip, port, false);
    }

    /**
     * 获取节点列表
     * @param ip
     * @param port
     * @return
     */
    public static List<Map<String, String>> getNodeList(String ip, int port, boolean filterMaster) {
        Map<String, Map> result = new HashMap<>();
        if( filterMaster ){
            result = JedisUtil.getMasterNodes(ip, port);
        }else{
            result = JedisUtil.getClusterNodes(ip, port);
        }

        List<Map<String, String>> nodeList = new ArrayList<>();
        for(Map.Entry<String, Map> entry : result.entrySet()){
            Map node = entry.getValue();
            Map temp = new HashMap<>();
            temp.put("ip", node.get("ip"));
            temp.put("port", String.valueOf( getPort(String.valueOf(node.get("port")))) );
            temp.put("role", "master");
            nodeList.add( temp );

            List<Map<String, String>> slaveList = (List<Map<String, String>>) node.get("slaveList");
            if( null != slaveList && !slaveList.isEmpty()){
                for(int j = 0; j < slaveList.size(); j++){
                    String slaveIp = slaveList.get(j).get("ip");
                    String slavePort = String.valueOf(getPort( String.valueOf(slaveList.get(j).get("port"))));
                    Map temp2 = new HashMap<>();
                    temp2.put("ip", slaveIp);
                    temp2.put("port", slavePort);
                    temp2.put("role", "slave");
                    nodeList.add( temp2 );
                }
            }
        }
        return nodeList;
    }

    public static List<Map<String, String>> getRedis2Nodes(String ip, int port){
        List<Map<String, String>> resList = new ArrayList<>();
        Map<String, String> infoMap = getMapInfo(ip, port);
        if( infoMap.get("role").equals("slave") ){
            ip = infoMap.get("master_host");
            port = Integer.parseInt(infoMap.get("master_port"));
            infoMap = getMapInfo(ip, port);
        }
        for(Map.Entry<String, String> item: infoMap.entrySet()){
            String key = item.getKey();
            String value = item.getValue();
            Map<String, String> node = new HashMap<>();
            if( key.contains("slave") ){
                String[] tmpArr = value.split(",");
                String tmpip = null;
                String tmpport = null;
                for(String tmp: tmpArr){
                    String[] tmpInnArr = tmp.split("=");
                    if( tmpInnArr[0].equals("ip") ){
                        tmpip = tmpInnArr[1];
                    }else if( tmpInnArr[0].equals("port") ){
                        tmpport = tmpInnArr[1];
                    }
                }
                if( !StringUtils.isBlank( tmpip ) && !StringUtils.isBlank(tmpport) ){
                    node.put("ip", tmpip);
                    node.put("port", tmpport);
                    node.put("role", "slave");
                }
            }
            if( null != node && !node.isEmpty() ){
                resList.add( node );
            }
        }
        Map<String, String> master = new HashMap<>();
        master.put("ip", ip);
        master.put("port", String.valueOf(port));
        master.put("role", "master");
        resList.add( master );
        return resList;
    }

    /**
     * 获取集群节点信息
     * @param ip
     * @param port
     * @return
     */
    public static Map<String,Map> getClusterNodes (String ip, int port) {
        Jedis jedis = new Jedis( ip, port);
        Map<String, Map> result = new HashMap<>();
        try {
            String info = jedis.clusterNodes();
            result = RedisMsgUtil.changeNodesInfoMap(info, false);
        }catch ( Exception e ){
            e.printStackTrace();
        }finally {
            jedis.close();
        }
        return result;
    }

    public static Map<String,Map> getMasterNodes (String ip, int port) {
        Jedis jedis = new Jedis( ip, port);
        Map<String, Map> result = new HashMap<>();
        try {
            String info = jedis.clusterNodes();
            result = RedisMsgUtil.changeNodesInfoMap(info, true);
        }catch ( Exception e ){
            e.printStackTrace();
        }finally {
            jedis.close();
        }
        return result;
    }

    public static String getNodeid(String ip, int port){
        Jedis jedis = new Jedis( ip , port);
        String nodeid = null;
        try {
            String info = jedis.clusterNodes();
            String[] lines = info.split("\n");
            for(String line : lines){
                if( line.contains("myself") ){
                    System.out.println( line );
                    String[] tmps = line.split(" ");
                    nodeid = tmps[0];
                    break;
                }
            }
        }catch (Exception e){

        }finally {
            jedis.close();
        }
        return nodeid;
    }

    /**
     * 发送 failover 信号
     * @param ip
     * @param port
     * @return
     */
    private static String clusterFailover (String ip, int port) {
        Jedis jedis = new Jedis( ip, port);
        String result = "";
        try {
            result = jedis.clusterFailover();
        }catch ( Exception e ){
            e.printStackTrace();
        }finally {
            jedis.close();
        }
        return result;
    }

    public static int getPort(String port){
        String strPort;
        if( port.contains("@") ){
            String[] tmp = port.split("@");
            strPort = tmp[0];
        }else{
            strPort = port;
        }
        return Integer.parseInt( strPort );
    }

    public static List<Slowlog> getSlowLog(String ip, int port,long size) {
        Jedis jedis = new Jedis( ip, port);
        List<Slowlog> list = new ArrayList((int)size);
        try {
            list = jedis.slowlogGet(size);
        }finally {
            jedis.close();
        }
        return list;
    }

    public static Set<String> getIPList(String nodeStr){
        Set<String> ipSet = new HashSet<>();
        String[] nodeArr = nodeStr.split("\n");
        for(String node : nodeArr){
            try {
                String[] tmpArr = node.split("\\s+");
                String host = tmpArr[0];
                String ip = host.split(":")[0];
                ipSet.add( ip );
            }catch (Exception ignore){

            }
        }
        return ipSet;
    }
    public static Map<RedisNode, List<RedisNode>> getInstallNodeMap(String nodeStr){
        Map<RedisNode, List<RedisNode>> resMap = new HashedMap();
        RedisNode currentMaster = new RedisNode();
        String[] nodeArr = nodeStr.split("\n");
        for(String node : nodeArr){
            try {
                RedisNode nodeItem = new RedisNode();
                String[] tmpArr = node.split("\\s+");
                if( tmpArr.length >= 1 ){
                    String hostStr = tmpArr[0];
                    Host host = NetUtil.getHost( hostStr );
                    nodeItem.setIp( host.getIp() );
                    nodeItem.setPort( host.getPort() );
                    nodeItem.setRole( RedisNodeType.slave );
                }
                if( tmpArr.length >= 2 ){
                    if( tmpArr[1].equals("master") ){
                        nodeItem.setRole( RedisNodeType.master );
                        currentMaster = nodeItem;
                    }
                }
                if( StringUtils.isBlank( currentMaster.getIp() ) ){
                    currentMaster = nodeItem;
                    currentMaster.setRole( RedisNodeType.master );
                }
                if( resMap.get(currentMaster) == null ){
                    resMap.put(currentMaster, new ArrayList<>());
                }
                List<RedisNode> slaveList = resMap.get(currentMaster);
                if( nodeItem.getRole() == RedisNodeType.slave ){
                    slaveList.add(nodeItem);
                }
            }catch (Exception ignore){

            }
        }
        return resMap;
    }

    public static  List<RedisNode> getInstallNodeList(String nodeStr){

        List<RedisNode> list = new LinkedList();
        String[] nodeArr = nodeStr.split("\n");
        for(String node : nodeArr){
                RedisNode nodeItem = new RedisNode();
                String[] tmpArr = node.split("\\s+");
                String hostStr = tmpArr[0];
                Host host = NetUtil.getHost( hostStr );
                nodeItem.setIp( host.getIp() );
                nodeItem.setPort(host.getPort());
                if( tmpArr.length >= 1 ){
                    nodeItem.setRole(RedisNodeType.slave);
                }
                if( tmpArr.length >= 2 ){
                    if( tmpArr[1].equals("master") ){
                        nodeItem.setRole(RedisNodeType.master);
                    }
                }
                list.add(nodeItem);
        }
        return list;
    }

}
