package com.newegg.ec.redis.config;

import com.newegg.ec.redis.dao.InitializationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author Jay.H.Zou
 * @date 7/23/2019
 */
@Configuration
public class InitializeConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private InitializationDao initializationDao;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        createTables();
    }

    private void createTables() {
        initializationDao.createGroupTable();;
        initializationDao.createUserTable();
        initializationDao.createGroupUserTable();
        initializationDao.createClusterTable();
        initializationDao.createMachineTable();
    }
}
