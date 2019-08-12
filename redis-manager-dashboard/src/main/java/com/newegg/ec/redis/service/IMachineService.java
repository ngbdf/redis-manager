package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.Machine;

import java.util.List;

/**
 * @author Jay.H.Zou
 * @date 7/26/2019
 */
public interface IMachineService {

    List<Machine> getAllMachine();

    /**
     * 获取多个集群集群
     *
     * @param machineGroup
     * @return
     */
    List<List<Machine>> getMachineByMachineListByGroupId(String machineGroup);

    /**
     * 获取一个机器集群
     *
     * @param machineGroup
     * @return
     */
    List<Machine> getMachineByMachineGroup(String machineGroup);

    List<Machine> getMachineListByIds(List<String> machineIdList);

    List<Machine> checkMachineConnection(List<String> machineIdList);

    int addMachine(Machine machine);

    int addMachineBatch(List<Machine> machineList);

    int deleteMachineById(String machineId);

    int deleteMachineByIdBatch(List<String> machineIdList);

}
