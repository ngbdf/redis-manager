package com.newegg.ec.cache.app.logic;

import com.newegg.ec.cache.Application;
import com.newegg.ec.cache.app.model.MemoryDoctorConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

/**
 * Created by gl49 on 2018/5/29.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class ExtensionLogicTest {

    @Autowired
    MemoryDoctorConfig config;

    @Autowired
    ExtensionLogic logic;

    @Test
    public void MemoryDoctorConfig(){
        System.out.println(config.getConfigMap());
    }

    @Test
    public void memoryDoctor(){
        long start = System.currentTimeMillis();
        List<Map<String, String>> result = logic.memoryDoctor(5);
        long time = System.currentTimeMillis() -start;
        System.err.println("cost time : " + time + " . " + result);
    }


}
