package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/9/18
 */
@Mapper
public interface IGroupUserDao {

    @Select("SELECT * FROM group_user WHERE user_role = 0")
    List<User> selectAllSuperAdmin();

    @Select("SELECT * FROM group_user WHERE grant_group_id = #{grantGroupId} AND user_id = #{userId}")
    User isGranted(@Param("grantGroupId") Integer grantGroupId, @Param("userId") Integer userId);

    @Insert("INSERT INTO `group_user` (group_id, user_id, user_role, grant_group_id, update_time) " +
            "VALUES (#{user.groupId}, #{user.userId}, #{user.userRole}, #{grantGroupId}, NOW())")
    int insertGroupUser(@Param("user") User user, @Param("grantGroupId") Integer grantGroupId);

    @Update("UPDATE `group_user` " +
            "SET user_role = #{userRole} " +
            "WHERE group_id = #{groupId} " +
            "AND grant_group_id = #{groupId} " +
            "AND user_id = #{userId}")
    int updateUserRole(User user);

    @Delete("DELETE FROM `group_user` WHERE grant_group_id = #{grantGroupId} AND user_id = #{userId}")
    int deleteGroupUserByGrantGroupId(@Param("grantGroupId") Integer grantGroupId, @Param("userId") Integer userId);

    @Delete("DELETE FROM `group_user` WHERE user_id = #{userId}")
    int deleteGroupUserByUserId(@Param("userId") Integer userId);

    @Select("create TABLE IF NOT EXISTS `group_user`( " +
            "group_user_id integer(4) NOT NULL AUTO_INCREMENT, " +
            "group_id integer(4) NOT NULL, " +
            "user_id integer(4) NOT NULL, " +
            "grant_group_id integer(4) NOT NULL, " +
            "user_role varchar(20) NOT NULL, " +
            "update_time datetime(0) NOT NULL, " +
            "PRIMARY KEY (group_user_id), " +
            "UNIQUE KEY `group_user` (user_id, group_id, grant_group_id, user_role) " +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createGroupUserTable();
}
