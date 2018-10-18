package com.newegg.ec.cache.app.component.redis;

import com.newegg.ec.cache.app.component.RedisManager;
import com.newegg.ec.cache.app.model.RedisValue;
import com.newegg.ec.cache.app.util.JedisUtil;
import redis.clients.jedis.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lzz on 2018/3/19.
 */
public class JedisClusterClient extends JedisSingleClient implements IRedis {
    private static ExecutorService executorPool = Executors.newFixedThreadPool(200);

    private JedisCluster jedis;

    public JedisClusterClient(String ip, int port) {
        super(ip, port);
        HostAndPort hostAndPort = new HostAndPort(ip, port);
        jedis = new JedisCluster(hostAndPort);
    }

    @Override
    public void close() {
        if (null != jedis) {
            try {
                jedis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (null != singleClient) {
            try {
                singleClient.close();
            } catch (Exception e) {
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

    /**
     * 可以考虑用 migrate 实现，但是 migrate 是在服务端建立 socket 链接到目标机器上发送命令
     *
     * @param targetIp
     * @param targetPort
     * @param keyFormat
     * @return
     * @throws InterruptedException
     */
    @Override
    public boolean importDataToCluster(String targetIp, int targetPort, String keyFormat) throws InterruptedException {
        String importKey = RedisManager.getImportKey(this.ip, this.port, targetIp, targetPort, keyFormat);
        if (null == RedisManager.importMap.get(importKey)) {
            RedisManager.importMap.put(importKey, new AtomicInteger(0));
        }
        Map<String, Map> masterNodes = JedisUtil.getMasterNodes(this.ip, this.port);
        CountDownLatch countDownLatch = new CountDownLatch(masterNodes.size());
        for (Map.Entry<String, Map> masterNode : masterNodes.entrySet()) {
            Map<String, String> masterMap = masterNode.getValue();
            String ip = masterMap.get("ip");
            int port = Integer.parseInt(masterMap.get("port"));
            // 一个 master 对应一个线程去 scan
            System.out.println(ip + "port" + port);
            executorPool.submit(new Runnable() {
                @Override
                public void run() {
                    Jedis jedis = new Jedis(ip, port);
                    try {
                        ScanParams scanParams = new ScanParams();
                        scanParams.match(keyFormat);
                        scanParams.count(1500);
                        ScanResult<String> scanResult = jedis.scan("0", scanParams);
                        RedisManager.importMap.get(importKey).addAndGet(scanResult.getResult().size()); //统计scan的次数
                        importScanResult(scanResult.getResult().iterator(), targetIp, targetPort);
                        while (!scanResult.getStringCursor().equals("0")) {
                            scanResult = jedis.scan(scanResult.getStringCursor(), scanParams);
                            RedisManager.importMap.get(importKey).addAndGet(scanResult.getResult().size()); //统计scan的次数
                            importScanResult(scanResult.getResult().iterator(), targetIp, targetPort);
                            System.out.println(scanResult.getStringCursor() + "---" + RedisManager.importMap.get(importKey).get());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        jedis.close();
                        countDownLatch.countDown();
                    }
                }
            });
        }
        countDownLatch.await();
        RedisManager.importMap.remove(importKey);
        return true;
    }

    private void importScanResult(Iterator<String> resultIterator, String targetIp, int targetPort) {
        HostAndPort targetHostAndPort = new HostAndPort(targetIp, targetPort);
        JedisCluster targetCluster = new JedisCluster(targetHostAndPort);
        try {
            while (resultIterator.hasNext()) {
                String key = resultIterator.next();
                RedisValue redisValue = getRedisValue(this, 0, key);
                try {
                    switch (redisValue.getType()) {
                        case "string":
                            targetCluster.set(key, String.valueOf(redisValue.getResult()));
                            break;
                        case "hash":
                            Map<String, String> hashRes = (Map<String, String>) redisValue.getResult();
                            targetCluster.hmset(key, hashRes);
                            break;
                        case "list":
                            List<String> listRes = (List<String>) redisValue.getResult();
                            for (int i = 0; i < listRes.size(); i++) {
                                targetCluster.lpush(key, listRes.get(i));
                            }
                            break;
                        case "set":
                            Set<String> setRes = (Set<String>) redisValue.getResult();
                            Iterator<String> setIterator = setRes.iterator();
                            while (setIterator.hasNext()) {
                                targetCluster.sadd(key, setIterator.next());
                            }
                            break;
                    }
                } finally {
                    targetCluster.expire(key, (int) redisValue.getTtl());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                targetCluster.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
