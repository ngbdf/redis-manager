package com.newegg.ec.redis.client;

import io.lettuce.core.api.StatefulConnection;

public interface LettuceRedisClient extends RedisBaseClient {
	
	/**
	 * 
	 * @return StatefulConnection the byte connection
	 */
	public StatefulConnection<String, byte[]> getByteConnect();
	
	/**
	 * 
	 * @return StatefulConnection the string connection
	 */
	public StatefulConnection<String, String> getStringConnect();
}
