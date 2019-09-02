package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.User;

import java.util.List;

/**
 * Manage users
 *
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public interface IUserDao {

    List<User> selectAllUser();

    List<User> selectUserByGroupId(int groupId);

    User selectUserByNameAndPassword(User user);

    User selectUserByName(String userName);

    int insertUser(User user);

    int updateUser(User user);

    int deleteUserById(int userId);

    int deleteUserByGroupId(int groupId);
}
