package com.newegg.ec.redis.client.factory;


import com.newegg.ec.redis.client.config.RedisFactoryConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public abstract class AbstractLettuceRedisClientFactory implements RedisClientFactory{

	protected GenericObjectPoolConfig stringPoolConfig;
	protected GenericObjectPoolConfig bytePoolConfig;
	
	public AbstractLettuceRedisClientFactory(RedisFactoryConfig config) {
		if (config.getStringMaxConnection() > 0) {
			GenericObjectPoolConfig stringPoolConfig = new GenericObjectPoolConfig();
			stringPoolConfig.setMaxTotal(getMaxIdleConnection(config.getStringMaxConnection()));
			stringPoolConfig.setMinIdle(getMinIdleConnection(config.getStringMaxConnection()));
			this.stringPoolConfig = stringPoolConfig;
		}
		
		if (config.getByteMaxConnection() > 0) {
			GenericObjectPoolConfig bytePoolConfig = new GenericObjectPoolConfig();
			bytePoolConfig.setMaxTotal(getMaxIdleConnection(config.getByteMaxConnection()));
			bytePoolConfig.setMinIdle(getMinIdleConnection(config.getByteMaxConnection()));
			this.bytePoolConfig =bytePoolConfig;
		}
	}
	
	protected abstract int getMaxIdleConnection(int maxConnection);
	
	protected abstract int getMinIdleConnection(int maxConnection);
		
}
