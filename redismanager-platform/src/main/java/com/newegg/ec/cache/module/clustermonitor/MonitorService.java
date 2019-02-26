package com.newegg.ec.cache.module.clustermonitor;

import com.newegg.ec.cache.core.entity.constants.Constants;
import com.newegg.ec.cache.core.entity.model.Cluster;
import com.newegg.ec.cache.core.entity.model.Host;
import com.newegg.ec.cache.core.entity.model.NodeInfo;
import com.newegg.ec.cache.core.entity.model.Slowlog;
import com.newegg.ec.cache.core.entity.redis.RedisConnectParam;
import com.newegg.ec.cache.core.entity.redis.RedisSlowLog;
import com.newegg.ec.cache.core.entity.redis.SlowLogParam;
import com.newegg.ec.cache.dao.IClusterDao;
import com.newegg.ec.cache.dao.INodeInfoDao;
import com.newegg.ec.cache.module.extend.MonitorExtensionService;
import com.newegg.ec.cache.util.DateUtil;
import com.newegg.ec.cache.util.NetUtil;
import com.newegg.ec.cache.util.redis.RedisUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by gl49 on 2018/4/21.
 */
@Component
public class MonitorService extends MonitorExtensionService implements IMonitorService{

    @Autowired
    private IClusterDao clusterDao;

    @Resource
    private INodeInfoDao nodeDao;

    @Override
    public List<NodeInfo> getGroupNodeInfo(int clusterId, int startTime, int endTime, String host, String type, String date) {
        return nodeDao.getGroupNodeInfo(Constants.NODE_INFO_TABLE_FORMAT + clusterId, startTime, endTime, host, type, date);
    }

    @Override
    public List<Map> getMaxField(int clusterId, int startTime, int endTime, String key, int limit) {
        return nodeDao.getMaxField(Constants.NODE_INFO_TABLE_FORMAT + clusterId, startTime, endTime, key, limit);
    }

    @Override
    public List<Map> getMinField(int clusterId, int startTime, int endTime, String key, int limit) {
        return nodeDao.getMinField(Constants.NODE_INFO_TABLE_FORMAT + clusterId, startTime, endTime, key, limit);
    }

    @Override
    public String getAvgField(int clusterId, int startTime, int endTime, String host, String key) {
        return nodeDao.getAvgField(Constants.NODE_INFO_TABLE_FORMAT + clusterId, startTime, endTime, host, key);
    }

    @Override
    public String getAllField(int clusterId, int startTime, int endTime, String key) {
        return nodeDao.getAllField(Constants.NODE_INFO_TABLE_FORMAT + clusterId, startTime, endTime, key);
    }

    @Override
    public NodeInfo getLastNodeInfo(int clusterId, int startTime, int endTime, String host) {
        return nodeDao.getLastNodeInfo(Constants.NODE_INFO_TABLE_FORMAT + clusterId, startTime, endTime, host);
    }

    @Override
    public List<RedisSlowLog> getSlowLogs(SlowLogParam slowLogParam) {
        List<RedisSlowLog> cmdList = new ArrayList<>(slowLogParam.getLogLimit());
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
                List<Slowlog> slowList =  RedisUtils.getSlowLog(new RedisConnectParam(host1.getIp(), host1.getPort(), cluster.getRedisPassword()), logLimit);
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

    @Override
    public int getDbSize(int clusterId, String address) {
        Host host = NetUtil.getHostPassAddress(address);
        Cluster cluster = clusterDao.getCluster(clusterId);
        RedisConnectParam param = new RedisConnectParam(host.getIp(), host.getPort(), cluster.getRedisPassword());
        int size = RedisUtils.dbSize(param);
        return size;
    }

}
