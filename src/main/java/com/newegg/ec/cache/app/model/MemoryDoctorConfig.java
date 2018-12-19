package com.newegg.ec.cache.app.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lf52 on 2018/12/15.
 *
 * 将redis4.0支持的命令MEMORY DOCTOR 返回的英文结果映射成中文结果。
 * 通过翻看github上redis源码，当前MEMORY DOCTOR的返回结果共有以下10种
 * 具体详情见：https://github.com/antirez/redis/blob/unstable/src/object.c
 * line 1080 getMemoryDoctorReport方法
 */
@Component
@PropertySource("classpath:config/memorydoctor.properties")
public class MemoryDoctorConfig implements ApplicationListener<ContextRefreshedEvent> {

    private Map<String,String> configMap = new HashMap<>();

    @Value("${memory_issue}")
    private String memory_issue;
    @Value("${little_memory}")
    private String little_memory;
    @Value("${Peak_memory}")
    private String Peak_memory;
    @Value("${High_fragmentation}")
    private String High_fragmentation;
    @Value("${High_allocator_fragmentation}")
    private String High_allocator_fragmentation;
    @Value("${High_allocator_RSS_overhead}")
    private String High_allocator_RSS_overhead;
    @Value("${High_process_RSS_overhead}")
    private String High_process_RSS_overhead;
    @Value("${Big_replica_buffers}")
    private String Big_replica_buffers;
    @Value("${Big_client_buffer}")
    private String Big_client_buffer;
    @Value("${Many_scripts}")
    private String Many_scripts;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
         initConfigMap();
    }

    private void initConfigMap() {
        configMap.put(Constants.MEMORY_ISSUE,memory_issue);
        configMap.put(Constants.LITTLE_MEMORY,little_memory);
        configMap.put(Constants.PEAK_MEMORY,Peak_memory);
        configMap.put(Constants.High_FRAGMENTATION,High_fragmentation);
        configMap.put(Constants.HIGH_ALLOCATOR_FRAGMENTATION,High_allocator_fragmentation);
        configMap.put(Constants.HIGH_ALLOCATOR_RSS_OVERHEAD,High_allocator_RSS_overhead);
        configMap.put(Constants.HIGH_PROCESS_RSS_OVERHEAD,High_process_RSS_overhead);
        configMap.put(Constants.BIG_REPLICA_BUFFERS,Big_replica_buffers);
        configMap.put(Constants.BIG_CLIENT_BUFFER,Big_client_buffer);
        configMap.put(Constants.MANY_SCRIPTS,Many_scripts);
    }

    public Map<String,String> getConfigMap(){
        return configMap;
    }

}
