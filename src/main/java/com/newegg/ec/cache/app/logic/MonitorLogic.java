package com.newegg.ec.cache.app.logic;

import com.newegg.ec.cache.app.component.RedisManager;
import com.newegg.ec.cache.app.dao.IClusterDao;
import com.newegg.ec.cache.app.dao.INodeInfoDao;
import com.newegg.ec.cache.app.model.*;
import com.newegg.ec.cache.app.util.DateUtil;
import com.newegg.ec.cache.app.util.JedisUtil;
import com.newegg.ec.cache.app.util.NetUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.util.Slowlog;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by gl49 on 2018/4/21.
 */
@Component
public class MonitorLogic {

    @Resource
    private RedisManager redisManager;

    @Autowired
    private IClusterDao clusterDao;

    @Resource
    private INodeInfoDao nodeDao;

    public List<NodeInfo> getGroupNodeInfo(int clusterId, int startTime, int endTime, String host, String type, String date) {
        return nodeDao.getGroupNodeInfo(Constants.NODE_INFO_TABLE_FORMAT + clusterId, startTime, endTime, host, type, date);
    }

    public List<Map> getMaxField(int clusterId, int startTime, int endTime, String key, int limit) {
        return nodeDao.getMaxField(Constants.NODE_INFO_TABLE_FORMAT + clusterId, startTime, endTime, key, limit);
    }

    public List<Map> getMinField(int clusterId, int startTime, int endTime, String key, int limit) {
        return nodeDao.getMinField(Constants.NODE_INFO_TABLE_FORMAT + clusterId, startTime, endTime, key, limit);
    }

    public String getAvgField(int clusterId, int startTime, int endTime, String host, String key) {
        return nodeDao.getAvgField(Constants.NODE_INFO_TABLE_FORMAT + clusterId, startTime, endTime, host, key);
    }

    public String getAllField(int clusterId, int startTime, int endTime, String key) {
        return nodeDao.getAllField(Constants.NODE_INFO_TABLE_FORMAT + clusterId, startTime, endTime, key);
    }

    public NodeInfo getLastNodeInfo(int clusterId, int startTime, int endTime, String host) {
        return nodeDao.getLastNodeInfo(Constants.NODE_INFO_TABLE_FORMAT + clusterId, startTime, endTime, host);
    }

    public List<RedisSlowLog> getSlowLogs(SlowLogParam slowLogParam) {
        List<RedisSlowLog> cmdList = new ArrayList<>();
        List<Host> ipList = slowLogParam.getHostList();
        int logLimit = 0;
        if (ipList.size() > 800) {
            logLimit = 1;
        } else if (slowLogParam.getLogLimit() == 0) {
            logLimit = 800 / ipList.size();
        }
        Cluster cluster = clusterDao.getCluster(slowLogParam.getClusterId());
        for (Host host1 : ipList) {
            try {
                List<Slowlog> slowList = JedisUtil.getSlowLog(new ConnectionParam(host1.getIp(), host1.getPort(), cluster.getRedisPassword()), logLimit);
                for (Slowlog log : slowList) {
                    String slowDate = DateUtil.getFormatDate(log.getTimeStamp() * 1000);
                    long logTime = log.getExecutionTime();
                    int runTime = (int) (logTime / 1000);
                    List<String> args = log.getArgs();
                    String type = args.get(0);
                    List<String> commands = args.subList(1, args.size());
                    String command = StringUtils.join(commands, " ");
                    RedisSlowLog redisSlowLog = new RedisSlowLog();
                    redisSlowLog.setCommand(command);
                    redisSlowLog.setHost(host1.getIp() + ":" + host1.getPort());
                    redisSlowLog.setRunTime(runTime);
                    redisSlowLog.setType(type);
                    redisSlowLog.setShowDate(slowDate);
                    redisSlowLog.setAddTime(logTime);
                    cmdList.add(redisSlowLog);
                }
            } catch (Exception e) {

            }
        }
        return cmdList;
    }

    public int getDbSize(int clusterId, String address) {
        Host host = NetUtil.getHostPassAddress(address);
        Cluster cluster = clusterDao.getCluster(clusterId);
        ConnectionParam param = new ConnectionParam(host.getIp(), host.getPort(), cluster.getRedisPassword());
        int size = redisManager.getDbSize(param);
        return size;
    }
}
