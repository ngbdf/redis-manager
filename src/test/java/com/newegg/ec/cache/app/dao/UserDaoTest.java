package com.newegg.ec.cache.app.dao;

import com.newegg.ec.cache.Application;
import com.newegg.ec.cache.app.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Created by lzz on 2018/4/23.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class UserDaoTest {
    @Autowired
    private IUserDao userDao;

    @Test
    public void getUserList(){
        List<User> list = userDao.getUserList();
        System.out.println( list );
    }

    @Test
    public void addUser(){
        User user = new User("admin", "admin", "admin");
        userDao.addUser(user);
    }

    @Test
    public void updateUser(){
        User user = new User(30005,"30005", "30005", "30005");
        System.out.println(userDao.updateUser( user ));
    }
}
