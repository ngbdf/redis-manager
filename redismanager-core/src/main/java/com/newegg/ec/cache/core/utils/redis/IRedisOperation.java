package com.newegg.ec.cache.core.utils.redis;

import com.newegg.ec.cache.core.entity.redis.RedisConnectParam;
import redis.clients.jedis.util.Slowlog;

import java.util.List;
import java.util.Map;

/**
 * Created by lf52 on 2019/2/23.
 */
public interface IRedisOperation {

    /**
     * redis info command
     * @param param
     * @return
     */
    public Map<String, String> getInfo(RedisConnectParam param);

    /**
     * get all alive node
     * @param param
     * @return
     */
    public List<String> getAllNodes(RedisConnectParam param);

    /**
     * get redis db info (version < 3.0.0)
     * @param param
     * @return
     */
    public List<Map<String, String>> redisDBInfo(RedisConnectParam param);

    /**
     * get redis service config
     * @param param
     * @return
     */
    public Map<String, String> getRedisConfig(RedisConnectParam param);

    /**
     * get redis version
     * @param param
     * @return
     */
    public int getRedisVersion(RedisConnectParam param);

    /**
     * getDbIndex (version < 3.0.0)
     * @param db
     * @return
     */
    public int getDbIndex(String db);

    /**
     * redis dbsize (version < 3.0.0)
     * @param db
     * @return
     */
    public int dbSize(String db);

    /**
     * redis cluster info (version > 3.0.0)
     * @param param
     * @return
     */
    public Map<String, String> getClusterInfo(RedisConnectParam param);

    /**
     * getMasterNodes (version > 3.0.0)
     * @param param
     * @return
     */
    public Map<String, Map> getMasterNodes(RedisConnectParam param);

    /**
     * getNodeId
     * @param param
     * @return
     */
    public String getNodeId(RedisConnectParam param);

    /**
     * get port
     * @param port
     * @return
     */
    public int getPort(String port);

    /**
     * get slow logs with size
     * @param param
     * @param size
     * @return
     */
    public List<Slowlog> getSlowLog(RedisConnectParam param, long size);

}
