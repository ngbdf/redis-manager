package com.newegg.ec.redis.config;

import com.newegg.ec.redis.dao.*;
import com.newegg.ec.redis.entity.Group;
import com.newegg.ec.redis.entity.RDBAnalyze;
import com.newegg.ec.redis.entity.User;
import com.newegg.ec.redis.plugin.alert.dao.IAlertChannelDao;
import com.newegg.ec.redis.plugin.alert.dao.IAlertRecordDao;
import com.newegg.ec.redis.plugin.alert.dao.IAlertRuleDao;
import com.newegg.ec.redis.schedule.RDBScheduleJob;
import com.newegg.ec.redis.service.IRdbAnalyzeResultService;
import com.newegg.ec.redis.service.IRdbAnalyzeService;
import com.newegg.ec.redis.service.impl.ScheduleTaskService;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 7/23/2019
 */
@Configuration
public class InitializeConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(InitializeConfiguration.class);
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

    @Autowired
    private IOperationLogDao operationLogDao;

    @Autowired
    private ISentinelMastersDao sentinelMastersDao;

    @Autowired
    private IRdbAnalyzeService rdbAnalyzeService;

    @Autowired
    private IRdbAnalyzeResultService rdbAnalyzeResultService;

    @Autowired
    private ScheduleTaskService scheduleTaskService;

    @Value("${redis-manager.auth.user-name:admin}")
    private String userName;

    @Value("${redis-manager.auth.password:admin}")
    private String password;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        createTables();
        initDefaultAuth();
        initRDBJob();
    }

    private void initRDBJob() {
        List<RDBAnalyze> rdbAnalyzes = rdbAnalyzeService.selectALL();
        for (RDBAnalyze rdbAnalyze : rdbAnalyzes) {
            try {
                if (rdbAnalyze.isAutoAnalyze()) {
                    scheduleTaskService.addTask(rdbAnalyze, RDBScheduleJob.class);
                }
            } catch (SchedulerException e) {
                LOG.error("schedule job run faild!message:{}", e.getMessage());
            }
        }
    }

    private void createTables() {
        groupDao.createGroupTable();
        userDao.createUserTable();
        groupUserDao.createGroupUserTable();
        clusterDao.createClusterTable();
        machineDao.createMachineTable();
        alertChannelDao.createAlertChannelTable();
        alertRuleDao.createAlertChannelTable();
        alertRecordDao.createAlertRecordTable();
        redisNodeDao.createRedisNodeTable();
        operationLogDao.createLogTable();
        sentinelMastersDao.createSentinelMastersTable();
        rdbAnalyzeService.createRdbAnalyzeTable();
        rdbAnalyzeResultService.createRdbAnalyzeResultTable();
    }

    @Transactional
    public void initDefaultAuth() {
        List<User> allSuperAdmin = groupUserDao.selectAllSuperAdmin();
        if (allSuperAdmin != null && allSuperAdmin.size() > 0) {
            return;
        }
        Group group = buildDefaultGroup();
        Group existGroup = groupDao.selectGroupByGroupName(group.getGroupName());
        if (existGroup != null) {
            group.setGroupId(existGroup.getGroupId());
        } else {
            groupDao.insertGroup(group);
        }
        User user = buildDefaultUser();
        user.setGroupId(group.getGroupId());
        User existUser = userDao.selectUserByName(userName);
        if (existUser != null) {
            user.setUserId(existUser.getUserId());
        } else {
            userDao.insertUser(user);
        }
        User existGroupUser = userDao.selectUserRole(user.getGroupId(), user.getUserId());
        if (existGroupUser == null) {
            groupUserDao.insertGroupUser(user, user.getGroupId());
        }
    }

    private User buildDefaultUser() {
        User user = new User();
        user.setUserRole(User.UserRole.SUPER_ADMIN);
        user.setUserName(userName);
        user.setPassword(password);
        return user;
    }

    private Group buildDefaultGroup() {
        Group group = new Group();
        group.setGroupName(userName);
        return group;
    }
}
