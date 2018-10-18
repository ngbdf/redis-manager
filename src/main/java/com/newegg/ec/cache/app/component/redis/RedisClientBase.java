package com.newegg.ec.cache.app.component.redis;

import com.newegg.ec.cache.app.model.RedisValue;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gl49 on 2018/3/20.
 */
public abstract class RedisClientBase {
    public abstract JedisCommands getRedisCommands();

    public abstract MultiKeyCommands redisMultiKeyCommands();

    public RedisValue getRedisValue(RedisClientBase redisClient, int db, String key) {
        JedisCommands jedisCommands = redisClient.getRedisCommands();
        Object res = null;
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
                    res = jedisCommands.hgetAll(key);
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

    public List<String> scanRedis(RedisClientBase redisClient, String key) {
        ScanParams params = new ScanParams();
        params.count(10);
        key = key.trim();
        if (StringUtils.isEmpty(key)) {
            key = "*";
        } else if (key.indexOf("*") != 0 && key.indexOf("*") != (key.length() - 1)) {
            key = "*" + key + "*";
        }
        params.match(key);
        String cursor = "0";
        List<String> resList = new ArrayList<>();
        boolean isBreak = false;
        for (int i = 0; i < 10; i++) {
            ScanResult<String> scanResult = redisClient.redisMultiKeyCommands().scan(cursor, params);
            for (String sResult : scanResult.getResult()) {
                resList.add(sResult);
                if (resList.size() > 10) {
                    isBreak = true;
                }
            }
            cursor = scanResult.getStringCursor();
            if (isBreak) {
                break;
            }
        }
        return resList;
    }

}
