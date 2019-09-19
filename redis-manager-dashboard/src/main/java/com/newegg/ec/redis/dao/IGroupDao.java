package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.Group;
import org.apache.ibatis.annotations.*;

import javax.ws.rs.DELETE;
import java.util.List;

/**
 * Manage group
 *
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
@Mapper
public interface IGroupDao {

    @Select("SELECT * FROM group")
    List<Group> selectAllGroup();

    @Select("SELECT " +
            "group.group_id AS group_id, " +
            "group.group_name AS group_name, " +
            "group.group_info AS group_info, " +
            "group.update_time AS update_time " +
            "FROM group, group_user " +
            "WHERE group_user.group_id = group.group_id " +
            "AND group_user.user_id = #{userId}")
    List<Group> selectGroupByUserId(Integer userId);

    @Select("SELECT * FROM group WHERE group_name = #{groupName}")
    Group selectGroupByGroupName(String groupName);

    @Select("SELECT * FROM group WHERE group_id = #{groupId}")
    Group selectGroupById(Integer groupId);

    @Insert("INSERT INTO group (group_id, group_name, group_info, update_time) " +
            "VALUES (#{groupId}, #{groupName}, #{groupInfo}, NOW())")
    int insertGroup(Group group);

    @Update("UPDATE group SET group_name = #{groupName}, group_info = #{groupInfo}, update_time = NOW() WHERE group_id = #{groupId}")
    int updateGroup(Group group);

    @Delete("DELETE FROM group WHERE group_id = #{groupId}")
    int deleteGroupById(Integer groupId);

}
