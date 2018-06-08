package com.newegg.ec.cache.app.logic;

import com.newegg.ec.cache.app.dao.INodeInfoDao;
import com.newegg.ec.cache.app.dao.impl.NodeInfoDao;
import com.newegg.ec.cache.app.model.*;
import com.newegg.ec.cache.app.util.DateUtil;
import com.newegg.ec.cache.app.util.JedisUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.util.Slowlog;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by gl49 on 2018/4/21.
 */
@Component
public class MonitorLogic {
    @Resource
    private INodeInfoDao nodeDao;

    public List<NodeInfo> getGroupNodeInfo(int clusterId,int startTime,int endTime, String host, String type, String date){
        return nodeDao.getGroupNodeInfo(Common.NODE_INFO_TABLE_FORMAT + clusterId, startTime, endTime, host, type, date);
    }

    public List<Map> getMaxField(int clusterId, int startTime, int endTime, String key, int limit){
        return nodeDao.getMaxField(Common.NODE_INFO_TABLE_FORMAT + clusterId, startTime, endTime, key, limit);
    }

    public List<Map> getMinField(int clusterId, int startTime, int endTime, String key, int limit){
        return nodeDao.getMinField(Common.NODE_INFO_TABLE_FORMAT + clusterId, startTime, endTime, key, limit);
    }

    public String getAvgField(int clusterId, int startTime, int endTime, String host, String key){
        return nodeDao.getAvgField(Common.NODE_INFO_TABLE_FORMAT + clusterId, startTime, endTime, host, key);
    }

    public String getAllField(int clusterId, int startTime, int endTime, String key){
        return nodeDao.getAllField(Common.NODE_INFO_TABLE_FORMAT + clusterId, startTime, endTime, key);
    }

    public NodeInfo getLastNodeInfo(int clusterId, int startTime, int endTime, String host){
        return nodeDao.getLastNodeInfo(Common.NODE_INFO_TABLE_FORMAT + clusterId, startTime, endTime, host);
    }

    public List<RedisSlowLog> getSlowLogs(SlowLogParam slowLogParam) {
        List<RedisSlowLog>  cmdList = new ArrayList<>();
        List<Host> ipList = slowLogParam.getHostList();
        int logLimit = 0;
        if( ipList.size() > 800 ){
            logLimit = 1;
        }else if( slowLogParam.getLogLimit() == 0 ){
            logLimit = 800/ipList.size();
        }
        for(Host host1 : ipList){
            try {
                List<Slowlog> slowList = JedisUtil.getSlowLog(host1.getIp(), host1.getPort(), logLimit);
                for( Slowlog log : slowList){
                    String slowDate =DateUtil.getFormatDate( log.getTimeStamp() * 1000 );
                    long logTime = log.getExecutionTime();
                    int runTime = (int) (logTime / 1000);
                    List<String> args = log.getArgs();
                    String type = args.get(0);
                    List<String> commands = args.subList(1, args.size());
                    String command = StringUtils.join(commands, " ");
                    RedisSlowLog redisSlowLog = new RedisSlowLog();
                    redisSlowLog.setCommand( command );
                    redisSlowLog.setHost( host1.getIp() + ":" + host1.getPort() );
                    redisSlowLog.setRunTime( runTime );
                    redisSlowLog.setType( type );
                    redisSlowLog.setShowDate( slowDate );
                    redisSlowLog.setAddTime( logTime );
                    cmdList.add( redisSlowLog );
                }
            }catch (Exception e){

            }
        }
        return cmdList;
    }
}
