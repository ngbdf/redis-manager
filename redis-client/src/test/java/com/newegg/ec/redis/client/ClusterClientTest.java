package com.newegg.ec.redis.client;

import com.newegg.ec.redis.client.config.RedisFactoryConfig;
import com.newegg.ec.redis.client.factory.ClusterClientFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Created by lf52 on 2019/2/21.
 */
public class ClusterClientTest {

    ClusterClientFactory clientFactory;
    RedisFactoryConfig config;

    @Before
    public void before(){
        config = RedisFactoryConfig.builder().setNodeList("10.16.50.219:9101").
                setByteMaxConnection(100).
                setStringMaxConnection(100).
                setPassword("Newegg@1234").
                setTimeout(3000).build();
       clientFactory = new ClusterClientFactory(config);
    }

    @Test
    public void testRedisCommand(){

        RedisBaseClient client = clientFactory.provideClient("0");

        System.out.println(client.clusterNodes());
        List<Map<String, String>> result = client.hgetall("itemService_itemPricingByType|75-977-067|1");
        System.out.println(result.get(0));

        System.out.println(client.contains("itemService_itemPricingByType|75-977-067|1"));
        clientFactory.releaseClient(client);

    }

    @Test
    public void testRedisCommandWithPassword(){
        clientFactory =  (ClusterClientFactory)clientFactory.reset(config,"Newegg@1234");
        RedisBaseClient client = clientFactory.provideClient("0");
        System.out.println(client.contains("02289190229|USA|1003"));
        clientFactory.releaseClient(client);
    }

    @After
    public void after(){

    }


}
