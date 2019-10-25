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
            "group_user.user_role AS user_role, " +
            "user.avatar AS avatar, " +
            "user.email AS email, " +
            "user.mobile AS mobile, " +
            "user.update_time AS update_time " +
            "FROM user, group_user " +
            "WHERE group_user.group_id = user.group_id " +
            "AND group_user.grant_group_id = user.group_id " +
            "AND group_user.user_id = user.user_id " +
            "AND user.user_id = #{userId}")
    User selectUserById(Integer userId);

    @Select("SELECT " +
            "user.user_id AS user_id, " +
            "user.group_id AS group_id, " +
            "user.user_name AS user_name, " +
            "group_user.user_role AS user_role, " +
            "user.avatar AS avatar, " +
            "user.email AS email, " +
            "user.mobile AS mobile, " +
            "user.update_time AS update_time " +
            "FROM user, group_user " +
            "WHERE group_user.group_id = user.group_id " +
            "AND group_user.user_id = user.user_id " +
            "AND group_user.group_id = #{groupId} " +
            "AND group_user.grant_group_id = #{groupId}")
    List<User> selectUserByGroupId(Integer groupId);

    @Select("SELECT " +
            "user.user_id AS user_id, " +
            "group_user.grant_group_id AS group_id, " +
            "user.user_name AS user_name, " +
            "group_user.user_role AS user_role, " +
            "user.avatar AS avatar, " +
            "user.email AS email, " +
            "user.mobile AS mobile, " +
            "user.update_time AS update_time " +
            "FROM group_user, user " +
            "WHERE group_user.group_id = user.group_id " +
            "AND group_user.user_id = user.user_id " +
            "AND grant_group_id = #{grantGroupId} " +
            "AND group_user.group_id != #{grantGroupId}")
    List<User> selectGrantUserByGroupId(Integer grantGroupId);

    @Select("SELECT " +
            "user.user_id AS user_id, " +
            "user.group_id AS group_id, " +
            "group_user.user_role AS user_role " +
            "FROM user, group_user " +
            "WHERE group_user.user_id = user.user_id " +
            "AND group_user.group_id = user.group_id " +
            "AND group_user.grant_group_id = #{grantGroupId} " +
            "AND group_user.user_id = #{userId}")
    User selectUserRole(@Param("grantGroupId") Integer grantGroupId, @Param("userId") Integer userId);

    @Select("SELECT * FROM `user`WHERE user_name = #{userName} AND password = #{password}")
    User selectUserByNameAndPassword(User user);

    @Select("SELECT * FROM user WHERE user_name = #{userName}")
    User selectUserByName(String userName);

    @Select("SELECT COUNT(user_id) FROM user WHERE group_id = #{groupId}")
    int selectUserNumber(Integer groupId);

    @Insert("INSERT INTO user (group_id, user_name, password, avatar, email, mobile, update_time) " +
            "VALUES (#{groupId}, #{userName}, #{password}, #{avatar}, #{email}, #{mobile}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    int insertUser(User user);

    @Update("UPDATE user SET user_name = #{userName}, password = #{password}, " +
            "avatar = #{avatar}, email = #{email}, mobile = #{mobile}, update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int updateUser(User user);

    @Delete("DELETE FROM user WHERE user_id = #{userId}")
    int deleteUserById(Integer userId);

    @Delete("DELETE FROM user WHERE group_id = #{groupId}")
    int deleteUserByGroupId(Integer groupId);

    @Select("create TABLE IF NOT EXISTS `user` ( " +
            "user_id integer(4) NOT NULL AUTO_INCREMENT, " +
            "group_id integer(4) NOT NULL, " +
            "user_name varchar(255) NOT NULL, " +
            "password varchar(255) DEFAULT NULL, " +
            "token varchar(255) DEFAULT NULL, " +
            "avatar varchar(255) DEFAULT NULL, " +
            "email varchar(255) DEFAULT NULL, " +
            "mobile varchar(20) DEFAULT NULL, " +
            "update_time datetime(0) NOT NULL, " +
            "PRIMARY KEY (user_id), " +
            "UNIQUE KEY `user_name` (user_name) " +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createUserTable();

}
