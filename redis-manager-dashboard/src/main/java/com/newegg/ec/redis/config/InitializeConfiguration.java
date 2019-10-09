package com.newegg.ec.redis.config;

import com.newegg.ec.redis.dao.*;
import com.newegg.ec.redis.plugin.alert.dao.IAlertChannelDao;
import com.newegg.ec.redis.plugin.alert.dao.IAlertRecordDao;
import com.newegg.ec.redis.plugin.alert.dao.IAlertRuleDao;
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
    private IGroupDao groupDao;

    @Autowired
    private IUserDao userDao;

    @Autowired
    private IGroupUserDao groupUserDao;

    @Autowired
    private IClusterDao clusterDao;

    @Autowired
    private IMachineDao machineDao;

    @Autowired
    private IAlertChannelDao alertChannelDao;

    @Autowired
    private IAlertRuleDao alertRuleDao;

    @Autowired
    private IAlertRecordDao alertRecordDao;

    @Autowired
    private IRedisNodeDao redisNodeDao;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        createTables();
    }

    private void createTables() {
        groupDao.createGroupTable();;
        userDao.createUserTable();
        groupUserDao.createGroupUserTable();
        clusterDao.createClusterTable();
        machineDao.createMachineTable();
        alertChannelDao.createAlertChannelTable();
        alertRuleDao.createAlertChannelTable();
        alertRecordDao.createAlertRecordTable();
        redisNodeDao.createRedisNodeTable();
    }
}
