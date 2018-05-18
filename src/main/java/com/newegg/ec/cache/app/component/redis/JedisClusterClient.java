package com.newegg.ec.cache.app.component.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;
/**
 * Created by lzz on 2018/3/19.
 */
public class JedisClusterClient extends JedisSingleClient implements IRedis {
    private JedisCluster jedis;
    public JedisClusterClient(String ip, int port){
        super(ip, port);
        HostAndPort hostAndPort = new HostAndPort(ip, port);
        jedis = new JedisCluster( hostAndPort );
    }

    @Override
    public void close() {
        if( null != jedis ){
            try {
                jedis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if( null != singleClient ){
            try {
                singleClient.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public Object getRedisValue(int db, String key) {
        return super.getRedisValue(this, db, key);
    }

    @Override
    public Object scanRedis(int db, String key) {
        return super.scanRedis(this, key);
    }

    @Override
    public JedisCommands getRedisCommands() {
        return jedis;
    }
}
