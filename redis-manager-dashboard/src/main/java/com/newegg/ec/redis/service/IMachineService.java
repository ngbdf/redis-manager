package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.Machine;

import java.util.List;
import java.util.Map;

/**
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
public interface IMachineService {

    List<String> getMachineGroupNameList(Integer groupId);

    /**
     * 获取多个集群
     *
     * @param groupId
     * @return
     */
    Map<String, List<Machine>> getMachineWithGroup(Integer groupId);

    List<Machine> getMachineByGroupId(Integer groupId);

    /**
     * 获取一个机器集群
     *
     * @param machineGroupName
     * @return
     */
    List<Machine> getMachineByMachineGroup(String machineGroupName);

    List<Machine> getMachineListByIds(List<Integer> machineIdList);

    /**
     *
     * @param machineIdList
     * @return connection refused list
     */
    List<Machine> checkMachineConnection(List<Integer> machineIdList);

    boolean addMachineBatch(List<Machine> machineList);

    boolean deleteMachineById(Integer machineId);

    boolean deleteMachineByIdBatch(List<Integer> machineIdList);

}
