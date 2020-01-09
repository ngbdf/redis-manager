package com.newegg.ec.redis.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.dao.IRdbAnalyze;
import com.newegg.ec.redis.entity.RDBAnalyze;
import com.newegg.ec.redis.service.IRdbAnalyzeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * @author Kyle.K.Zhao
 * @date 1/8/2020 17:57
 */
@Service
public class RdbAnalyzeService implements IRdbAnalyzeService {

    @Autowired
    IRdbAnalyze iRdbAnalyze;

    @Override
    public JSONObject allocationRDBAnalyzeJob(Long id, int[] analyzer) {
        return null;
    }

    @Override
    public JSONObject canceRDBAnalyze(String instance) {
        return null;
    }

    @Override
    public boolean ifRDBAnalyzeIsRunning(Long id) {
        return false;
    }

    @Override
    public void rdbBgsave(Jedis jedis, String host, String port) {

    }

    @Override
    public boolean update(RDBAnalyze rdbAnalyze) {
        return false;
    }

    @Override
    public RDBAnalyze selectById(Long id) {
        return null;
    }

    @Override
    public boolean add(RDBAnalyze rdbAnalyze) {
        return false;
    }

    @Override
    public List<RDBAnalyze> list() {
        return null;
    }

    @Override
    public RDBAnalyze getRDBAnalyzeByPid(Long pid) {
        return null;
    }

    @Override
    public RDBAnalyze getRDBAnalyzeById(Long pid) {
        return null;
    }

    @Override
    public Long getRedisIDBasePID(Long pid) {
        return null;
    }

    @Override
    public boolean updateRdbAnalyze(RDBAnalyze rdbAnalyze) {
        return false;
    }

    @Override
    public Long selectPidById(Long id) {
        return null;
    }

    @Override
    public void createRdbAnalyzeTable() {
        iRdbAnalyze.createRdbAnalyzeTable();
    }
}
