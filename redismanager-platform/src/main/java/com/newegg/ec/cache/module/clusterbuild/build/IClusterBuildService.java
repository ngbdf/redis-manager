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

    /**
     * query redis value
     * @param redisQueryParam
     * @return
     */
    public RedisValue query(RedisQueryParam redisQueryParam);

    /**
     * get Cluster Info
     * @param clusterId
     * @param ip
     * @param port
     * @return
     */
    public Map<String, String> getClusterInfo(int clusterId, String ip, int port);

    /**
     * get Cluster Info
     * @param clusterId
     * @param address
     * @return
     */
    public Map<String, String> getClusterInfo(int clusterId, String address);

    /**
     * get Node Info
     * @param clusterId
     * @param address
     * @return
     */
    public Map<String, String> getNodeInfo(int clusterId, String address);

    /**
     * get Redis Server Config
     * @param clusterId
     * @param address
     * @return
     */
    public Map<String, String> getRedisConfig(int clusterId, String address);

    /**
     * get nodeList
     * @param clusterId
     * @param address
     * @return
     */
    public List<Map<String, String>> nodeList(int clusterId, String address);

    /**
     * get Cluster Host
     * @param id
     * @return
     */
    public Host getClusterHost(int id);

    /**
     * get Cluster State
     * @param clusterId
     * @return
     */
    public boolean getClusterState(int clusterId);

    /***
     * NodeList detail info
     * @param clusterId
     * @param address
     * @return
     */
    public Map<String, Map> detailNodeList(int clusterId, String address);

    /**
     * get RedisDB List
     * @param clusterId
     * @param address
     * @return
     */
    public List<Map<String, String>> getRedisDBList(int clusterId, String address);

    /**
     * get ClusterList By Group
     * @param group
     * @return
     */
    public List<Cluster> getClusterListByGroup(String group);

    /**
     * beSlave
     * @param clusterId
     * @param ip
     * @param port
     * @param masterId
     * @return
     */
    public boolean beSlave(int clusterId, String ip, int port, String masterId);

    /**
     *
     * @param clusterId
     * @param ip
     * @param port
     * @return
     */
    public boolean beMaster(int clusterId, String ip, int port);

    /**
     * forget Node
     * @param clusterId
     * @param ip
     * @param port
     * @param nodeId
     * @return
     */
    public boolean forgetNode(int clusterId, String ip, int port, String nodeId);

    /**
     * import Node To Cluster
     * @param clusterId
     * @param ip
     * @param port
     * @param masterIP
     * @param masterPort
     * @return
     */
    public boolean importNode(int clusterId, String ip, int port, String masterIP, int masterPort);

    /**
     * batch Config set
     * @param clusterId
     * @param myIp
     * @param myPort
     * @param configName
     * @param configValue
     * @return
     */
    public boolean batchConfig(int clusterId, String myIp, int myPort, String configName, String configValue);

    /**
     * addRedisPassd
     * @param ip
     * @param port
     * @param password
     */
    public void addRedisPassd(String ip, int port, String password);

    /**
     * initSlot
     * @param clusterId
     * @param address
     * @return
     */
    public boolean initSlot(int clusterId, String address);

    /**
     * reShard
     * @param clusterId
     * @param ip
     * @param port
     * @param startKey
     * @param endKey
     * @return
     */
    public boolean reShard(int clusterId, String ip, int port, int startKey, int endKey);

    /**
     * import Data To Cluster
     * @param clusterId
     * @param address
     * @param targetAddress
     * @param keyFormat
     * @return
     */
    public boolean importDataToCluster(int clusterId, String address, String targetAddress, String keyFormat);

    /**
     * get importCount List
     * @return
     */
    public List<ClusterImportResult> getImportCountList();
}
