package com.newegg.ec.redis.client.factory;


import com.newegg.ec.redis.client.config.RedisFactoryConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public abstract class AbstractLettuceRedisClientFactory implements RedisClientFactory{

	protected static final int DEFAULT_CONNECTION = 16;

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

	public AbstractLettuceRedisClientFactory(int stringMaxConnection,int stringMinConnection,int byteMaxConnection,int byteMinConnection) {
		if (stringMaxConnection > 0) {
			GenericObjectPoolConfig stringPoolConfig = new GenericObjectPoolConfig();
			stringPoolConfig.setMaxTotal(getMaxIdleConnection(stringMaxConnection));
			stringPoolConfig.setMinIdle(getMinIdleConnection(stringMinConnection));
			this.stringPoolConfig = stringPoolConfig;
		}

		if (byteMaxConnection > 0) {
			GenericObjectPoolConfig bytePoolConfig = new GenericObjectPoolConfig();
			bytePoolConfig.setMaxTotal(getMaxIdleConnection(byteMaxConnection));
			bytePoolConfig.setMinIdle(getMinIdleConnection(byteMinConnection));
			this.bytePoolConfig =bytePoolConfig;
		}
	}

	public RedisFactoryConfig buildConfig(String nodeList, String password) {
		RedisFactoryConfig config = RedisFactoryConfig.builder().setNodeList(nodeList).
				setPassword(password).
				build();
		return config;
	}

	protected abstract int getMaxIdleConnection(int maxConnection);
	
	protected abstract int getMinIdleConnection(int maxConnection);

}
