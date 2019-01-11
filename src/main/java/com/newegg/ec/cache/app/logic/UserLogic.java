package com.newegg.ec.cache.app.logic;

import com.newegg.ec.cache.app.dao.IUserDao;
import com.newegg.ec.cache.app.model.User;
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
public class UserLogic {

    private static final Log logger = LogFactory.getLog(UserLogic.class);

    @Autowired
    private IUserDao userDao;

    public User getUser(int id) {
        return userDao.getUser(id);
    }

    public User getUser(String username) {
        return userDao.getUserByName(username);
    }

    public List<User> getUserList() {
        return userDao.getUserList();
    }

    public List<String> getGroups() {
        return userDao.getGroups();
    }

    public boolean removeUser(int id) {
        boolean res = false;
        try {
            userDao.removeUser(id);
            res = true;
        } catch (Exception e) {

        }
        return res;
    }

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
