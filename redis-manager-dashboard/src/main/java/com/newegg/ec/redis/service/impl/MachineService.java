package com.newegg.ec.redis.service.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.newegg.ec.redis.dao.IMachineDao;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.service.IMachineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Jay.H.Zou
 * @date 2019/9/2
 */
@Service
public class MachineService implements IMachineService {

    private static final Logger logger = LoggerFactory.getLogger(MachineService.class);

    @Autowired
    private IMachineDao machineDao;

    @Override
    public List<String> getMachineGroupNameList(Integer groupId) {
        try {
            return machineDao.getMachineGroupNameList(groupId);
        } catch (Exception e) {
            logger.error("Get machine group name list failed.", e);
            return null;
        }
    }

    @Override
    public Map<String, List<Machine>> getMachineWithGroup(Integer groupId) {
        try {
            List<Machine> machines = machineDao.selectMachineByGroupId(groupId);
            if (machines == null || machines.isEmpty()) {
                return null;
            }
            Multimap<String, Machine> machineMultimap = ArrayListMultimap.create();
            machines.forEach(machine -> machineMultimap.put(machine.getMachineGroupName(), machine));
            Map<String, List<Machine>> machineMap = new LinkedHashMap<>();
            Set<String> machineGroupNameSet = machineMultimap.keySet();
            machineGroupNameSet.forEach(machineGroupName -> machineMap.put(machineGroupName, new ArrayList<>(machineMultimap.get(machineGroupName))));
            return machineMap;
        } catch (Exception e) {
            logger.error("Get machine by group id failed, group id = " + groupId, e);
            return null;
        }
    }

    @Override
    public List<Machine> getMachineByGroupId(Integer groupId) {
        try {
            List<Machine> machineList = machineDao.selectMachineByGroupId(groupId);
            if (machineList == null) {
                return null;
            }
            return machineList;
        } catch (Exception e) {
            logger.error("Get machine by group id failed, group id = " + groupId, e);
            return null;
        }
    }

    @Override
    public List<Machine> getMachineByMachineGroup(String machineGroupName) {
        try {
            return machineDao.selectMachineByMachineGroup(machineGroupName);
        } catch (Exception e) {
            logger.error("Get machine by machine group name failed, machine group name = " + machineGroupName, e);
            return null;
        }
    }

    @Override
    public List<Machine> getMachineListByIds(List<Integer> machineIdList) {
        try {
            return machineDao.selectMachineByIds(machineIdList);
        } catch (Exception e) {
            logger.error("Get machine by id list failed, " + machineIdList, e);
            return null;
        }
    }

    @Override
    public Machine getMachineByHost(Integer groupId, String host) {
        try {
            List<Machine> machines = machineDao.selectMachineByHost(groupId, host);
            if (machines == null || machines.isEmpty()) {
                return null;
            }
            return machines.get(0);
        } catch (Exception e) {
            logger.error("Get machine by host failed, host: " + host, e);
            return null;
        }
    }

    @Override
    public List<Machine> checkMachineConnection(List<Integer> machineIdList) {
        try {
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }

    @Override
    public boolean addMachineBatch(List<Machine> machineList) {
        try {
            machineDao.insertMachine(machineList);
            return true;
        } catch (Exception e) {
            logger.error("Add batch machine failed, " + machineList, e);
            return false;
        }
    }

    @Override
    public boolean updateMachine(Machine machine) {
        try {
             return machineDao.updateMachine(machine) > 0;
        } catch (Exception e) {
            logger.error("Update machine failed, " + machine, e);
            return false;
        }
    }

    @Override
    public boolean deleteMachineById(Integer machineId) {
        try {
            int row = machineDao.deleteMachineById(machineId);
            return row > 0;
        } catch (Exception e) {
            logger.error("Delete machine by id failed, machine id = " + machineId, e);
            return false;
        }
    }

    @Override
    public boolean deleteMachineByIdBatch(List<Integer> machineIdList) {
        try {
            machineDao.deleteMachineByIdBatch(machineIdList);
            return true;
        } catch (Exception e) {
            logger.error("Delete batch machine failed, " + machineIdList, e);
            return false;
        }
    }
}
