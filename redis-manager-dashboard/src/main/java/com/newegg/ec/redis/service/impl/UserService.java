package com.newegg.ec.redis.service.impl;

import com.newegg.ec.redis.dao.IGroupUserDao;
import com.newegg.ec.redis.dao.IUserDao;
import com.newegg.ec.redis.entity.User;
import com.newegg.ec.redis.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/9/2
 */
@Service
public class UserService implements IUserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private IUserDao userDao;

    @Autowired
    private IGroupUserDao groupUserDao;

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
    public List<User> getUserByGroupId(Integer groupId) {
        try {
            return userDao.selectUserByGroupId(groupId);
        } catch (Exception e) {
            logger.error("Get user by group id failed.", e);
            return null;
        }
    }

    @Override
    public User getUserByNameAndPassword(User user) {
        try {
            User userLogin = userDao.selectUserByNameAndPassword(user);
            if (userLogin == null) {
                return null;
            }
            User userRole = getUserRole(userLogin.getGroupId(), userLogin.getUserId());
            userLogin.setUserRole(userRole.getUserRole());
            userLogin.setPassword(null);
            userLogin.setToken(null);
            return userLogin;
        } catch (Exception e) {
            logger.error("Get user by user name and password failed, " + user, e);
            return null;
        }
    }

    @Override
    public User getUserById(Integer userId) {
        try {
            return userDao.selectUserById(userId);
        } catch (Exception e) {
            logger.error("Get user by id failed.", e);
            return null;
        }
    }

    @Override
    public User getUserRole(Integer groupId, Integer userId) {
        try {
            return userDao.selectUserRole(groupId, userId);
        } catch (Exception e) {
            logger.error("Get user by group id and user id failed.", e);
            return null;
        }
    }

    @Transactional
    @Override
    public boolean addUser(User user) {
        userDao.insertUser(user);
        groupUserDao.insertGroupUser(user);
        return true;
    }

    @Override
    public User getUserByName(String userName) {
        try {
            return userDao.selectUserByName(userName);
        } catch (Exception e) {
            logger.error("Get user by user name failed, user name = " + userName, e);
            return new User();
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

    @Transactional
    @Override
    public boolean deleteUserById(Integer userId) {
         userDao.deleteUserById(userId);
         groupUserDao.deleteGroupUserByUserId(userId);
         return true;
    }

    @Override
    public boolean deleteUserByGroupId(Integer groupId) {
        try {
            userDao.deleteUserByGroupId(groupId);
            return true;
        } catch (Exception e) {
            logger.error("Delete users by group id failed, group id = " + groupId, e);
            return false;
        }
    }

    @Override
    public Integer getUserNumber(Integer groupId) {
        if (groupId == null) {
            return 0;
        }
        try {
            return userDao.selectUserNumber(groupId);
        } catch (Exception e) {
            logger.error("Get user number failed, ", e);
            return 0;
        }
    }
}
