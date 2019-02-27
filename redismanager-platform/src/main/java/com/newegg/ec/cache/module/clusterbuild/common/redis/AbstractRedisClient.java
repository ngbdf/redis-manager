package com.newegg.ec.cache.module.clusterbuild.common.redis;

import com.newegg.ec.cache.core.entity.redis.RedisValue;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gl49 on 2018/3/20.
 */
public abstract class AbstractRedisClient implements IRedis {

    public abstract RedisCommands getRedisCommands();

   // public abstract MultiKeyCommands redisMultiKeyCommands();

    public RedisValue getRedisValue(AbstractRedisClient redisClient, int db, String key) {
        RedisCommands jedisCommands = redisClient.getRedisCommands();
        Object res = new Object();
        String type = jedisCommands.type(key);
        long ttl = jedisCommands.ttl(key);
        try {
            switch (type) {
                case "none":
                    break;
                case "string":
                    res = jedisCommands.get(key);
                    break;
                case "hash":
                    res = jedisCommands.hgetall(key);
                    break;
                case "list":
                    res = jedisCommands.lrange(key, 0, 100);
                    break;
                case "set":
                    res = jedisCommands.srandmember(key, 100);
                    break;
                case "zset":
                    res = jedisCommands.zrange(key, 0, 100);
                    break;
            }

        } catch (Exception e) {

        }
        return new RedisValue(ttl, type, res);
    }

    public List<String> scanRedis(AbstractRedisClient redisClient, String key) {

        List<String> resList = new ArrayList<>(10);
        ScanArgs scanArgs = new ScanArgs();
        key = key.trim();
        if (StringUtils.isEmpty(key)) {
            key = "*";
        } else if (key.indexOf("*") != 0 && key.indexOf("*") != (key.length() - 1)) {
            key = "*" + key + "*";
        }

        scanArgs.limit(10);
        scanArgs.match(key);
        boolean isBreak = false;
        for (int i = 0; i < 10; i++) {
            KeyScanCursor<String> result = redisClient.getRedisCommands().scan(scanArgs);
            for(String k : result.getKeys()){
                resList.add(k);
                if (resList.size() > 10) {
                    isBreak = true;
                }
            }
            if (result.getCursor().equals("0")) {
                break;
            }else {
                scanArgs.match(result.getCursor());
            }
            if (isBreak) {
                break;
            }
        }
        return resList;
    }

}
