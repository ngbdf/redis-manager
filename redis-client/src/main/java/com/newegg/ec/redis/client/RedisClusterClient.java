package com.newegg.ec.redis.client;

import com.google.common.base.Strings;
import com.newegg.ec.redis.client.entity.KeyValue;
import com.newegg.ec.redis.client.entity.ValueAndScore;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;

import java.util.*;

public class RedisClusterClient implements LettuceRedisClient {
	protected  RedisClusterCommands<String, byte[]> bytecommand;
	protected  StatefulRedisClusterConnection<String, byte[]> byteConnect;

	protected  RedisClusterCommands<String, String> command;
	protected  StatefulRedisClusterConnection<String, String> stringConnect;

	protected RedisClusterClient() {
	}


	public RedisClusterClient(StatefulRedisClusterConnection<String, byte[]> byteConnect, StatefulRedisClusterConnection<String, String> stringConnect) {
		if (byteConnect != null) {
			this.bytecommand = byteConnect.sync();
			this.byteConnect = byteConnect;
		}
		if (stringConnect != null) {
			this.command = stringConnect.sync();
			this.stringConnect = stringConnect;
		}
	}
	
	
	@Override
	public Boolean auth(String password) {
		return !Strings.isNullOrEmpty(command.auth(password));
	}

	@Override
	public Long del(String... keys) {
		return command.del(keys);
	}

	@Override
	public Boolean ping() {
		return !Strings.isNullOrEmpty(command.ping());
	}

	@Override
	public Map<String, String> configGet(String parameter) {
		return command.configGet(parameter);
	}

	@Override
	public Boolean configSet(KeyValue<String>... kvs) {
		for (KeyValue<String> kv : kvs){
			command.configSet(kv.getKey(),kv.getValue());
		}
		return true;
	}

	@Override
	public Boolean configReWrite() {
		return !Strings.isNullOrEmpty(command.configRewrite());
	}

	@Override
	public void scan(int rowSize, ScannerHandler scannerHandler) {
		ScanArgs scanArgs = new ScanArgs();
		scanArgs.limit(rowSize);
		while(true){
			KeyScanCursor<String> result = command.scan(scanArgs);
			scannerHandler.handle(result.getKeys());
			if (result.getCursor().equals("0")) {
				break;
			}else {
				scanArgs.match(result.getCursor());
			}
		}
	}

	@Override
	public List<String> clusterNodes() {

		List<String> result = new LinkedList<>();
		String[] nodes =command.clusterNodes().split("\n");
		for (String node : nodes) {
			String[] arr = node.split(" ");
			result.add(arr[1]);
		}
		return result;
	}

	public Map<String,String> clusterInfo() {

		Map<String, String> result = new HashMap();
		String[] clusterinfos =command.clusterInfo().split("\r\n");
		for (String info : clusterinfos) {
			String[] arr = info.split(":");
			result.put(arr[0], arr[1]);
		}
		return result;
	}

	@Override
	public Boolean contains(String... key) {
		return command.exists(key) == 1 ? true : false;
	}

	@Override
	public Long decr(String key) {
		return command.decr(key);
	}

	@Override
	public Long decrby(String key,Long amount) {
		return command.decrby(key, amount);
	}

	@Override
	public Long incr(String key) {
		return command.incr(key);
	}

	@Override
	public Long incrby(String key,Long amount) {
		return command.incrby(key, amount);
	}

	@Override
	public String info() {
		return command.info();
	}

	@Override
	public List<String> get(String... keys) {
		if (keys == null || keys.length == 0 ) {
			return null;
		}
		List<String> records = new ArrayList<>();
		if (keys.length > 1) {
			List<io.lettuce.core.KeyValue<String, String>> kvs = command.mget(keys);
			for (io.lettuce.core.KeyValue<String, String> kv : kvs) {
				records.add( kv.getValue());
			}
			return records;
		}else {
			String value = command.get(keys[0]);
			records.add(value);
		}
		return records;
	}

