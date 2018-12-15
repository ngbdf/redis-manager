package com.newegg.ec.cache.app.logic;

import com.newegg.ec.cache.Application;
import com.newegg.ec.cache.app.model.MemoryDoctorConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
            //System.err.println(logic.memoryDoctor("222222","10.16.50.224", 8010));
            //System.err.println(logic.memoryDoctor("","10.16.46.197", 8800));
            System.err.println(logic.memoryDoctor("","172.16.35.234", 8021));
    }

}
