package com.newegg.ec.redis.aop.aspect;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.newegg.ec.redis.entity.Cluster;
import com.newegg.ec.redis.entity.OperationLog;
import com.newegg.ec.redis.entity.Result;
import com.newegg.ec.redis.entity.User;
import com.newegg.ec.redis.plugin.install.entity.InstallationParam;
import com.newegg.ec.redis.service.IOperationLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author fw13
 * @date 2019/11/20 13:08
 */
@Aspect
@Component
public class OperationLogAspect implements ApplicationListener<ContextRefreshedEvent> {

    private BlockingQueue<OperationLog> operationLogs = new LinkedBlockingDeque<>();

    private Logger logger = LoggerFactory.getLogger(OperationLogAspect.class);

    @Autowired
    private IOperationLogService operationLogService;

    @Pointcut("@annotation(com.newegg.ec.redis.aop.annotation.OperationLog)")
    public void controllerPointcut() {

    }

    @AfterReturning(pointcut = "controllerPointcut()  && @annotation(operationLog)", returning = "returnResult")
    public void afterReturning(JoinPoint joinPoint, com.newegg.ec.redis.aop.annotation.OperationLog operationLog, Object returnResult) {
        try {
            if (!(returnResult instanceof Result)) {
                return;
            }

            //filter failed request
            Result result = (Result) returnResult;
            if (result.getCode() != Result.ResultCode.SUCCESS.getCode()) {
                return;
            }

            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

            Object[] args = joinPoint.getArgs();

            String requestParams = JSONObject.toJSONString(args);

            Integer operationGroupId = getOperationGroupId(args);

            String userIp = request.getHeader("UserIp");
            User user = (User) request.getSession().getAttribute("user");

            Integer userId = -1;
            Integer groupId = -1;

            if (user != null) {
                userId = user.getUserId();
                groupId = user.getGroupId();
            }

            String type = operationLog.type().getType();
            String objType = operationLog.objType().getObjType();

            OperationLog log = new OperationLog(groupId, userId, userIp,operationGroupId, type + " " + objType, requestParams, new Timestamp(System.currentTimeMillis()));

            operationLogs.add(log);

        } catch (Exception e) {
            logger.error("operation log aspect error", e);
        }
    }

    /**
     * consume queue 2 save db
     */
    private void saveLogs() {
        while (true) {
            try {
                OperationLog log = operationLogs.poll(5, TimeUnit.MILLISECONDS);
                if (log != null) {
                    operationLogService.insertLog(log);
                }
            } catch (Exception e) {
                logger.error("insert operation log to db error", e);
            }
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ExecutorService pool = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setNameFormat("Save-Logs-%d").setDaemon(true).build());
        pool.submit(this::saveLogs);
    }

    /**
     * 这里可能不是特别合理，只为实现功能
     */
    private Integer getOperationGroupId(Object[] args){
        Object arg = args[0];
        Integer operationGroupId = -1;
        if (arg instanceof JSONObject){
            operationGroupId = (Integer)((JSONObject)arg).get("groupId");
        }else{
            if (arg instanceof List) {
                JSONArray argArray = (JSONArray)JSONObject.toJSON(arg);
                JSONObject jsonObject = (JSONObject)argArray.get(0);
                operationGroupId = (Integer)jsonObject.get("groupId");
            }else {
                if (arg instanceof InstallationParam){
                    InstallationParam installationParam = (InstallationParam)arg;
                    Cluster cluster = installationParam.getCluster();
                    operationGroupId = cluster.getGroupId();
                }else{
                    JSONObject argJson = (JSONObject)JSONObject.toJSON(arg);
                    operationGroupId = (Integer)argJson.get("groupId");
                }
            }
        }

        return operationGroupId;
    }
}
