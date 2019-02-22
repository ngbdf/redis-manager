package com.newegg.ec.redis.client.factory;


import com.newegg.ec.redis.client.RedisBaseClient;
import com.newegg.ec.redis.client.RedisClusterClient;
import com.newegg.ec.redis.client.RedisStandAloneClient;
import com.newegg.ec.redis.client.config.RedisFactoryConfig;
import com.newegg.ec.redis.client.entity.StringByteRedisCodec;
import com.newegg.ec.redis.client.exception.RedisClientException;
import io.lettuce.core.RedisURI;
import io.lettuce.core.RedisURI.Builder;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClusterClientFactory extends AbstractLettuceRedisClientFactory {

	private GenericObjectPool<StatefulRedisClusterConnection<String, String>> stringPool;
	private GenericObjectPool<StatefulRedisClusterConnection<String, byte[]>> bytePool;


	/**
	 * 自定义Config项构建Factory
	 * @param config
	 */
	public ClusterClientFactory(RedisFactoryConfig config) {
		super(config);

		io.lettuce.core.cluster.RedisClusterClient clusterClient = createClient(config);
		this.stringPool = ConnectionPoolSupport.createGenericObjectPool(new Supplier<StatefulRedisClusterConnection<String, String>>() {
			@Override
			public StatefulRedisClusterConnection<String, String> get() {
				return clusterClient.connect();
			}
		},stringPoolConfig);

		this.bytePool = ConnectionPoolSupport.createGenericObjectPool(new Supplier<StatefulRedisClusterConnection<String, byte[]>>() {
			@Override
			public StatefulRedisClusterConnection<String, byte[]> get() {
				return clusterClient.connect(new StringByteRedisCodec());
			}
		},bytePoolConfig);
	}

	public ClusterClientFactory(String nodeList,String password) {

		super(DEFAULT_CONNECTION,DEFAULT_CONNECTION,DEFAULT_CONNECTION,DEFAULT_CONNECTION);

		RedisFactoryConfig config = buildConfig(nodeList, password);
		io.lettuce.core.cluster.RedisClusterClient clusterClient = createClient(config);
		this.stringPool = ConnectionPoolSupport.createGenericObjectPool(new Supplier<StatefulRedisClusterConnection<String, String>>() {
			@Override
			public StatefulRedisClusterConnection<String, String> get() {
				return clusterClient.connect();
			}
		},stringPoolConfig);

		this.bytePool = ConnectionPoolSupport.createGenericObjectPool(new Supplier<StatefulRedisClusterConnection<String, byte[]>>() {
			@Override
			public StatefulRedisClusterConnection<String, byte[]> get() {
				return clusterClient.connect(new StringByteRedisCodec());
			}
		},bytePoolConfig);

	}

	protected io.lettuce.core.cluster.RedisClusterClient createClient(RedisFactoryConfig redisConfig) {
		List<RedisFactoryConfig.IpAndPort> nodeList = redisConfig.getNodeList();
		if (nodeList != null && !nodeList.isEmpty()) {
			List<RedisURI> redisURIs = new ArrayList<>();
			for (RedisFactoryConfig.IpAndPort node : nodeList) {
				Builder build = RedisURI.builder();
				build.withHost(node.getIp()).withPort(node.getPort());
				build.withTimeout(Duration.ofMillis(redisConfig.getTimeout()));
				if (redisConfig.getPassword() != null) {
					build.withPassword(redisConfig.getPassword());
				}
				redisURIs.add(build.build());
			}


			ClusterTopologyRefreshOptions clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
					.enablePeriodicRefresh(Duration.ofMillis(60)) //long
					.enableAllAdaptiveRefreshTriggers()
					.enablePeriodicRefresh(true) //boolean
					.refreshPeriod(Duration.ofMillis(60)) //long
					.adaptiveRefreshTriggersTimeout(Duration.ofMillis(30))//long
					.refreshTriggersReconnectAttempts(5) //int
					.build();

			ClusterClientOptions clusterClientOptions = ClusterClientOptions.builder()
					.maxRedirects(5) //int
					.topologyRefreshOptions(clusterTopologyRefreshOptions)
					.build();

			io.lettuce.core.cluster.RedisClusterClient clusterClient = io.lettuce.core.cluster.RedisClusterClient.create(redisURIs);
			clusterClient.setOptions(clusterClientOptions);
			return clusterClient;
		}
		return null;
	}

	protected RedisClusterClient createLettuceClient(String keySpace, StatefulRedisClusterConnection<String, byte[]> byteconnnect,
														StatefulRedisClusterConnection<String, String> stringconnnect) {
		return new RedisClusterClient(byteconnnect, stringconnnect);
	}

	@Override
	public RedisClusterClient provideClient(String keySpace) {
		StatefulRedisClusterConnection<String, String> stringConnection = null;
		StatefulRedisClusterConnection<String, byte[]> byteConnection = null;

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
				throw new RedisClientException("can not get a string connection for redis!",e);
			}
		}
		return createLettuceClient(keySpace,byteConnection,stringConnection);
	}

	@Override
	public void releaseClient(RedisBaseClient client) {
		if (client instanceof RedisStandAloneClient) {
			RedisStandAloneClient tempClient = (RedisStandAloneClient) client;
			if (tempClient.getByteConnect() != null) {
				bytePool.returnObject((StatefulRedisClusterConnection<String, byte[]>)tempClient.getByteConnect());
			}
			if (tempClient.getStringConnect() != null) {
				stringPool.returnObject((StatefulRedisClusterConnection<String, String>)tempClient.getStringConnect());
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
	public ClusterClientFactory resetFactory(RedisFactoryConfig config,String password) {
           return new ClusterClientFactory(config.resetFactoryConfig(config, password));
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
