package com.newegg.ec.redis.service;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.entity.RDBAnalyze;
import redis.clients.jedis.Jedis;

import java.net.UnknownHostException;
import java.util.List;

public interface IRdbAnalyzeService {
    public JSONObject allocationRDBAnalyzeJob(Long id);

    public JSONObject allocationRDBAnalyzeJob(RDBAnalyze rdbAnalyze);

    public JSONObject canceRDBAnalyze(String instance,String scheduleID) throws UnknownHostException;

    public RDBAnalyze selectById(Long id);

    public boolean update(RDBAnalyze rdbAnalyze);

    public boolean add(RDBAnalyze rdbAnalyze);

    public List<RDBAnalyze> list(Long groupId);

    public List<RDBAnalyze> selectALL();

    public boolean checkResult(Integer result);

    public RDBAnalyze getRDBAnalyzeByPid(Long cluster_id);

    public RDBAnalyze getRDBAnalyzeById(Long cluster_id);

    public boolean ifRDBAnalyzeIsRunning(Long id);

    public Long getRedisIDBasePID(Long cluster_id);

    public boolean updateRdbAnalyze(RDBAnalyze rdbAnalyze);

    public void updateJob(RDBAnalyze rdbAnalyze);

    public void rdbBgsave(Jedis jedis, String host, String port);

    public Long selectClusterIdById(Long id);

    void createRdbAnalyzeTable();

    boolean deleteRdbAnalyze(Long id);

    boolean exitsRdbAnalyze(RDBAnalyze rdbAnalyze);
}
