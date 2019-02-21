package com.newegg.ec.redis.client;

import com.newegg.ec.redis.client.entity.KeyValue;
import com.newegg.ec.redis.client.entity.ValueAndScore;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract Redis Client implements by lettuce
 * Not thread safe
 * @author wc4t
 */
@SuppressWarnings("unchecked")
public  class StandAloneLettuceClient extends RedisClusterClient {
	protected  StatefulRedisConnection<String, byte[]> byteRedisConnect;
	
	protected  StatefulRedisConnection<String, String> stringRedisConnect;
	
	
	public StandAloneLettuceClient(StatefulRedisConnection<String, byte[]> byteConnect, StatefulRedisConnection<String, String> stringConnect) {
		if (byteConnect != null) {
			this.bytecommand = byteConnect.sync();
			this.byteRedisConnect = byteConnect;
		}
		if (stringConnect != null) {
			this.command = stringConnect.sync();
			this.stringRedisConnect =  stringConnect;
		}
	}
	
	public StandAloneLettuceClient(StatefulRedisConnection<String, byte[]> byteConnect,
								   StatefulRedisConnection<String, String> stringConnect, int db) {
		this(byteConnect, stringConnect);
		 
		if (bytecommand != null && bytecommand instanceof RedisCommands) {
			((RedisCommands<String, byte[]>) bytecommand).select(db);
		}
		
		if (command != null) {
			((RedisCommands<String, String>) command).select(db);
		}
	}

	@Override
	public List<String> clusterNodes() {
		return null;
	}

	@Override
	public Boolean hset(long ttl,boolean overrwrite,KeyValue<Map<String, String>>... kvs) {
		if (kvs == null || kvs.length == 0) {
			return true;
		}
		((RedisCommands<String, String>) command).multi();
		super.hset(ttl, overrwrite, kvs);
		((RedisCommands<String, String>) command).exec();
		return true;
	}


	@Override
	public Boolean hdel(KeyValue<Map<String, String>>... kvs) {
		if (kvs == null || kvs.length == 0) {
			return true;
		}
		((RedisCommands<String, String>) command).multi();
		super.hdel(kvs);
		((RedisCommands<String, String>) command).exec();
		return true;
	}

	@Override
	public Boolean lpush(long ttl,boolean overrwrite,KeyValue<List<String>>... kvs) {
		if (kvs == null || kvs.length == 0) {
			return true;
		}
		((RedisCommands<String, String>) command).multi();
		super.lpush(ttl, overrwrite, kvs);
		((RedisCommands<String, String>) command).exec();
		return true;
	}

	@Override
	public Boolean lrem(KeyValue<List<String>>... kvs) {
		if (kvs == null || kvs.length == 0) {
			return true;
		}
		((RedisCommands<String, String>) command).multi();
		super.lrem(kvs);
		((RedisCommands<String, String>) command).exec();
		return true;
	}


	@Override
	public Boolean sadd(long ttl,boolean overrwrite,KeyValue<Set<String>>... kvs) {
		if (kvs == null || kvs.length == 0) {
			return true;
		}
		((RedisCommands<String, String>) command).multi();
		super.sadd(ttl, overrwrite, kvs);
		((RedisCommands<String, String>) command).exec();
		return true;
	}

	@Override
	public Boolean srem(KeyValue<Set<String>>... kvs) {
		if (kvs == null || kvs.length == 0) {
			return true;
		}
		((RedisCommands<String, String>) command).multi();
		super.srem(kvs);
		((RedisCommands<String, String>) command).exec();
		return true;
	}


	@Override
	public Boolean zadd(long ttl,boolean overrwrite,KeyValue<Set<ValueAndScore>>... kvs) {
		if (kvs == null || kvs.length == 0) {
			return true;
		}
		((RedisCommands<String, String>) command).multi();
		super.zadd(ttl, overrwrite, kvs);
		((RedisCommands<String, String>) command).exec();
		return true;
	}

	@Override
	public Boolean zrem(KeyValue<Set<ValueAndScore>>... kvs) {
		if (kvs == null || kvs.length == 0) {
			return true;
		}
		((RedisCommands<String, String>) command).multi();
		super.zrem(kvs);
		((RedisCommands<String, String>) command).exec();
		return true;
	}
	
	@Override
	public StatefulConnection<String, byte[]> getByteConnect() {
		return byteConnect;
	}

	@Override
	public StatefulConnection<String, String> getStringConnect() {
		return stringConnect;
	}
	
}
