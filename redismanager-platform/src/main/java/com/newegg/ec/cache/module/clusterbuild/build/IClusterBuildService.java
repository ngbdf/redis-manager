package com.newegg.ec.cache.module.clusterbuild.build;

import com.newegg.ec.cache.core.entity.model.Cluster;
import com.newegg.ec.cache.core.entity.model.ClusterImportResult;
import com.newegg.ec.cache.core.entity.model.Host;
import com.newegg.ec.cache.core.entity.redis.RedisQueryParam;
import com.newegg.ec.cache.core.entity.redis.RedisValue;

import java.util.List;
import java.util.Map;

/**
 * Created by lf52 on 2019/2/26.
 */
public interface IClusterBuildService {

    public RedisValue query(RedisQueryParam redisQueryParam);
    public Map<String, String> getClusterInfo(int clusterId, String ip, int port);
    public Map<String, String> getClusterInfo(int clusterId, String address);
    public Map<String, String> getNodeInfo(int clusterId, String address);
    public Map<String, String> getRedisConfig(int clusterId, String address);
    public List<Map<String, String>> nodeList(int clusterId, String address);
    public Host getClusterHost(int id);
    public boolean getClusterState(int clusterId);
    public Map<String, Map> detailNodeList(int clusterId, String address);
    public List<Map<String, String>> getRedisDBList(int clusterId, String address);
    public List<Cluster> getClusterListByGroup(String group);
    public boolean beSlave(int clusterId, String ip, int port, String masterId);
    public boolean beMaster(int clusterId, String ip, int port);
    public boolean forgetNode(int clusterId, String ip, int port, String nodeId);
    public boolean importNode(int clusterId, String ip, int port, String masterIP, int masterPort);
    public boolean batchConfig(int clusterId, String myIp, int myPort, String configName, String configValue);
    public void addRedisPassd(String ip, int port, String password);
    public boolean initSlot(int clusterId, String address);
    public boolean reShard(int clusterId, String ip, int port, int startKey, int endKey);
    public boolean importDataToCluster(int clusterId, String address, String targetAddress, String keyFormat);
    public List<ClusterImportResult> getImportCountList();
}
