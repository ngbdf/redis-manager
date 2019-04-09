package com.newegg.ec.cache.app.dao;

import com.newegg.ec.cache.app.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by gl49 on 2018/4/20.
 */
@Repository
public interface IUserDao {
    List<User> getUserList();

    User getUser(int id);

    void removeUser(int id);

    void addUser(User user);

    boolean updateUser(User user);

    List<String> getGroups();

    User getUserByName(String username);
}
