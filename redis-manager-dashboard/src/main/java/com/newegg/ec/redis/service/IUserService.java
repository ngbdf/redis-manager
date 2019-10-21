package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.User;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public interface IUserService {

    List<User> getAllUser();

    List<User> getUserByGroupId(Integer groupId);

    User getUserByNameAndPassword(User user);

    User getUserById(Integer userId);

    User getUserRole(Integer groupId, Integer userId);

    boolean addUser(User user);

    User getUserByName(String userName);

    boolean updateUser(User user);

    boolean deleteUserById(Integer userId);

    boolean deleteUserByGroupId(Integer groupId);

    Integer getUserNumber(Integer groupId);
}
