package com.newegg.ec.redis.client.factory;

import com.newegg.ec.redis.client.RedisBaseClient;
import com.newegg.ec.redis.client.RedisStandAloneClient;
import com.newegg.ec.redis.client.config.RedisFactoryConfig;
import com.newegg.ec.redis.client.entity.StringByteRedisCodec;
import com.newegg.ec.redis.client.exception.RedisClientException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.RedisURI.Builder;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.time.Duration;
import java.util.function.Supplier;

public class StandAloneClientFactory extends AbstractLettuceRedisClientFactory {

	private GenericObjectPool<StatefulRedisConnection<String, String>> stringPool;
	private GenericObjectPool<StatefulRedisConnection<String, byte[]>> bytePool;
	
	public StandAloneClientFactory(RedisFactoryConfig redisConfig) {
		super(redisConfig);
		RedisClient redisClient = createClient(redisConfig);
		
		this.stringPool = ConnectionPoolSupport.createGenericObjectPool(new Supplier<StatefulRedisConnection<String, String>>() {

			@Override
			public StatefulRedisConnection<String, String> get() {
				return redisClient.connect();
			}
		},stringPoolConfig);
		
		this.bytePool = ConnectionPoolSupport.createGenericObjectPool(new Supplier<StatefulRedisConnection<String, byte[]>>() {

			@Override
			public StatefulRedisConnection<String, byte[]> get() {
				return redisClient.connect(new StringByteRedisCodec());
			}
		},bytePoolConfig);
	}

	public StandAloneClientFactory(String nodeList,String password) {

		super(DEFAULT_CONNECTION,DEFAULT_CONNECTION,DEFAULT_CONNECTION,DEFAULT_CONNECTION);

		RedisFactoryConfig config = buildConfig(nodeList, password);
		RedisClient redisClient = createClient(config);

		this.stringPool = ConnectionPoolSupport.createGenericObjectPool(new Supplier<StatefulRedisConnection<String, String>>() {

			@Override
			public StatefulRedisConnection<String, String> get() {
				return redisClient.connect();
			}
		},stringPoolConfig);

		this.bytePool = ConnectionPoolSupport.createGenericObjectPool(new Supplier<StatefulRedisConnection<String, byte[]>>() {

			@Override
			public StatefulRedisConnection<String, byte[]> get() {
				return redisClient.connect(new StringByteRedisCodec());
			}
		},bytePoolConfig);

	}


	
	@Override
	public RedisStandAloneClient provideClient(String keySpace) {
		StatefulRedisConnection<String, String> stringConnection = null;
		StatefulRedisConnection<String, byte[]> byteConnection = null; 
		
		if (stringPool != null) {
			try {
				stringConnection = stringPool.borrowObject();
			} catch (Exception e) {
				throw new RedisClientException("can not get a string connection for redis!",e);
			}
		}
		
		if (bytePool != null) {
			try {
				byteConnection = bytePool.borrowObject();
			} catch (Exception e) {
				throw new RedisClientException("can not get a byte connection for redis!",e);
			}
		}
		return createLettuceClient(keySpace,byteConnection,stringConnection);
	}

	protected RedisClient createClient(RedisFactoryConfig redisConfig) {
		if (redisConfig.getNodeList() != null &&  !redisConfig.getNodeList().isEmpty()) {
			Builder build = RedisURI.builder();
			RedisFactoryConfig.IpAndPort ipAndPort = redisConfig.getNodeList().get(0);
			build.withHost(ipAndPort.getIp()).withPort(ipAndPort.getPort()).withTimeout(Duration.ofMillis(redisConfig.getTimeout()));
			if (redisConfig.getPassword() != null) {
				build.withPassword(redisConfig.getPassword());
			}

			RedisClient standAloneClient = RedisClient.create(build.build());
			return standAloneClient;
		}
		return null;
	}

	protected RedisStandAloneClient createLettuceClient(String keySpace, StatefulRedisConnection<String, byte[]> byteconnnect,
			StatefulRedisConnection<String, String> stringconnnect) {
		int db = 0;
		try {
			db = Integer.parseInt(keySpace);
		} catch (Exception e) {
			throw new RedisClientException("keySpace is not number,sentinel keySpace must be int", e);
		}
		return new RedisStandAloneClient(byteconnnect, stringconnnect, db);
	}


	
	@Override
	public void releaseClient(RedisBaseClient client) {
		if (client instanceof RedisStandAloneClient) {
			RedisStandAloneClient tempClient = (RedisStandAloneClient) client;
			if (tempClient.getByteConnect() != null) {
				bytePool.returnObject((StatefulRedisConnection<String, byte[]>)tempClient.getByteConnect());
			}
			if (tempClient.getStringConnect() != null) {
				stringPool.returnObject((StatefulRedisConnection<String, String>)tempClient.getStringConnect());
			}
		}
	}



	@Override
	public void shutdown() {
		if (stringPool != null) {
			stringPool.close();
		}
		if (bytePool != null) {
			stringPool.close();
		}
	}


	@Override
	public StandAloneClientFactory resetFactory(RedisFactoryConfig config,String password) {
		return new StandAloneClientFactory(config.resetFactoryConfig(config,password));
	}


	@Override
	protected int getMaxIdleConnection(int maxConnection) {
		return (int) (maxConnection * 0.3);
	}


	@Override
	protected int getMinIdleConnection(int maxConnection) {
		return (int) (maxConnection * 0.1);
	}
}
