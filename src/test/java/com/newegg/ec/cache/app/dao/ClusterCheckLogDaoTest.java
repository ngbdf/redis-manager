package com.newegg.ec.cache.app.dao;

import com.newegg.ec.cache.Application;
import com.newegg.ec.cache.app.model.ClusterCheckLog;
import com.newegg.ec.cache.app.util.CommonUtil;
import com.newegg.ec.cache.app.util.DateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gl49 on 2018/4/24.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class ClusterCheckLogDaoTest {

    @Resource
    private IClusterCheckLogDao logDao;

    @Test
    public void addTest(){
        ClusterCheckLog log = new ClusterCheckLog();
        log.setId(CommonUtil.getUuid());
        log.setClusterId("2");
        log.setNodeId("localhost:8018");
        log.setFormula("@{mem_fragmentation_ratio}>2.2");
        log.setLogInfo("mem_fragmentation_ratio = 5.0 !!!");
        log.setDescription("Redis节点磁盘碎片率");
        log.setLogType(ClusterCheckLog.LogType.warnlog);
        log.setUpdateTime(DateUtil.getTime());
        System.out.println(logDao.addClusterCheckLog(log));
    }

    @Test
    public void getLogsTest(){
        Map<String,Object> param = new HashMap();
        param.put("clusterId", "1");
        param.put("nodeId", "10.16.46.192:8018");
        param.put("formula", "@{mem_fragmentation_ratio}>=1.0");
        param.put("logType", ClusterCheckLog.LogType.warnlog.toString());
        //param.put("limit",1);
        System.out.println(logDao.getClusterCheckLogs(param).size());
    }

    @Test
    public void delLogsTest(){
        Map<String,Object> param = new HashMap();
        param.put("clusterId","ssecbigdata");
        param.put("nodeId","localhost:8008");
        param.put("logType",ClusterCheckLog.LogType.warnlog.toString());
        logDao.delLogs(param);
    }

    @Test
    public void checkWarningLogTest(){
        Map<String,Object> param = new HashMap();
        param.put("clusterId","ssecbigdata");
        System.out.println(logDao.checkWarningLogs(param));
    }

    @Test
    public void countWarningLogNumTest(){
        //System.out.println(logDao.countTotalWarningLog(null) + "---------------------------");
        System.out.println(logDao.countWarningLogByClusterId(1));
    }


}
