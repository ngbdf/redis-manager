package com.newegg.ec.cache.module.clusterbuild.common.redis;

import com.newegg.ec.cache.core.entity.redis.RedisConnectParam;
import com.newegg.ec.cache.core.entity.redis.RedisValue;
import com.newegg.ec.cache.core.logger.RMException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by lzz on 2018/3/19.
 */
public class RedisSingleClient extends AbstractRedisClient{

    protected String ip;
    protected int port;
    protected String password;
    protected RedisClient singleClient;

    public RedisSingleClient(RedisConnectParam param) {
        this.ip = param.getIp();
        this.port = param.getPort();
        this.password = param.getRedisPassword();

        if(StringUtils.isNotBlank(param.getRedisPassword())){
            singleClient = RedisClient.create(RedisURI.Builder.redis(param.getIp(), param.getPort()).withPassword(param.getRedisPassword()).build());
        }else{
            singleClient = RedisClient.create(RedisURI.Builder.redis(param.getIp(), param.getPort()).build());
        }
    }


    @Override
    public void close() {
        if (null != singleClient) {
            try {
                singleClient.shutdown();
            } catch (Exception e) {
                throw new RMException("close client error",e);
            }
        }

    }

    @Override
    public RedisValue getRedisValue(int db, String key) {
        return null;
    }

    @Override
    public Object scanRedis(int db, String key) {
        return super.scanRedis(this, key);
    }

    @Override
    public boolean importDataToCluster(String targetIp, int targetPort, String keyFormat) throws InterruptedException {
        throw new RMException("not support");
    }

    @Override
    public RedisCommands getRedisCommands() {
        return singleClient.connect().sync();
    }

}