	@Override
	public List<byte[]> getbyte(String... keys) {
		if (keys == null || keys.length == 0 ) {
			return null;
		}
		List<byte[]> records = new ArrayList<>();
		if (keys.length > 1) {
			List<io.lettuce.core.KeyValue<String, byte[]>> kvs = bytecommand.mget(keys);
			for (io.lettuce.core.KeyValue<String, byte[]> kv : kvs) {
				records.add(kv.getValue());
			}
			return records;
		}else {
			byte[] value = bytecommand.get(keys[0]);
			records.add(value);
		}
		return records;
	}

	@Override
	public Boolean set(long ttl, KeyValue<String>... kvs) {
		if (kvs == null || kvs.length == 0) {
			return true;
		}
		Map<String, String> keyvalues = new  HashMap<>();
		if (kvs.length > 1) {
			for (KeyValue<String> kv : kvs) {
				keyvalues.put(kv.getKey(), kv.getValue());
			}
			command.mset(keyvalues);
		}else {
			command.set(kvs[0].getKey(), kvs[0].getValue());
		}
		if (ttl > 0) {
			ttl(kvs, ttl);
		}
		return true;
	}

	@Override
	public Boolean setbyte(long ttl,KeyValue<byte[]>... kvs) {
		if (kvs == null || kvs.length == 0) {
			return true;
		}
		Map<String, byte[]> keyvalues = new  HashMap<>();
		if (kvs.length > 1) {
			for (KeyValue<byte[]> kv : kvs) {
				keyvalues.put(kv.getKey(), kv.getValue());
			}
			bytecommand.mset(keyvalues);
		}else {
			bytecommand.set(kvs[0].getKey(), kvs[0].getValue());
		}
		if (ttl > 0) {
			ttl(kvs, ttl);
		}
		return true;
	}

	@Override
	public List<Map<String,String>> hget(List<String> fields, String... keys) {
		if (keys == null || keys.length == 0) {
			return null;
		}
		List<Map<String, String>> records = new ArrayList<>();
		if (fields == null || fields.isEmpty()) {
			for (String key : keys) {
				records.add(command.hgetall(key));
			}
		}else {
			String[] fl = new  String[fields.size()];
			fl =  fields.toArray(fl);
			for (String key : keys) {
				Map<String, String> valueTemp = new HashMap<>();
				List<io.lettuce.core.KeyValue<String, String>> values = command.hmget(key,fl);
				for (io.lettuce.core.KeyValue<String, String> keyValue : values) {
					valueTemp.put(keyValue.getKey(), keyValue.getValue());
				}
				records.add(valueTemp);
			}
		}
		return records;
	}

	@Override
	public List<Map<String, String>> hgetall(String... keys) {
		if (keys == null || keys.length == 0) {
			return null;
		}
		List<Map<String, String>> records = new ArrayList<>();
		for (String key : keys) {
			records.add(command.hgetall(key));
		}
		return records;
	}

	@Override
	public Boolean hset(long ttl,boolean overrwrite,KeyValue<Map<String, String>>... kvs) {
		if (kvs == null || kvs.length == 0) {
			return true;
		}
		if (overrwrite) {
			command.del(KeyValue.keys(kvs));
		}
		for (KeyValue<Map<String, String>> keyValue : kvs) {
			command.hmset(keyValue.getKey(), keyValue.getValue());
		}
		if (ttl > 0) {
			ttl(kvs, ttl);
		}
		return true;
	}


	@Override
	public Boolean hdel(KeyValue<Map<String, String>>... kvs) {
		if (kvs == null || kvs.length == 0) {
			return true;
		}
		for (KeyValue<Map<String, String>> keyValue : kvs) {
			String [] fileds = new String[keyValue.getValue().size()];
			fileds = keyValue.getValue().keySet().toArray(fileds);
			command.hdel(keyValue.getKey(), fileds);
		}
		return true;
	}

	@Override
	public List<List<String>> lrange(int start, int end, String... keys) {
		if (keys == null || keys.length == 0) {
			return null;
		}
		List<List<String>> records = new ArrayList<>();
		if (start == end) {
			for (String  key: keys) {
				List<String> value = new ArrayList<>();
				value.add(command.lindex(key, start));
				records.add(value);
			}
		}else {
			for (String  key: keys) {
				records.add(command.lrange(key, start, end));
			}
		}
		return records;
	}

