package com.newegg.ec.cache.app.component.redis;

import com.newegg.ec.cache.app.model.RedisValue;
import com.newegg.ec.cache.app.util.JedisUtil;
import redis.clients.jedis.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lzz on 2018/3/19.
 */
public class JedisClusterClient extends JedisSingleClient implements IRedis {
    private static ExecutorService executorPool = Executors.newFixedThreadPool(200);
    private static Map<String, AtomicInteger> importMap = new HashMap<>(); // 用于统计已经倒入多少数据

    private JedisCluster jedis;
    public JedisClusterClient(String ip, int port){
        super(ip, port);
        HostAndPort hostAndPort = new HostAndPort(ip, port);
        jedis = new JedisCluster( hostAndPort );
    }

    @Override
    public void close() {
        if( null != jedis ){
            try {
                jedis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if( null != singleClient ){
            try {
                singleClient.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public RedisValue getRedisValue(int db, String key) {
        return super.getRedisValue(this, db, key);
    }

    @Override
    public Object scanRedis(int db, String key) {
        return super.scanRedis(this, key);
    }

    @Override
    public JedisCommands getRedisCommands() {
        return jedis;
    }

    @Override
    public boolean importDataToCluster(String targetIp, int targetPort, String keyFormat) throws InterruptedException {
        String importKey = getImportKey(this.ip, this.port, targetIp, targetPort, keyFormat);
        if( null == importMap.get(importKey) ){
            importMap.put(importKey, new AtomicInteger(0));
        }
        Map<String,Map> masterNodes = JedisUtil.getMasterNodes(this.ip, this.port);
        CountDownLatch countDownLatch = new CountDownLatch( masterNodes.size() );
        for(Map.Entry<String, Map> masterNode : masterNodes.entrySet()){
            Map<String, String> masterMap = masterNode.getValue();
            String ip = masterMap.get("ip");
            int port = Integer.parseInt(masterMap.get("port"));
            // 一个 master 对应一个线程去 scan
            System.out.println( ip + "port" + port);
            executorPool.submit(new Runnable() {
                @Override
                public void run() {
                    Jedis jedis = new Jedis(ip, port);
                    try {
                        ScanParams scanParams = new ScanParams();
                        scanParams.match( keyFormat );
                        scanParams.count(1500);
                        ScanResult<String> scanResult =  jedis.scan( "0", scanParams );
                        importMap.get( importKey ).getAndAdd( scanResult.getResult().size() ); //统计scan的次数
                        importScanResult( scanResult.getResult().iterator(), targetIp, targetPort );
                        while ( !scanResult.getStringCursor().equals("0") ){
                            System.out.println( scanResult.getStringCursor() );
                            scanResult = jedis.scan( scanResult.getStringCursor(), scanParams );
                            importMap.get( importKey ).getAndAdd(scanResult.getResult().size()); //统计scan的次数
                            importScanResult( scanResult.getResult().iterator(), targetIp, targetPort );
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        jedis.close();
                        countDownLatch.countDown();
                    }
                }
            });
        }
        countDownLatch.await();
        importMap.remove( importKey );
        return true;
    }

    private void importScanResult(Iterator<String> resultIterator, String targetIp, int targetPort){
        HostAndPort targetHostAndPort = new HostAndPort(targetIp, targetPort);
        JedisCluster targetCluster = new JedisCluster( targetHostAndPort );
        while ( resultIterator.hasNext() ){
            String key = resultIterator.next();
            RedisValue redisValue = getRedisValue(this, 0, key );
            try {
                switch ( redisValue.getType() ){
                    case "string":
                        targetCluster.set(key, String.valueOf(redisValue.getResult()));
                        break;
                    case "hash":
                        Map<String, String> hashRes = (Map<String, String>) redisValue.getResult();
                        for(Map.Entry<String, String> resItem: hashRes.entrySet()){
                            targetCluster.hset(key, resItem.getKey(), resItem.getValue());
                        }
                        break;
                    case "list":
                        List<String> listRes = (List<String>) redisValue.getResult();
                        for(int i = 0; i < listRes.size(); i++){
                            targetCluster.lpush(key, listRes.get(i));
                        }
                        break;
                    case "set":
                        Set<String> setRes  = (Set<String>) redisValue.getResult();
                        Iterator<String> setIterator = setRes.iterator();
                        while (setIterator.hasNext()){
                            targetCluster.sadd( key, setIterator.next() );
                        }
                        break;
                }
            }finally {
                targetCluster.expire(key, (int) redisValue.getTtl());
                try {
                    targetCluster.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getImportKey(String ownerIp, int ownerPort, String targetIp, int targetPort, String formatKey){
        return ownerIp + "-" + port + "-" + targetIp + "-" + targetPort + "-" + formatKey;
    }
}
