package com.newegg.ec.cache.module.usermanager;

import com.newegg.ec.cache.core.entity.model.User;
import com.newegg.ec.cache.dao.IUserDao;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by gl49 on 2018/4/20.
 */
@Component
public class UserService implements IUserService{

    private static final Log logger = LogFactory.getLog(UserService.class);

    @Autowired
    private IUserDao userDao;

    @Override
    public User getUser(int id) {
        return userDao.getUser(id);
    }

    @Override
    public User getUser(String username) {
        return userDao.getUserByName(username);
    }

    @Override
    public List<User> getUserList() {
        return userDao.getUserList();
    }

    @Override
    public List<String> getGroups() {
        return userDao.getGroups();
    }

    @Override
    public boolean removeUser(int id) {
        boolean res = false;
        try {
            userDao.removeUser(id);
            res = true;
        } catch (Exception e) {

        }
        return res;
    }

    @Override
    public boolean addUser(JSONObject user) {
        boolean res = false;
        try {
            if(user.containsKey("userId")){
               return userDao.updateUser(new User(Integer.parseInt(user.getString("userId")),user.getString("username"),user.getString("password"),user.getString("group")));
            }else {
                userDao.addUser(new User(user.getString("username"),user.getString("password"),user.getString("group")));
            }
            res = true;
        } catch (Exception e) {
            logger.error("Add User error",e);
        }
        return res;
    }

}
