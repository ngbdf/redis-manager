package com.newegg.ec.redis.client;

import com.newegg.ec.redis.client.entity.KeyValue;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface RedisBaseClient extends RedisStringClient {
		
	/**
	 * Authenticate to the server
	 * @param password  the redis password
	 * @return Boolean  if password correct 
	 */
	public Boolean auth(String password);
	
	/**
	 * Delete one or more key from redis
	 * @param keyss the key Array
	 * @return Long integer-reply The number of keys that were removed
	 */
	public Long del(String... keyss);
	

	/**
	 * Ping redis
	 * @return
	 */
	public Boolean ping();

	/**
	 * get redis node config
	 * @param parameter
	 * @return
	 */
	public Map<String, String> configGet(String parameter);

	/**
	 * set redis config
	 * @param kvs
	 * @return
	 */
	public Boolean configSet(KeyValue<String>... kvs);

	/**
	 * rewrite redis config
	 * @return
	 */
	public Boolean configReWrite();

	/**
	 * scan redis all key by batch
	 * @param rowSize
	 * @param scannerHandler
	 */
	public void scan(int rowSize, ScannerHandler scannerHandler);

	/**
	 * get redis cluster node
	 * @return
	 */
	public List<String> clusterNodes();

	/**
	 * is redis key in
	 * @param key
	 * @return
	 */
	public Boolean contains(String... key);

	/**
	 * redis decr command
	 * @param key
	 * @return
	 */
	public Long decr(String key);

	/**
	 * @param key
	 * @param amount
	 * @return
	 */
	public Long decrby(String key,Long amount);

	/**
	 * redis incr command
	 * @return
	 */
	public Long incr(String key);

	/**
	 * @param key
	 * @param amount
	 * @return
	 */
	public Long incrby(String key,Long amount);

	/**
	 * scan hanlder
	 */
	public interface ScannerHandler{
		
		public void handle(List<String> keys);
	}

}
