package com.newegg.ec.redis.client.entity;

import java.io.Serializable;

/**
 * Created by lf52 on 2019/2/20.
 * redis 返回结果的 k-v键值对
 * @param <V>
 */
public class KeyValue<V> implements Serializable{

	private static final long serialVersionUID = -2265034850324338136L;
	
	private String key;
	
	private V value;
	
	public KeyValue() {
		super();
	}

	public KeyValue(String key, V value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public V getValue() {
		return value;
	}
	
	public void setValue(V value) {
		this.value = value;
	}

	public static String[] keys(KeyValue<?>[] kvs){
		if (kvs == null || kvs.length == 0) {
			return null;
		}
		String[] keys = new String[kvs.length];
		for (int i = 0; i < kvs.length; i++) {
			keys[i] = kvs[i].getKey();
		}
		return keys;
	}

}