	@Override
	public Boolean lpush(long ttl,boolean overrwrite,KeyValue<List<String>>... kvs) {
		if (kvs == null || kvs.length == 0) {
			return true;
		}
		if (overrwrite) {
			command.del(KeyValue.keys(kvs));
		}
		for (KeyValue<List<String>> keyValue : kvs) {
			String[] values = new  String[keyValue.getValue().size()];
			values =   keyValue.getValue().toArray(values);
			command.lpush(keyValue.getKey(), values);
		}
		if (ttl > 0) {
			ttl(kvs, ttl);
		}
		return true;
	}

	@Override
	public Boolean lrem(KeyValue<List<String>>... kvs) {
		if (kvs == null || kvs.length == 0) {
			return true;
		}
		for (KeyValue<List<String>> keyValue : kvs) {
			for (String value : keyValue.getValue()) {
				command.lrem(keyValue.getKey(), 0,value );
			}
		}
		return true;
	}

	@Override
	public List<Set<String>> smembers(String... keys) {
		if (keys == null || keys.length == 0) {
			return null;
		}
		List<Set<String>>  records = new ArrayList<>();
		for (String key : keys) {
			records.add(command.smembers(key));
		}
		return records;
	}

	@Override
	public Boolean sadd(long ttl, boolean overrwrite, KeyValue<Set<String>>... kvs){
		if (kvs == null || kvs.length == 0) {
			return true;
		}
		if (overrwrite) {
			command.del(KeyValue.keys(kvs));
		}
		for (KeyValue<Set<String>> keyValue : kvs) {
			String[] values = new  String[keyValue.getValue().size()];
			values =   keyValue.getValue().toArray(values);
			command.sadd(keyValue.getKey(),values);
		}
		return true;
	}

	@Override
	public Boolean srem(KeyValue<Set<String>>... kvs) {
		if (kvs == null || kvs.length == 0) {
			return true;
		}
		for (KeyValue<Set<String>> keyValue : kvs) {
			String[] values = new  String[keyValue.getValue().size()];
			values =   keyValue.getValue().toArray(values);
			command.srem(keyValue.getKey(), values);
		}
		return true;
	}

	@Override
	public List<List<String>> zrange(long start, long stop, String... keys) {
		if (keys == null || keys.length == 0) {
			return null;
		}
		List<List<String>> records = new ArrayList<>();
		for (String  key: keys) {
			records.add(command.zrange(key, start, stop));
		}
		return records;
	}

	@Override
	public Boolean zadd(long ttl,boolean overrwrite,KeyValue<Set<ValueAndScore>>... kvs) {
		if (kvs == null || kvs.length == 0) {
			return true;
		}
		if (overrwrite) {
			command.del(KeyValue.keys(kvs));
		}
		for (KeyValue<Set<ValueAndScore>> keyValue : kvs) {
			ScoredValue<String>[] values = new ScoredValue[keyValue.getValue().size()];
			int i = 0;
			for (ValueAndScore valueAndScore : keyValue.getValue()) {
				values[i++] = ScoredValue.fromNullable(valueAndScore.getScore(),valueAndScore.getValue());
			}
			values =   keyValue.getValue().toArray(values);
			command.zadd(keyValue.getKey(),values);
		}
		return true;
	}

	@Override
	public Boolean zrem(KeyValue<Set<ValueAndScore>>... kvs) {
		if (kvs == null || kvs.length == 0) {
			return true;
		}
		for (KeyValue<Set<ValueAndScore>> keyValue : kvs) {
			String[] values = new  String[keyValue.getValue().size()];
			values =   keyValue.getValue().toArray(values);
			command.zrem(keyValue.getKey(),values );
		}
		return true;
	}
	
	private Boolean ttl(KeyValue<?>[] kvs,long ttl){
		for (int i = 0; i < kvs.length; i++) {
			command.pexpire(kvs[i].getKey(), ttl);
		}
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
