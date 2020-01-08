package com.newegg.ec.redis.service;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.entity.RDBAnalyze;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * @author Kyle.K.Zhao
 * @date 1/8/2020 17:10
 */
public interface IRdbAnalyzeService {

    JSONObject allocationRDBAnalyzeJob(Long id, int[] analyzer);

    JSONObject canceRDBAnalyze(String instance);

    boolean ifRDBAnalyzeIsRunning(Long id);

    void rdbBgsave(Jedis jedis, String host, String port);

    boolean update(RDBAnalyze rdbAnalyze);

    RDBAnalyze selectById(Long id);

    boolean add(RDBAnalyze rdbAnalyze);

    List<RDBAnalyze> list();

    RDBAnalyze getRDBAnalyzeByPid(Long pid);

    RDBAnalyze getRDBAnalyzeById(Long pid);

    Long getRedisIDBasePID(Long pid);

    boolean updateRdbAnalyze(RDBAnalyze rdbAnalyze);

    Long selectPidById(Long id);

    void createRdbAnalyzeTable();


}
