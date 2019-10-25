package com.newegg.ec.redis.service.impl;

import com.newegg.ec.redis.dao.IGroupDao;
import com.newegg.ec.redis.dao.IGroupUserDao;
import com.newegg.ec.redis.dao.IUserDao;
import com.newegg.ec.redis.entity.Group;
import com.newegg.ec.redis.entity.User;
import com.newegg.ec.redis.service.IGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/9/2
 */
@Service
public class GroupService implements IGroupService {

    private static final Logger logger = LoggerFactory.getLogger(GroupService.class);

    @Autowired
    private IGroupDao groupDao;

    @Autowired
    private IGroupUserDao groupUserDao;

    @Autowired
    private IUserDao userDao;

    @Override
    public List<Group> getAllGroup() {
        try {
            return groupDao.selectAllGroup();
        } catch (Exception e) {
            logger.error("Get all group failed.", e);
            return null;
        }

    }

    @Override
    public List<Group> getGroupByUserId(Integer userId) {
        try {
            return groupDao.selectGroupByUserId(userId);
        } catch (Exception e) {
            logger.error("Get group by user id failed, user id = " + userId, e);
            return new ArrayList<>();
        }

    }

    @Override
    public Group getGroupById(Integer groupId) {
        try {
            return groupDao.selectGroupById(groupId);
        } catch (Exception e) {
            logger.error("Get group by id failed, group id = " + groupId, e);
            return null;
        }
    }

    @Override
    public Group getGroupByName(String groupName) {
        try {
            return groupDao.selectGroupByGroupName(groupName);
        } catch (Exception e) {
            logger.error("Get group by name failed, group name = " + groupName, e);
            return new Group();
        }
    }

    @Transactional
    @Override
    public boolean addGroup(Group group) {
        groupDao.insertGroup(group);
        // 获取所有拥有超级管理员的账户
        List<User> superAdminList = groupUserDao.selectAllSuperAdmin();
        for (User superAdmin : superAdminList) {
            User user = new User();
            Integer grantGroupId = group.getGroupId();
            user.setUserId(superAdmin.getUserId());
            user.setUserRole(User.UserRole.SUPER_ADMIN);
            user.setGroupId(superAdmin.getGroupId());
            groupUserDao.insertGroupUser(user, grantGroupId);
        }

        return true;
    }

    @Override
    public boolean groupExist(String groupName) {
        try {
            Group group = groupDao.selectGroupByGroupName(groupName);
            return group != null;
        } catch (Exception e) {
            logger.error("Get group by group name failed, group name = " + groupName, e);
            return true;
        }
    }

    @Override
    public boolean updateGroup(Group group) {
        try {
            return groupDao.updateGroup(group) > 0;
        } catch (Exception e) {
            logger.error("Update group failed, " + group, e);
            return false;
        }
    }

    @Override
    public boolean deleteGroupById(Integer groupId) {
        try {
            int row = groupDao.deleteGroupById(groupId);
            return row > 0;
        } catch (Exception e) {
            logger.error("Delete group by id, group id = " + groupId, e);
            return false;
        }
    }
}
