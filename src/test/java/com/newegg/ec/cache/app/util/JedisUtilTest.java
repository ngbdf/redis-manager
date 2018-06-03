package com.newegg.ec.cache.app.util;

import com.newegg.ec.cache.app.component.redis.JedisClusterClient;
import com.newegg.ec.cache.app.model.RedisNode;
import org.junit.Test;
import redis.clients.jedis.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by gl49 on 2018/4/21.
 */
public class JedisUtilTest {
    @Test
    public void test(){
        Map<String, Map> res = JedisUtil.getClusterNodes("localhost", 8028);
        List<Map<String, String>> ress = JedisUtil.dbInfo("localhost", 8028);
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
    public void testIplist3() {
        String filePathFormat = "/opt/aa/{port}/fdafd.conf";
        String[] filePaths = filePathFormat.split("\\{port\\}");
        System.out.println( filePaths[0] );
        System.out.println( filePaths[1] );
    }

    @Test
    public void testSet(){
        HostAndPort hostAndPort = new HostAndPort("10.16.46.170", 8052);
        JedisCluster jedis =  new JedisCluster(hostAndPort);
        for(int i = 0; i < 10000; i++){
            jedis.set( "key-test" + i, String.valueOf(i));
            jedis.expire("key-test" + i, 500);
        }

    }

    @Test
    public void testConfig2(){
        Jedis jedis = new Jedis("10.16.46.170", 8052);
        jedis.configSet("slowlog-max-len", "200");
        jedis.configResetStat();
        jedis.clusterSaveConfig();
    }

    @Test
    public void testScan(){
        //HostAndPort hostAndPort = new HostAndPort("10.16.46.170", 8052);
        //JedisCluster jedis =  new JedisCluster(hostAndPort);
        Jedis jedis = new Jedis("10.16.46.170", 8051); //72788
        int count = 0;
        ScanParams scanParams = new ScanParams();
        scanParams.match("key-test*");
        scanParams.count(1000);
        ScanResult<String> scanResult =  jedis.scan( "0", scanParams );
        count += scanResult.getResult().size();
        System.out.println( count + "----");
        while ( !scanResult.getStringCursor().equals("0") ){
            System.out.println( scanResult.getStringCursor() );
            scanResult = jedis.scan( scanResult.getStringCursor(), scanParams );
            count += scanResult.getResult().size();
            System.out.println( scanResult.getStringCursor() + " scan cursor");
            System.out.println( count + "------------ size");
        }
    }
}
