package com.newegg.ec.redis.client;

import com.newegg.ec.redis.client.entity.KeyValue;
import com.newegg.ec.redis.client.entity.ValueAndScore;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface RedisStringClient {

	/**
	 * redis get/mget command
	 * @param keys
	 * @return
	 */
	public List<String> get(String... keys);

	/**
	 * redis set/mset command
	 * @param ttl
	 * @param kvs
	 * @return
	 */
	public Boolean set(long ttl, KeyValue<String>... kvs);

	/**
	 * redis hget/hmget command
	 * @param fields
	 * @param keys
	 * @return
	 */
	public List<Map<String,String>> hget(List<String> fields, String... keys);


	/**
	 * hgetall command
	 * @param keys
	 * @return
	 */
	public List<Map<String,String>> hgetall(String... keys);

	/**
	 * redis hset/hmset command
	 * @param ttl
	 * @param overrwrite
	 * @param kvs
	 * @return
	 */
	public Boolean hset(long ttl, boolean overrwrite, KeyValue<Map<String, String>>... kvs);

	/**
	 * redis hdel command
	 * @param kvs
	 * @return
	 */
	public Boolean hdel(KeyValue<Map<String, String>>... kvs);

	/**
	 * redis lrange command
	 * @param start
	 * @param end
	 * @param keys
	 * @return
	 */
	public List<List<String>>  lrange(int start, int end, String... keys);

	/**
	 * redis lpush command, add key-listValue to redis
	 * @param ttl
	 * @param overrwrite
	 * @param kvs
	 * @return
	 */
	public Boolean lpush(long ttl, boolean overrwrite, KeyValue<List<String>>... kvs);

	/**
	 * redis lpush command ,remove key-value to redis
	 * @param kvs
	 * @return
	 */
	public Boolean lrem(KeyValue<List<String>>... kvs);

	/**
	 * redis smembers  command , get set type value
	 * @param keys
	 * @return
	 */
	public List<Set<String>> smembers(String... keys);

	/**
	 * redis sadd command,add key-setValue to redis
	 * @param ttl
	 * @param overrwrite
	 * @param kvs
	 * @return
	 */
	public Boolean sadd(long ttl, boolean overrwrite, KeyValue<Set<String>>... kvs);

	/**
	 * redis srem command,remove key-value from redis
	 * @param kvs
	 * @return
	 */
	public Boolean srem(KeyValue<Set<String>>... kvs);

	/**
	 * redis zrange command,get sort set value by score sort
	 * @param start
	 * @param stop
	 * @param keys
	 * @return
	 */
	public List<List<String>> zrange(long start, long stop, String... keys);

	/**
	 * redis zadd command
	 * @param ttl
	 * @param overrwrite
	 * @param kvs
	 * @return
	 */
	public Boolean zadd(long ttl,boolean overrwrite,KeyValue<Set<ValueAndScore>>... kvs);

	/**
	 * redis zrem command
	 * @param kvs
	 * @return
	 */
	public Boolean zrem(KeyValue<Set<ValueAndScore>>... kvs);

	/**
	 * redis get/mget command
	 * @param keys
	 * @return
	 */
	public List<byte[]> getbyte(String... keys);

	/**
	 * redis set/mset command
	 * @param ttl
	 * @param kvs
	 * @return
	 */
	public Boolean setbyte(long ttl, KeyValue<byte[]>... kvs);
	
}
