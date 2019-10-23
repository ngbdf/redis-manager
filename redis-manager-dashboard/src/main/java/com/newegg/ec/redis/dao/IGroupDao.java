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

    @Select("SELECT * FROM `group`")
    List<Group> selectAllGroup();

    @Select("<script>" +
            "SELECT " +
            "distinct group.group_id AS group_id, " +
            "group.group_name AS group_name, " +
            "group.group_info AS group_info, " +
            "group.update_time AS update_time " +
            "FROM `group`, group_user " +
            "WHERE group_user.grant_group_id = group.group_id " +
            "<if test='userId != null'>" +
            "AND group_user.user_id = #{userId} " +
            "</if>" +
            "ORDER BY group_name" +
            "</script>")
    List<Group> selectGroupByUserId(@Param("userId") Integer userId);

    @Select("SELECT * FROM `group` WHERE group_name = #{groupName}")
    Group selectGroupByGroupName(String groupName);

    @Select("SELECT * FROM `group` WHERE group_id = #{groupId}")
    Group selectGroupById(Integer groupId);

    @Insert("INSERT INTO `group` (group_name, group_info, update_time) " +
            "VALUES (#{groupName}, #{groupInfo}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "groupId", keyColumn = "group_id")
    int insertGroup(Group group);

    @Update("UPDATE `group` SET group_name = #{groupName}, group_info = #{groupInfo}, update_time = NOW() WHERE group_id = #{groupId}")
    int updateGroup(Group group);

    @Delete("DELETE FROM `group` WHERE group_id = #{groupId}")
    int deleteGroupById(Integer groupId);

    @Select("create TABLE IF NOT EXISTS `group` (" +
            "group_id integer(4) NOT NULL AUTO_INCREMENT, " +
            "group_name varchar(255) NOT NULL, " +
            "group_info varchar(255) DEFAULT NULL, " +
            "update_time datetime(0) NOT NULL, " +
            "PRIMARY KEY (group_id), " +
            "UNIQUE KEY (group_name) " +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createGroupTable();
}
