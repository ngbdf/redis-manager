package com.newegg.ec.redis.util;

import com.newegg.ec.redis.entity.RedisNode;

import java.util.List;
import java.util.Objects;

import static com.newegg.ec.redis.entity.RedisNode.CONNECTED;
import static com.newegg.ec.redis.entity.RedisNode.UNCONNECTED;
import static com.newegg.ec.redis.util.RedisUtil.REDIS_MODE_CLUSTER;

/**
 * @author Jay.H.Zou
 * @date 4/5/2020
 */
public class RedisNodeUtil {

    private RedisNodeUtil() {
    }

    public static boolean equals(RedisNode redisNode1, RedisNode redisNode2) {
        return Objects.equals(redisNode1.getHost(), redisNode2.getHost())
                && redisNode1.getPort() == redisNode2.getPort();
    }

    public static void setRedisRunStatus(String redisModel, List<RedisNode> redisNodeList) {
        redisNodeList.forEach(redisNode -> {
            setRedisRunStatus(redisModel, redisNode);
        });
    }

    public static void setRedisRunStatus(String redisModel, RedisNode redisNode) {
        boolean telnet = NetworkUtil.telnet(redisNode.getHost(), redisNode.getPort());
        redisNode.setRunStatus(telnet);
        if (!Objects.equals(redisModel, REDIS_MODE_CLUSTER)) {
            redisNode.setLinkState(telnet ? CONNECTED : UNCONNECTED);
        }
    }
}
