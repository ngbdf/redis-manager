package com.newegg.ec.redis.client;

/**
 * @author Jay.H.Zou
 * @date 2019/7/18
 */
public interface IRedisClusterClient extends IRedisClient {

    String getRedisClusterClient(ConnectParam connectParam);

    String getClusterInfo(ConnectParam connectParam);

    String getNodeList(ConnectParam connectParam);

    String getMasterList(ConnectParam connectParam);

    String getSlaveList(ConnectParam connectParam);

}
