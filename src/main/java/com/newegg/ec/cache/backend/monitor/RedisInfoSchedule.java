package com.newegg.ec.cache.backend.monitor;

import com.newegg.ec.cache.app.dao.IClusterDao;
import com.newegg.ec.cache.app.dao.INodeInfoDao;
import com.newegg.ec.cache.app.model.Cluster;
import com.newegg.ec.cache.app.model.Common;
import com.newegg.ec.cache.app.model.Host;
import com.newegg.ec.cache.app.model.NodeInfo;
import com.newegg.ec.cache.app.util.DateUtil;
import com.newegg.ec.cache.app.util.JedisUtil;
import com.newegg.ec.cache.app.util.NetUtil;
import com.newegg.ec.cache.core.logger.CommonLogger;
import com.newegg.ec.cache.core.mysql.MysqlField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lzz on 2018/4/20.
 */
@Component
public class RedisInfoSchedule{
    public static CommonLogger logger = new CommonLogger( RedisInfoSchedule.class );
    private static final int JEDIS_TIMEOUT = 1000;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(200);

    @Resource
    private IClusterDao clusterDao;
    @Resource
    private INodeInfoDao infoDao;

    /**
     * 两分钟后开始采集，以后一分钟采集一次
     */
    @Scheduled(fixedRate = 1000 * 60, initialDelay = 1000 * 120)
    public void scheduledMetricRedisInfo() {
        List<Cluster>  clusterList = clusterDao.getClusterList(null);
        for (Cluster cluster: clusterList) {
            try {
                String address = cluster.getAddress();
                int clusterId = cluster.getId();
                Host host = NetUtil.getHostPassAddress( address );
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        infoProducer(clusterId, host.getIp(), host.getPort());
                    }
                });
            }catch (Exception e){
                //
            }
        }
    }

    public static NodeInfo getNodeMonitorInfo(String strInfo) {
        NodeInfo o = null;
        try {
            o = new NodeInfo();
            Field[] fields=o.getClass().getDeclaredFields();
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(strInfo.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
            String line;
            while ( (line = br.readLine()) != null ) {
                String[] arr = line.split(":");
                if( arr.length == 2 ){
                    if( arr[0].contains("db") ){
                        continue;
                    }
                    for(Field f : fields){
                        String fieldName = f.getName();
                        f.setAccessible(true);
                        MysqlField mysqlField = f.getAnnotation(MysqlField.class);
                        if( mysqlField != null ){
                            fieldName = mysqlField.field();
                        }
                        if( arr[0].equals( fieldName ) ){
                            String type = f.getGenericType().toString();
                            if( type.equals("class java.lang.String") ){
                                f.set( o, String.valueOf(arr[1]) );
                            }else if( "int".equals(type)){
                                f.set( o, Integer.valueOf(arr[1]) );
                            }else if( "long".equals(type)){
                                f.set( o, Long.valueOf(arr[1]) );
                            }else if( "float".equals( type ) ){
                                f.set( o, Float.valueOf(arr[1]) );
                            }
                        }
                    }
                }
            }
            processDb(strInfo, o);
        }catch (Exception e){

        }
        return o;
    }

    private static void processDb(String strInfo, NodeInfo o) {
        List<Map<String, String>> resDb = JedisUtil.dbInfo( strInfo );
        long keys = 0;
        long avg_ttl = 0;
        long expires = 0;
        int size = resDb.size();
        for(int i = 0; i < size; i++){
            Map<String , String> item = resDb.get( i );
            long tempKeys = Long.parseLong(item.get("keys"));
            long tempAvgTTl = Long.parseLong(item.get("avg_ttl"));
            long tempExpires = Long.parseLong(item.get("expires"));
            keys += tempKeys;
            avg_ttl += tempAvgTTl;
            expires += tempExpires;
        }
        if( size > 0 ){
            avg_ttl = avg_ttl / size;
            expires = expires / size;
        }
        o.setTotalKeys( keys );
        o.setExpires( expires );
        o.setAvgTtl( avg_ttl );
    }

    public static NodeInfo changeNodeInfoTime(NodeInfo info) {
        info.setAddTime(DateUtil.getTime() );
        info.setDay( DateUtil.getDay() );
        info.setHour( DateUtil.getHour() );
        info.setMinute( DateUtil.getMinute());
        return info;
    }

    private long responTimeProcess(long pingTime, long responTime) {
        pingTime = 2 * pingTime;
        long redisTime = 0;
        if( responTime > pingTime ){
            redisTime = responTime - pingTime;
        }else{
            redisTime = 1;
        }
        // redis 调用往返
        redisTime = redisTime/2;
        if( redisTime == 0 ){
            redisTime = 1;
        }
        return redisTime;
    }

    public void infoProducer(int clusterId, String ip, int port){
        // 获取集群的所有节点
        List<Map<String, String>> nodeList = JedisUtil.nodeList(ip, port);
        try {
            long pingTime = NetUtil.pingTime( ip.trim() );
            for(Map<String, String> node : nodeList){
                Jedis jedis = null;
                try {
                    String nodeIp = node.get("ip");
                    int nodePort = JedisUtil.getPort(node.get("port"));
                    StopWatch stopWatch = new StopWatch();
                    stopWatch.start();
                    jedis = new Jedis(nodeIp, nodePort, JEDIS_TIMEOUT);
                    String strInfo = jedis.info();
                    stopWatch.stop();
                    long responTime = stopWatch.getTotalTimeMillis();
                    NodeInfo info = getNodeMonitorInfo(strInfo);
                    info.setResponseTime( responTimeProcess(pingTime, responTime) );
                    NodeInfo resInfo = changeNodeInfoTime( info );
                    resInfo.setHost( nodeIp +":" + nodePort);
                    resInfo.setIp( nodeIp );
                    resInfo.setPort( nodePort );

                    infoDao.addNodeInfo( Common.NODE_INFO_TABLE_FORMAT + clusterId, resInfo );
                }catch ( Exception e ){
                    logger.error( "", e );
                }finally {
                    if( null != jedis ){
                        jedis.close();
                    }
                }
            }
        }catch ( Exception e ){
            logger.error( "", e );
        }
    }
}
