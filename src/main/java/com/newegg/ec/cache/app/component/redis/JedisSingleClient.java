package com.newegg.ec.cache.app.component.redis;

import com.newegg.ec.cache.app.model.ConnectionParam;
import com.newegg.ec.cache.app.model.RedisValue;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.*;

/**
 * Created by lzz on 2018/3/19.
 */
public class JedisSingleClient extends RedisClientBase implements IRedis {
    protected String ip;
    protected int port;
    protected String password;
    protected Jedis singleClient;

    public JedisSingleClient(ConnectionParam param) {
        this.ip = param.getIp();
        this.port = param.getPort();
        this.password = param.getRedisPassword();
        singleClient = new Jedis(ip, port);
        if (StringUtils.isNotBlank(this.password)) {
            singleClient.auth(this.password);
        }
    }

    public ScanResult<String> redisScan(String path, String cursor, ScanParams params) {
        return singleClient.scan(cursor, params);
    }

    @Override
    public void close() {
        if (null != singleClient) {
            try {
                singleClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public Object scanRedis(int db, String key) {
        return super.scanRedis(this, key);
    }

    @Override
    public boolean importDataToCluster(String targetIp, int targetPort, String keyFormat) throws InterruptedException {
        throw new RuntimeException("not support");
    }

    @Override
    public RedisValue getRedisValue(int db, String key) {
        return super.getRedisValue(this, db, key);
    }

    @Override
    public JedisCommands getRedisCommands() {
        return singleClient;
    }

    @Override
    public MultiKeyCommands redisMultiKeyCommands() {
        return singleClient;
    }
}
