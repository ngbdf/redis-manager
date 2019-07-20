package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.User;

/**
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public interface IUserService {

    boolean addUser(User user);

    boolean updateUser(User user);

    boolean deleteUser(User user);

}
