package com.newegg.ec.cache.app.logic;

import com.newegg.ec.cache.app.component.RedisManager;
import com.newegg.ec.cache.app.component.redis.RedisClient;
import com.newegg.ec.cache.app.dao.IClusterDao;
import com.newegg.ec.cache.app.model.Cluster;
import com.newegg.ec.cache.app.model.ConnectionParam;
import com.newegg.ec.cache.app.model.Host;
import com.newegg.ec.cache.app.model.MemoryDoctorConfig;
import com.newegg.ec.cache.app.util.NetUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by lf52 on 2018/12/15.
 * 对于Redis4.0新特性的功能扩展 ： MEMORY DOCTOR,给出redis 集群的诊断建议
 */
@Component
public class ExtensionLogic implements ApplicationListener<ContextRefreshedEvent> {

    private static final Log logger = LogFactory.getLog(ExtensionLogic.class);

    private static ExecutorService executorPool = Executors.newFixedThreadPool(30);

    @Autowired
    private IClusterDao clusterDao;

    @Resource
    private RedisManager redisManager;

    @Autowired
    private MemoryDoctorConfig config;

    private static Map<String, String> map;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        map  =  config.getConfigMap();
    }

    /**
     * Memory Doctor功能
     * @param clusterId
     * @return
     */
    public List<Map<String,String>> memoryDoctor(int clusterId){

        List<Map<String,String>> result = new LinkedList<>();

        Cluster cluster = clusterDao.getCluster(clusterId);
        String password = cluster.getRedisPassword();
        Host host = NetUtil.getHostPassAddress(cluster.getAddress());
        ConnectionParam param = new ConnectionParam(host.getIp(), host.getPort(),password);
        List<Map<String, String>> nodeList = redisManager.nodeList(param);;

        List<Future<String>> futureList = new ArrayList<>(nodeList.size());
        nodeList.forEach(node -> {
            futureList.add(executorPool.submit(new MemoryDiagnosis(password, node.get("ip"), Integer.parseInt(node.get("port")))));
        });
        futureList.forEach(future -> {
            try {
                String str = future.get();
                Map<String,String> map = new HashMap<>();
                map.put("addr",str.split("~")[0] + " : ");
                map.put("result",str.split("~")[1]);
                result.add(map);
            } catch (Exception e) {
                //ingore
            }
        });
        return result;
    }

    /**
     * 内存诊断功能
     */
    private class MemoryDiagnosis implements Callable<String>{

        private String password;
        private String ip;
        private int port;

        public MemoryDiagnosis(String password,String ip,int port){
            this.ip = ip;
            this.password = password;
            this.port = port;
        }

        @Override
        public String call() throws Exception {
            String result = "";
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

            return ip + ":" + port + "~" +SwitchResult(result);
        }

    }


    private String SwitchResult(String commandResult) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (commandResult.contains(entry.getKey().replaceAll("_", " "))) {
                builder.append(entry.getValue() + ";");
            }
        }
        if(builder.length() > 0){
            return builder.toString();
        }
        return "Can not get effective diagnosis result" ;
    }


}
