package com.newegg.ec.cache.plugin.humpback;

import com.newegg.ec.cache.Application;
import com.newegg.ec.cache.plugin.basemodel.StartType;
import net.sf.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;

/**
 * Created by lf52 on 2018/5/8.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class HumpackOptionTest {

    @Autowired
    private HumpbackManager humpback;

    @Test
    public void testoptionContainer() {
        boolean result = humpback.optionContainer("localhost", "itemService_test_Log", StartType.start);
        System.out.println("result "+result);
    }


    @Test
    public void testDelContainer() {
        boolean stop = humpback.optionContainer("localhost", "itemservicespring_E4_leotest", StartType.stop);
        if(stop){
            JSONObject param = new JSONObject();
            param.put("ip","localhost");
            param.put("containerId","itemservicespring_E4_leotest");
            System.out.println(humpback.remove(param));
        }
    }

    @Test
    public void testpullImage() {
        String ipListStr = "localhost:8008 master\n" +
                "localhost:8009\n" +
                "localhost:8010";
        JSONObject param = new JSONObject();
        param.put("iplist",ipListStr);
        param.put("imageUrl", "humpback-hub.newegg.org/shec/redis3.0.6:v3");
        System.out.println(humpback.pullImage(param));
    }

    @Test
    public void testCreateContainer() {
        String param = "{\n" +
                "        \"Status\": {\n" +
                "        \"Status\": \"running\",\n" +
                "                \"Restarting\": false,\n" +
                "                \"Dead\": false,\n" +
                "                \"ExitCode\": 0,\n" +
                "                \"Running\": true,\n" +
                "                \"Error\": \"\",\n" +
                "                \"FinishedAt\": \"0001-01-01T00:00:00Z\",\n" +
                "                \"OOMKilled\": false,\n" +
                "                \"Pid\": 30908,\n" +
                "                \"StartedAt\": \"2017-12-06T07:48:30.31872744Z\",\n" +
                "                \"Paused\": false\n" +
                "    },\n" +
                "        \"Dns\": [],\n" +
                "        \"RestartPolicy\": \"always\",\n" +
                "            \"NetworkMode\": \"host\",\n" +
                "            \"Image\": \"localhost/shec/itemservice-autodeploy:v0.2\",\n" +
                "            \"Env\": [\n" +
                "        \"JAVA_HOME=/usr/local/jdk1.8.0_60\",\n" +
                "                \"PATH=$PATH:$JAVA_HOME/bin\",\n" +
                "                \"port=8361\",\n" +
                "                \"zk_address=localhost:8181,localhost:8181,1localhost:8181,localhost:8181,localhost:8181\",\n" +
                "                \"zk_conf_path=/opt/app/baseservice/bdservice-redis-landingpage-new/conf/service-configuration-dynamic.xml\",\n" +
                "                \"zk_listen_path=/zkconf/itemservice-spring \",\n" +
                "                \"JVM_OPTS=-Xms4096M -Xmx4096M -Xmn1536M -Xss512K -XX:ParallelGCThreads=20 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+CMSConcurrentMTEnabled -XX:+DisableExplicitGC -XX:CMSInitiatingOccupancyFraction=70 \",\n" +
                "                \"MetricsLogger=WARN,METRICS\",\n" +
                "                \"LoggerLevel=WARN,INFO,ERROR,MAIL\"\n" +
                "        ],\n" +
                "        \"Name\": \"itemservicespring_E4_leotest\",\n" +
                "            \"Volumes\": [\n" +
                "        {\n" +
                "            \"ContainerVolume\": \"/opt/app/baseservice/bdservice-redis-landingpage-new/logs\",\n" +
                "                \"HostVolume\": \"/opt/app/baseservice/bdservice-redis-landingpage-new/logs\"\n" +
                "        }\n" +
                "        ],\n" +
                "        \"Command\": \"/bin/bash -c bin/start-service.sh\",\n" +
                "            \"Id\": \"8afb1f54743d451e02b209140fdc2475b4d84e216d9eb90ea7d5f88e027b063b\",\n" +
                "            \"HostName\": \"e4redis19.mercury.corp\",\n" +
                "            \"SHMSize\": 67108864\n" +
                "    }";

        System.out.println(humpback.createContainer("localhost",JSONObject.fromObject(param)));


    }

    @Test
    public void testgetContainerInfo() {
        System.out.println(humpback.getContainerInfo("localhost", "itemserviceSSL"));
    }

    @Test
    public void testInstall() {
        JSONObject param = new JSONObject();
        param.put("imageUrl", "humpback-hub.newegg.org/shec/redis3.0.6:v3");
        humpback.installNodeList(param,new ArrayList());
    }
}
