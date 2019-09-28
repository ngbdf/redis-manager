package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.Machine;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Manage machines
 *
 * @author Jay.H.Zou
 * @date 7/19/2019
 */
@Mapper
public interface IMachineDao {

    @Select("SELECT * FROM machine WHERE group_id = #{groupId}")
    List<Machine> selectMachineByGroupId(Integer groupId);

    @Select("SELECT DISTINCT machine_group_name FROM machine WHERE group_id = #{groupId}")
    List<String> getMachineGroupNameList(Integer groupId);

    @Select("SELECT * FROM machine WHERE machine_group_name = #{machineGroupName}")
    List<Machine> selectMachineByMachineGroup(String machineGroupName);

    @Select("<script>" +
            "SELECT * FROM machine WHERE machine_id IN " +
            "<foreach item='machineId' collection='machineIdList' open='(' separator=',' close=')'>" +
            "#{machineId}" +
            "</foreach>)" +
            "</script>")
    List<Machine> selectMachineByIds(List<Integer> machineIdList);

    @Update("UPDATE machine SET machine_group_name = #{machineGroupName}, host = #{host}, password = #{password}, " +
            "token = #{token}, machine_type = #{machineType}, machine_info = #{machineInfo}, update_time = NOW() " +
            "WHERE machine_id = #{machineId}")
    int updateMachine(Machine machine);

    @Insert("<script>" +
            "INSERT INTO machine (machine_group_name, group_id, host, user_name, password, token, machine_type, machine_info, update_time) " +
            "VALUES <foreach item='machine' collection='machineList' separator=','>" +
            "(#{machine.machineGroupName}, #{machine.groupId}, #{machine.host}, #{machine.userName}, #{machine.password}, " +
            "#{machine.token}, #{machine.machineType}, #{machine.machineInfo}, NOW())" +
            "</foreach>" +
            "</script>")
    int insertMachine(@Param("machineList") List<Machine> machineList);

    @Delete("DELETE FROM machine WHERE machine_id = #{machineId}")
    int deleteMachineById(Integer machineId);

    @Delete("<script>" +
            "DELETE FROM machine WHERE machine_id IN (" +
            "<foreach item='machineId' collection='machineIdList' open='(' separator=',' close=')'>" +
            "#{machineId}" +
            "</foreach>)" +
            "</script>")
    int deleteMachineByIdBatch(@Param("machineIdList") List<Integer>  machineIdList);

}
