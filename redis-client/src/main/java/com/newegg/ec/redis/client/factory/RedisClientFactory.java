package com.newegg.ec.redis.client.factory;


import com.newegg.ec.redis.client.RedisBaseClient;
import com.newegg.ec.redis.client.config.RedisFactoryConfig;

/**
 * Thread Safed redis client factory
 * @author wc4t
 */
public interface RedisClientFactory {

	/**
	 * provide one redis client
	 * @param keySpace if(sentinel) keySpace = db
	 * @return RedisClient the redis client
	 */
	public RedisBaseClient provideClient(String keySpace);
	
	
	/**
	 * @param client the client to pool
	 * return 
	 */
	public void releaseClient(RedisBaseClient client);
	
	
	/**
	 * close the factory and clear resource
	 */
	public void shutdown();

	/**
	 * reset factory with password
	 */
	public AbstractLettuceRedisClientFactory reset(RedisFactoryConfig config,String password);
	
}
