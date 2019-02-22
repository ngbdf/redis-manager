package com.newegg.ec.redis.client.factory;


import com.newegg.ec.redis.client.RedisBaseClient;
import com.newegg.ec.redis.client.config.RedisFactoryConfig;

/**
 * Thread Safed redis client factory
 * @author wc4t
 */
public interface RedisClientFactory<T extends  AbstractLettuceRedisClientFactory> {

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
	 * quick build Factory with nodelist & password
	 * @param nodeList
	 * @param password
	 * @return
	 */
	public RedisFactoryConfig buildConfig(String nodeList,String password);

	/**
	 * reset factory with password
	 */
	public T resetFactory(RedisFactoryConfig config,String password);

	/**
	 * close the factory and clear resource
	 */
	public void shutdown();

}
