package com.newegg.ec.cache.app.util;

import com.newegg.ec.cache.app.model.RedisNode;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by gl49 on 2018/4/21.
 */
public class JedisUtilTest {
    @Test
    public void test(){
        Map<String, Map> res = JedisUtil.getClusterNodes("172.16.35.75", 8028);
        List<Map<String, String>> ress = JedisUtil.dbInfo("172.16.35.75", 8028);
        System.out.println( ress );
    }

    @Test
    public void test454(){
        int size = JedisUtil.dbSize("10.16.46.196", 8700);
        System.out.println( size );
    }

    @Test
    public void testConfig(){
        Map map = JedisUtil.getRedisConfig("10.16.46.192", 8008);
        System.out.println( map );
    }

    @Test
    public void testDivice(){
        int logSize = 10000;
        int size = logSize/3;
        System.out.println( size );
    }

    @Test
    public void testIplist(){
        String ipListStr = "127.0.0.1:8080 master gl49 lzz363216\n" +
                "127.0.0.1:8081  gl49 lzz363216\n" +
                "127.0.0.1:8082 master gl49 lzz363216\n" +
                "127.0.0.1:8083 master gl49 lzz363216\n" +
                "127.0.0.2:8084  gl49 lzz363216\n" +
                "127.0.0.1:8085  gl49 lzz363216\n" +
                "127.0.0.1:8086  gl49 lzz363216\n" +
                "127.0.0.1:8087 master gl49 lzz363216\n" +
                "127.0.0.1:8088  gl49 lzz363216";
        Map<RedisNode, List<RedisNode>> res = JedisUtil.getInstallNodeMap( ipListStr );
        System.out.println( res );
        Set<String> ipSet = JedisUtil.getIPList( ipListStr );
        System.out.println( ipSet );
        System.out.println(JedisUtil.getInstallNodeList(ipListStr));
        System.out.println(JedisUtil.getInstallNodeList(ipListStr).size());
    }

    @Test
    public void testIplist2(){
        String ipListStr = "127.0.0.1:8080 master\n" +
                "127.0.0.1:8081\n" +
                "127.0.0.1:8082 master\n" +
                "127.0.0.1:8083 master\n" +
                "127.0.0.2:8084\n" +
                "127.0.0.1:8085\n" +
                "127.0.0.1:8086\n" +
                "127.0.0.1:8087\n" +
                "127.0.0.1:8088";
        String ipListStr1 = "127.0.0.1:8080";
        Map<RedisNode, List<RedisNode>> res = JedisUtil.getInstallNodeMap(ipListStr1);
        System.out.println( res );
        Set<String> ipSet = JedisUtil.getIPList( ipListStr );
        System.out.println( ipSet );
        System.out.println(JedisUtil.getInstallNodeList(ipListStr));
        System.out.println(JedisUtil.getInstallNodeList(ipListStr).size());
    }

    @Test
    public void testIplist3(){
        String ipListStr = "127.0.0.1:8080";
        System.out.println( JedisUtil.getInstallNodeMap(ipListStr) );
    }
}
