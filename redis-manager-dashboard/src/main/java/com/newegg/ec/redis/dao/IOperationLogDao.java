package com.newegg.ec.redis.dao;

import com.newegg.ec.redis.entity.OperationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author fw13
 * @date 2019/11/20 18:06
 */
@Mapper
public interface IOperationLogDao {

    @Select("create TABLE IF NOT EXISTS `operation_log`( " +
            "log_id integer NOT NULL AUTO_INCREMENT, " +
            "group_id integer(4) NOT NULL, " +
            "operation_group_id integer(4) NOT NULL, " +
            "user_id integer(4) NOT NULL, " +
            "user_ip varchar(50) DEFAULT NULL, " +
            "operation_info varchar(50) DEFAULT NULL, " +
            "request_params TEXT DEFAULT NULL, " +
            "log_time datetime(0) NOT NULL, " +
            "PRIMARY KEY (log_id) " +
            ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;")
    void createLogTable();

    @Select(" select a.log_id,a.user_id,b.user_name,c.group_name,a.operation_info,a.user_ip,a.log_time" +
            " from operation_log as a left join user as b on (a.user_id = b.user_id) left join `group` as c on (a.group_id = c.group_id) " +
            "where a.operation_group_id = #{groupId} order by a.log_time desc")
    List<OperationLog> selectLogsByOperationGroupId(Integer groupId);

    @Insert("INSERT INTO operation_log(group_id, user_id, user_ip, operation_group_id, operation_info, request_params, log_time) " +
            "VALUES (#{groupId}, #{userId}, #{userIp}, #{operationGroupId}, #{operationInfo}, #{requestParams}, #{logTime})")
    @Options(useGeneratedKeys = true, keyProperty = "logId", keyColumn = "log_id")
    int insertLog(OperationLog log);

}
