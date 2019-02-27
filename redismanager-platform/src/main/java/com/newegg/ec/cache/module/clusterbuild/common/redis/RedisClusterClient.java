package com.newegg.ec.cache.module.clusterbuild.common.redis;

import com.newegg.ec.cache.core.entity.redis.RedisConnectParam;
import com.newegg.ec.cache.core.entity.redis.RedisValue;
import com.newegg.ec.cache.core.logger.RMException;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lf52 on 2019/2/26.
 */
public class RedisClusterClient extends RedisSingleClient implements IRedis {

    private static final Log logger = LogFactory.getLog(RedisClusterClient.class);

    private static ExecutorService pool = Executors.newFixedThreadPool(200);

    private io.lettuce.core.cluster.RedisClusterClient redis;

    public RedisClusterClient(RedisConnectParam param) {
        super(param);
        redis =getRedisClusterClient(param);
    }

    @Override
    public void close() {
        if (null != redis) {
            try {
                redis.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (null != singleClient) {
            try {
                singleClient.shutdown();
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
    public RedisCommands getRedisCommands() {
        return (RedisCommands)redis.connect().sync();
    }

    private io.lettuce.core.cluster.RedisClusterClient getRedisClusterClient(RedisConnectParam param) {
        List<RedisURI> redisURIs = new ArrayList<>();
        RedisURI.Builder build = RedisURI.builder();
        build.withHost(param.getIp()).withPort(param.getPort());
        build.withTimeout(Duration.ofMillis(3000));
        if (param.getRedisPassword() != null) {
            build.withPassword(param.getRedisPassword());
        }
        redisURIs.add(build.build());
        redis = io.lettuce.core.cluster.RedisClusterClient.create(redisURIs);
        return redis;
    }

    @Override
    public boolean importDataToCluster(String targetIp, int targetPort, String keyFormat) throws InterruptedException {
        throw new RMException("not support");
    }

}
