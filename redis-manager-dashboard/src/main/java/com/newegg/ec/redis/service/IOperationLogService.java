package com.newegg.ec.redis.service;

import com.newegg.ec.redis.entity.OperationLog;

import java.util.Map;

/**
 * @author fw13
 * @date 2019/11/21 13:47
 */
public interface IOperationLogService {

    Map<String, Object> getLogsByGroupId(Integer groupId, Integer pageNo, Integer pageSize);

    int insertLog(OperationLog operationLog);
}
