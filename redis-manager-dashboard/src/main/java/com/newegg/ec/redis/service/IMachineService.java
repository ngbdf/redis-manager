package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.Machine;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
public interface IMachineService {

    List<Machine> getAllMachine();

    List<Machine> getMachineByGroupId(String groupId);

    int addMachine(Machine machine);

    int addMachineBatch(List<Machine> machineList);

    int deleteMachineById(String machineId);

    int deleteMachineByIdBatch(List<String>  machineIdList);

}
