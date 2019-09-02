package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.Group;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 2019/9/2
 */
public interface IGroupService {

    List<Group> getAllGroup();

    List<Group> getGroupByUserId(String userId);

    Group getGroupById(int groupId);

    boolean addGroup(Group group);

    boolean updateGroup(Group group);

    boolean deleteGroupById(String groupId);

}
