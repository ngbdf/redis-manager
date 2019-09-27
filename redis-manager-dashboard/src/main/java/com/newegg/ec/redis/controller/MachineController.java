package com.newegg.ec.redis.controller;

import ch.ethz.ssh2.Connection;
import com.newegg.ec.redis.entity.Machine;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.service.IMachineService;
import com.newegg.ec.redis.util.SSH2Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @RequestMapping(value = "/addMachineList", method = RequestMethod.POST)
    @ResponseBody
    public Result addMachineList(@RequestBody List<Machine> machineList) {
        boolean result = machineService.addMachineBatch(machineList);
        return result ? Result.successResult() : Result.failResult().setMessage("Save machine list failed.");
    }

    @RequestMapping(value = "/updateMachine", method = RequestMethod.POST)
    @ResponseBody
    public Result updateMachine(@RequestBody Machine machine) {
        return Result.successResult();
    }

    @RequestMapping(value = "/deleteMachine", method = RequestMethod.POST)
    @ResponseBody
    public Result deleteMachine(@RequestBody Machine machine) {
        return Result.successResult();
    }

    @RequestMapping(value = "/deleteMachineBatch", method = RequestMethod.POST)
    @ResponseBody
    public Result deleteMachineBatch(@RequestBody List<Machine> machineList) {
        return Result.successResult();
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
