package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.Group;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/9/2
 */
public interface IGroupService {

    List<Group> getAllGroup();

    List<Group> getGroupByUserId(Integer userId);

    Group getGroupById(Integer groupId);

    Group getGroupByName(String groupName);

    boolean addGroup(Group group);

    boolean groupExist(String groupName);

    boolean updateGroup(Group group);

    boolean deleteGroupById(Integer groupId);

}
