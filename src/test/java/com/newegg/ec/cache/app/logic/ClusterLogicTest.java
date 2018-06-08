package com.newegg.ec.cache.app.logic;

import com.newegg.ec.cache.app.model.Host;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gl49 on 2018/5/29.
 */
public class ClusterLogicTest {
    @Test
    public void testChangeConfigFile(){
        //tcp-keepalive  timeout
        ClusterLogic clusterLogic = new ClusterLogic();
        List<Host> hostList = new ArrayList<>();
        Host host1 = new Host();
        host1.setIp("10.16.46.170");
        host1.setPort(8051);
        hostList.add( host1 );

        Host host2 = new Host();
        host2.setIp("10.16.46.170");
        host2.setPort(8052);
        hostList.add( host2 );

        Host host3 = new Host();
        host3.setIp("10.16.46.170");
        host3.setPort(8053);
        hostList.add( host3 );

        //List<Host> hostList, String username, String password, String filePathFormat, String field, String value)
        clusterLogic.changeConfigFile(hostList, "gl49", "Lzz363219", "/home/ABS_CORP/gl49/redis-cluster/test-8008.conf", "timeout", "864001");
    }
}
