package com.newegg.ec.redis.service;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.entity.RDBAnalyze;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import redis.clients.jedis.Jedis;

import java.util.List;

public interface IRdbAnalyzeService {
    public JSONObject allocationRDBAnalyzeJob(Long id, int[] analyzer);

    public JSONObject canceRDBAnalyze(String instance);

    public boolean update(RDBAnalyze rdbAnalyze);

    public RDBAnalyze selectById(Long id);

    public boolean add(RDBAnalyze rdbAnalyze);

    public List<RDBAnalyze> list();

    public boolean checkResult(Integer result);

    public RDBAnalyze getRDBAnalyzeByPid(Long pid);

    public RDBAnalyze getRDBAnalyzeById(Long pid);

    public boolean ifRDBAnalyzeIsRunning(Long id);

    public Long getRedisIDBasePID(Long pid);

    public boolean updateRdbAnalyze(RDBAnalyze rdbAnalyze);

    public void updateJob(RDBAnalyze rdbAnalyze);

    public void rdbBgsave(Jedis jedis, String host, String port);

    public Long selectClusterIdById(Long id);

    void createRdbAnalyzeTable();
}
