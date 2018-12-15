package com.newegg.ec.cache.app.logic;

import com.newegg.ec.cache.app.component.redis.RedisClient;
import com.newegg.ec.cache.app.model.ConnectionParam;
import com.newegg.ec.cache.app.model.MemoryDoctorConfig;
import com.newegg.ec.cache.app.util.JedisUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by lf52 on 2018/12/15.
 * 对于Redis4.0新特性的功能扩展 ： MEMORY DOCTOR,给出redis 集群的诊断建议
 */
@Component
public class ExtensionLogic implements ApplicationListener<ContextRefreshedEvent> {

    private static Log logger = LogFactory.getLog(ExtensionLogic.class);

    @Autowired
    private MemoryDoctorConfig config;

    private Map<String, String> map;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        map  =  config.getConfigMap();
    }

    /**
     * 内存诊断功能：
     * @param password
     * @param ip
     * @param port
     * @return
     */
    public String memoryDoctor(String password, String ip, int port){

        String result = "";

        ConnectionParam param = new ConnectionParam(ip, port, password);
        Map<String, String> nodeInfo = JedisUtil.getMapInfo(param);
        String redisVersion = nodeInfo.get("redis_version");
        if(Integer.valueOf(redisVersion.substring(0, 1)) >= 4){
            RedisClient redisClient = new RedisClient(ip, port);
            try {
                if (StringUtils.isNotBlank(password)) {
                    result = redisClient.redisCommandOpt(password, RedisClient.MEMORYDOCTOR);
                } else {
                    result = redisClient.redisCommandOpt(RedisClient.MEMORYDOCTOR);
                }
            } catch (Exception e) {
                result = "memory doctor error";
                logger.error("memory doctor error",e);
            } finally {
                redisClient.closeClient();
            }

        }else{
            result = "this redis cannot supprot this function";
        }

        return ip + ":" + port + " : " +SwitchResult(result);
    }


    private String SwitchResult(String commandResult) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (commandResult.contains(entry.getKey().replaceAll("_", " "))) {
               return entry.getValue();
            }
        }
        return "获取不到有效的诊断信息";
    }



}
