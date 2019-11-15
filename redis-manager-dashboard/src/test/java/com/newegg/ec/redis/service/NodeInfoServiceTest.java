package com.newegg.ec.redis.service;

import com.newegg.ec.redis.RedisManagerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Jay.H.Zou
 * @date 11/9/2019
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RedisManagerApplication.class)
public class NodeInfoServiceTest {

    @Autowired
    private INodeInfoService nodeInfoService;

    @Test
    public void testCleanupNodeInfo() {
        boolean b = nodeInfoService.cleanupNodeInfo(1);
        System.err.println(b);
    }

}
