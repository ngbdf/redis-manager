package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.User;
import org.apache.ibatis.annotations.*;

/**
 * @author Jay.H.Zou
 * @date 2019/9/18
 */
@Mapper
public interface IGroupUserDao {

    @Insert("INSERT INTO `group_user` (group_id, user_id, user_role, update_time) " +
            "VALUES (#{groupId}, #{userId}, #{userRole}, NOW())")
    int insertGroupUser(User user);

    @Update("UPDATE `group_user` SET #{user_role} = #{userRole} WHERE group_id = #{groupId} AND user_id = #{userId}")
    int updateUserRole(User user);

    @Delete("DELETE FROM `group_user` WHERE group_id = #{groupId} AND user_id = #{userId}")
    int deleteGroupUser(@Param("groupId") Integer groupId, @Param("userId") Integer userId);

    @Delete("DELETE FROM `group_user` WHERE user_id = #{userId}")
    int deleteGroupUserByUserId(@Param("userId") Integer userId);

    @Delete("DELETE FROM `group_user` WHERE user_id = #{userId}")
    int deleteGroupUserByGroupId(@Param("groupId") Integer groupId);

    @Select("SELECT COUNT(user_id) FROM `group_user` WHERE group_id = #{groupId}")
    Integer getUserNumber(Integer groupId);

    @Select("create TABLE IF NOT EXISTS `group_user`( " +
            "group_user_id integer(4) NOT NULL AUTO_INCREMENT, " +
            "group_id integer(4) NOT NULL, " +
            "user_id integer(4) NOT NULL, " +
            "user_role varchar(20) NOT NULL, " +
            "update_time datetime(0) NOT NULL, " +
            "PRIMARY KEY (group_user_id) " +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createGroupUserTable();
}
