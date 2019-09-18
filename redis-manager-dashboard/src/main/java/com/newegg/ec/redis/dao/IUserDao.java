package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Manage users
 *
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
@Mapper
public interface IUserDao {

    @Select("SELECT " +
            "user.user_id AS user_id, " +
            "user.group_id AS group_id, " +
            "user.user_name AS user_name, " +
            "user.password AS password, " +
            "user.token AS token, " +
            "group_user.user_role AS user_role, " +
            "user.head_pic AS head_pic, " +
            "user.email AS email, " +
            "user.mobile AS mobile, " +
            "user.user_type AS user_type, " +
            "user.update_time AS update_time " +
            "FROM user, group_user")
    List<User> selectAllUser();

    @Select("SELECT " +
            "user.user_id AS user_id, " +
            "user.group_id AS group_id, " +
            "user.user_name AS user_name, " +
            "user.password AS password, " +
            "user.token AS token, " +
            "group_user.user_role AS user_role, " +
            "user.head_pic AS head_pic, " +
            "user.email AS email, " +
            "user.mobile AS mobile, " +
            "user.user_type AS user_type, " +
            "user.update_time AS update_time " +
            "FROM user, group_user " +
            "WHERE group_user.group_id = user.group_id " +
            "AND group_user.group_id = #{groupId}")
    List<User> selectUserByGroupId(Integer groupId);

    @Select("SELECT * FROM user WHERE user_name = #{userName} AND password = #{password}")
    User selectUserByNameAndPassword(User user);

    @Select("SELECT * FROM user WHERE user_name = #{userName}")
    User selectUserByName(String userName);

    @Insert("INSERT INFO user (user_name, password, user_role, head_pic, email, mobile, update_time) " +
            "VALUES (#{userName}, #{password}, #{userRole}, #{headPic}, #{email}, #{mobile}, NOW())")
    int insertUser(User user);

    @Update("UPDATE user SET user_name = #{userName}, password = #{password}, #{user_role} = #{userRole}, " +
            "head_pic = #{headPic}, email = #{email}, mobile = #{mobile}, update_time = NOW()")
    int updateUser(User user);

    @Delete("DELETE FROM user WHERE user_id = #{userId}")
    int deleteUserById(Integer userId);

    @Delete("DELETE FROM user WHERE group_id = #{groupId}")
    int deleteUserByGroupId(Integer groupId);

    @Select("SELECT COUNT(user_id) WHERE group_id = #{groupId}")
    Integer getUserNumber(Integer groupId);
}
