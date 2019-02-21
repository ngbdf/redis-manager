package com.newegg.ec.redis.client;

import com.newegg.ec.redis.client.config.RedisFactoryConfig;
import com.newegg.ec.redis.client.factory.StandAloneClientFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by lf52 on 2019/2/21.
 */
public class StandAloneClientTest {

    StandAloneClientFactory clientFactory;
    RedisFactoryConfig config;

    @Before
    public void before(){
        config = RedisFactoryConfig.builder().setNodeList("10.16.46.196:8800").
                setByteMaxConnection(100).
                setStringMaxConnection(100).
                setPassword("").
                setTimeout(3000).build();
       clientFactory = new StandAloneClientFactory(config);
    }

    @Test
    public void testRedisCommand(){

       // RedisBaseClient client = clientFactory.provideClient("0");

        //System.out.println(client.clusterNodes());
        //List<Map<String, String>> result = client.hgetall("itemService_itemPricingByType|75-977-067|1");
        //System.out.println(result.get(0));

        clientFactory =  (StandAloneClientFactory)clientFactory.reset(config,"12345678");
        RedisBaseClient client = clientFactory.provideClient("0");
        //client.auth("12345678");
        System.out.println(client.contains("02289190229|USA|10031"));
        clientFactory.releaseClient(client);

    }

    @After
    public void after(){

    }


}
