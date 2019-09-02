package com.newegg.ec.redis.service.impl;

import com.newegg.ec.redis.dao.IUserDao;
import com.newegg.ec.redis.entity.User;
import com.newegg.ec.redis.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/9/2
 */
public class UserService implements IUserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private IUserDao userDao;

    @Override
    public List<User> getAllUser() {
        try {
            return userDao.selectAllUser();
        } catch (Exception e) {
            logger.error("Get all user failed.", e);
            return null;
        }
    }

    @Override
    public List<User> getUserByGroupId(int groupId) {
        try {
            return userDao.selectUserByGroupId(groupId);
        } catch (Exception e) {
            logger.error("");
            return null;
        }
    }

    @Override
    public User getUserByNameAndPassword(User user) {
        try {
            return userDao.selectUserByNameAndPassword(user);
        } catch (Exception e) {
            logger.error("Get user by user name and password failed, " + user, e);
            return null;
        }
    }

    @Override
    public boolean addUser(User user) {
        try {
            int row = userDao.insertUser(user);
            return row > 0;
        } catch (Exception e) {
            logger.error("Add user failed, " + user, e);
            return false;
        }
    }

    @Override
    public boolean userNameExist(String userName) {
        try {
            User user = userDao.selectUserByName(userName);
            return user != null;
        } catch (Exception e) {
            logger.error("Get user by user name failed, user name = " + userName, e);
            return true;
        }
    }

    @Override
    public boolean updateUser(User user) {
        try {
            int row = userDao.updateUser(user);
            return row > 0;
        } catch (Exception e) {
            logger.error("Update user failed, " + user, e);
            return false;
        }
    }

    @Override
    public boolean deleteUserById(int userId) {
        try {
            int row = userDao.deleteUserById(userId);
            return row > 0;
        } catch (Exception e) {
            logger.error("Delete user by user id failed, user id = " + userId, e);
            return false;
        }
    }

    @Override
    public boolean deleteUserByGroupId(int groupId) {
        try {
            userDao.deleteUserByGroupId(groupId);
            return true;
        } catch (Exception e) {
            logger.error("Delete users by group id failed, group id = " + groupId, e);
            return false;
        }
    }
}
