package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.Group;
import com.newegg.ec.redis.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/9/18
 */
@Mapper
public interface IGroupUserDao {

    @Insert("INSERT INFO group_user (group_id, user_id, user_role, update_time) " +
            "VALUES (#{groupId}, #{userId}, #{userRole}, NOW())")
    int insertGroupUser(User user);

    @Update("UPDATE user SET #{user_role} = #{userRole} WHERE group_id = #{groupId} AND user_id = #{userId}")
    int updateUserRole(User user);

    @Delete("DELETE FROM group_user WHERE group_id = #{groupId} AND user_id = #{userId}")
    int deleteGroupUser(@Param("groupId") Integer groupId, @Param("userId") Integer userId);

    @Delete("DELETE FROM group_user WHERE user_id = #{userId}")
    int deleteGroupUserByUserId( @Param("userId") Integer userId);

    @Delete("DELETE FROM group_user WHERE user_id = #{userId}")
    int deleteGroupUserByGroupId( @Param("groupId") Integer groupId);

    @Select("SELECT COUNT(user_id) FROM group_user WHERE group_id = #{groupId}")
    Integer getUserNumber(Integer groupId);
}
