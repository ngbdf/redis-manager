package com.newegg.ec.redis.plugin.install;

import com.newegg.ec.redis.entity.RedisNode;

import java.util.List;

/**
 *
 *
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
public interface InstallOperation {

    boolean checkEnvironment();

    boolean pullImage();

    boolean install(List<RedisNode> redisNodeList);

}
