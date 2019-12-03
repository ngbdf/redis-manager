package com.newegg.ec.redis.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.newegg.ec.redis.dao.IOperationLogDao;
import com.newegg.ec.redis.entity.OperationLog;
import com.newegg.ec.redis.service.IOperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fw13
 * @date 2019/11/21 14:37
 */
@Component
public class OperationLogService implements IOperationLogService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IOperationLogDao operationLogDao;

    @Override
    public Map<String, Object> getLogsByGroupId(Integer groupId, Integer pageNo, Integer pageSize) {
        try{
            Map<String, Object> returnMap = new HashMap<>();
            Page<OperationLog> page = PageHelper.startPage(pageNo, pageSize);
            operationLogDao.selectLogsByOperationGroupId(groupId);
            returnMap.put("logData", page);
            returnMap.put("totalCount", page.getTotal());
            returnMap.put("totalPage",page.getPages());
            return returnMap;
        }catch (Exception e){
            logger.error("query logs from db error",e);
            return null;
        }
    }

    @Override
    public int insertLog(OperationLog operationLog) {
        return operationLogDao.insertLog(operationLog);
    }
}
