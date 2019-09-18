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

    @Select("SELECT " +
            "group.group_id AS group_id, " +
            "group.group_name AS group_name, " +
            "group.group_info AS group_info, " +
            "group.update_time AS update_time " +
            "FROM group, group_user " +
            "WHERE group_user.group_id = group.group_id " +
            "AND user_id = #{userId}")
    List<Group> selectGroupByUserId(Integer userId);

    @Insert("INSERT INFO group_user (group_id, user_id, user_role, update_time) " +
            "VALUES (#{groupId}, #{userId}, #{userRole}, NOW())")
    int insertGroupUser(User user);

    @Update("UPDATE user SET user_name = #{userName}, password = #{password}, #{user_role} = #{userRole}, " +
            "head_pic = #{headPic}, email = #{email}, mobile = #{mobile}, update_time = NOW()")
    int updateUserRole(User user);

    @Delete("DELETE FROM group_user WHERE group_id = #{groupId} AND user_id = #{userId}")
    int deleteGroupUser(@Param("groupId") Integer groupId, @Param("userId") Integer userId);

}
