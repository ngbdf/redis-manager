package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.Machine;

import java.util.List;

/**
 * Manage machines
 *
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
public interface IMachineDao {

    List<Machine> selectAllMachine();

    List<Machine> selectMachineByGroupId(int groupId);

    List<Machine> selectMachineByMachineGroup(String machineGroupName);

    List<Machine> selectMachineByIds(List<Integer> machineIdList);

    int insertMachine(Machine machine);

    int insertMachineBatch(List<Machine> machineList);

    int deleteMachineById(int machineId);

    int deleteMachineByIdBatch(List<Integer>  machineIdList);

}
