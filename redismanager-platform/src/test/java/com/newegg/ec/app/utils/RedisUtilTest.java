package com.newegg.ec.app.utils;

import com.newegg.ec.cache.core.entity.redis.RedisConnectParam;
import com.newegg.ec.cache.util.redis.RedisUtils;
import net.sf.json.JSONArray;
import org.junit.Test;

/**
 * Created by lf52 on 2019/2/23.
 */
public class RedisUtilTest {

    @Test
    public void test1(){
        RedisConnectParam param = new RedisConnectParam("10.16.46.196",8800,"12345678");
        //System.out.println(RedisUtils.getInfo(param));
        //System.out.println(RedisUtils.getRedisConfig(param));
        //System.out.println(RedisUtils.getRedisMode(param));
        //System.out.println(RedisUtils.redisDBInfo(param));
        //System.out.println(RedisUtils.dbSize(param));
        //System.out.println(RedisUtils.getAllNodes(param));
        //System.out.println(RedisUtils.getClusterInfo(param));
        //System.out.println(RedisUtils.getNodeId(param));
        System.out.println(RedisUtils.getSlowLog(param, 1000));

    }

    @Test
    public void test2(){
        RedisConnectParam param = new RedisConnectParam("10.16.46.192",8008);
        //System.out.println(RedisUtils.getInfo(param));
        //System.out.println(RedisUtils.getRedisConfig(param));
        //System.out.println(RedisUtils.getRedisMode(param));
        //System.out.println(RedisUtils.redisDBInfo(param));
        //System.out.println(RedisUtils.dbSize(param));
        //System.out.println(RedisUtils.getAllNodes(param));
        //System.out.println(RedisUtils.getClusterInfo(param));
        //System.out.println(RedisUtils.getNodes(param,false));
        //System.out.println(RedisUtils.getNodeId(param));
        JSONArray array = JSONArray.fromObject(RedisUtils.getSlowLog(param, 1));
        System.out.println((int)JSONArray.fromObject(array.get(0)).get(1));
    }

}
