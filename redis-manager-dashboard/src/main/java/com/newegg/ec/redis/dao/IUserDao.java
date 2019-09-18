package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Manage users
 *
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public interface IUserDao {

    @Select("SELECT * FROM user")
    List<User> selectAllUser();

    @Select("SELECT * FROM user WHERE group_id = #{groupId}")
    List<User> selectUserByGroupId(int groupId);

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
    int deleteUserById(int userId);

    @Delete("DELETE FROM user WHERE group_id = #{groupId}")
    int deleteUserByGroupId(int groupId);

    @Select("SELECT COUNT(user_id) WHERE group_id = #{groupId}")
    Integer getUserNumber(Integer groupId);
}
