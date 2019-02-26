package com.newegg.ec.cache.module.clustermonitor;

import com.newegg.ec.cache.core.entity.model.NodeInfo;
import com.newegg.ec.cache.core.entity.redis.RedisSlowLog;
import com.newegg.ec.cache.core.entity.redis.SlowLogParam;

import java.util.List;
import java.util.Map;

/**
 * Created by lf52 on 2019/2/26.
 */
public interface IMonitorService{

    public List<NodeInfo> getGroupNodeInfo(int clusterId, int startTime, int endTime, String host, String type, String date);

    public List<Map> getMaxField(int clusterId, int startTime, int endTime, String key, int limit);

    public List<Map> getMinField(int clusterId, int startTime, int endTime, String key, int limit);

    public String getAvgField(int clusterId, int startTime, int endTime, String host, String key);

    public String getAllField(int clusterId, int startTime, int endTime, String key);

    public NodeInfo getLastNodeInfo(int clusterId, int startTime, int endTime, String host);

    public List<RedisSlowLog> getSlowLogs(SlowLogParam slowLogParam);

    public int getDbSize(int clusterId, String address);

}
