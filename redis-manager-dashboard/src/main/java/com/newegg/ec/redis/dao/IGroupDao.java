package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.Group;

import java.util.List;

/**
 * Manage group
 *
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public interface IGroupDao {

    List<Group> selectAllGroup();

    List<Group> selectGroupByUserId(String userId);

    Group getGroupById(int groupId);

    int insertGroup(Group group);

    int updateGroup(Group group);

    int deleteGroupById(int groupId);

}
