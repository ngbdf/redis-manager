package com.newegg.ec.cache.app.logic;

import com.newegg.ec.cache.app.dao.IUserDao;
import com.newegg.ec.cache.app.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Created by gl49 on 2018/4/20.
 */
@Component
public class UserLogic {
    @Autowired
    private IUserDao userDao;

    public User getUser(int id){
        return userDao.getUser( id );
    }

    public User getUser(String username){
        return userDao.getUserByName( username );
    }

    public List<User> getUserList(){
        return userDao.getUserList();
    }

    public List<String> getGroups(){
        return userDao.getGroups();
    }

    public boolean removeUser(int id){
        boolean res = false;
        try {
            userDao.removeUser( id );
            res = true;
        }catch (Exception e){

        }
        return res;
    }

    public boolean addUser(User user){
        boolean res = false;
        try {
            userDao.addUser( user );
            res = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return res;
    }

}
