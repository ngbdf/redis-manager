package com.newegg.ec.cache.module.usermanager;

import com.newegg.ec.cache.core.entity.model.User;
import net.sf.json.JSONObject;

import java.util.List;

/**
 * Created by lf52 on 2019/2/25.
 */
public interface IUserService {

    public User getUser(int id);

    public User getUser(String username);

    public List<User> getUserList();

    public List<String> getGroups();

    public boolean removeUser(int id);

    public boolean addUser(JSONObject user);
}
