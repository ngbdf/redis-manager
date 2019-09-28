package com.newegg.ec.redis.controller;

import ch.ethz.ssh2.Connection;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.service.IMachineService;
import com.newegg.ec.redis.util.SSH2Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Jay.H.Zou
 * @date 7/20/2019
 */
@RequestMapping("/machine/*")
@Controller
public class MachineController {

    @Autowired
    private IMachineService machineService;

    @RequestMapping(value = "/getMachineGroupNameList/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getMachineGroupNameList(@PathVariable("groupId") Integer groupId) {
        List<String> machineGroupNameList = machineService.getMachineGroupNameList(groupId);
        boolean result = machineGroupNameList != null;
        return result ? Result.successResult(machineGroupNameList) : Result.failResult().setMessage("Get machine group name list failed.");
    }

    @RequestMapping(value = "/getMachineList/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getMachineList(@PathVariable("groupId") Integer groupId) {
        List<Machine> machineList = machineService.getMachineByGroupId(groupId);
        return machineList != null ? Result.successResult(machineList) : Result.failResult().setMessage("Get machine list failed.");
    }

    @RequestMapping(value = "/getHierarchyMachineList/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Result getHierarchyMachineList(@PathVariable("groupId") Integer groupId) {
        List<Machine> machineList = machineService.getMachineByGroupId(groupId);
        if (machineList == null) {
          return Result.failResult().setMessage("Get machine list failed.");
        }
        Multimap<String, Machine> hierarchyMachineMap = ArrayListMultimap.create();
        machineList.forEach(machine -> hierarchyMachineMap.put(machine.getMachineGroupName(), machine));
        List<JSONObject> result = new ArrayList<>();
        hierarchyMachineMap.keySet().forEach(machineGroupName -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("machineGroupName", machineGroupName);
            jsonObject.put("children", hierarchyMachineMap.get(machineGroupName));
            result.add(jsonObject);
        });
        return Result.successResult(result);
    }

    @RequestMapping(value = "/addMachineList", method = RequestMethod.POST)
    @ResponseBody
    public Result addMachineList(@RequestBody List<Machine> machineList) {
        boolean result = machineService.addMachineBatch(machineList);
        return result ? Result.successResult() : Result.failResult().setMessage("Save machine list failed.");
    }

    @RequestMapping(value = "/updateMachine", method = RequestMethod.POST)
    @ResponseBody
    public Result updateMachine(@RequestBody List<Machine> machineList) {
        if (machineList == null || machineList.isEmpty()) {
            return Result.failResult().setMessage("Update machine failed, cause machine list is empty.");
        }
        Machine machine = machineList.get(0);
        boolean result = machineService.updateMachine(machine);
        return result ? Result.successResult() : Result.failResult().setMessage("Update machine failed.");
    }

    @RequestMapping(value = "/deleteMachine", method = RequestMethod.POST)
    @ResponseBody
    public Result deleteMachine(@RequestBody Machine machine) {
        boolean result = machineService.deleteMachineById(machine.getMachineId());
        return result ? Result.successResult() : Result.failResult().setMessage("Delete machine failed.");
    }

    @RequestMapping(value = "/deleteMachineBatch", method = RequestMethod.POST)
    @ResponseBody
    public Result deleteMachineBatch(@RequestBody List<Machine> machineList) {
        List<Integer> machineIdList = new ArrayList<>();
        machineList.forEach(machine -> machineIdList.add(machine.getMachineId()));
        boolean result = machineService.deleteMachineByIdBatch(machineIdList);
        return result ? Result.successResult() : Result.failResult().setMessage("Delete machine list failed.");
    }

    @RequestMapping(value = "/validateConnection", method = RequestMethod.POST)
    @ResponseBody
    public Result validateHost(@RequestBody Machine machine) {
        try {
            SSH2Util.getConnection(machine);
        } catch (Exception e) {
            return Result.failResult().setMessage("Connection refused");
        }
        return Result.successResult();
    }

}
