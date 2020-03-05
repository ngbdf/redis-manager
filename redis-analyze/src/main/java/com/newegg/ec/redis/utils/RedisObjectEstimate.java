package com.newegg.ec.redis.utils;

import java.util.Map.Entry;


import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueHash;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueList;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueSet;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueString;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueZSet;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.moilioncircle.redis.replicator.rdb.datatype.ZSetEntry;

/**
 * @author：Truman.P.Du
 * @createDate: 2019年2月21日 下午1:47:42
 * @version:1.0
 * @description: 根据KeyValuePair 估算内存占用大小
 */
public class RedisObjectEstimate {
    /**
     * 
     * @param kv
     * @param useCustomAlgo
     *   true：根据RedisMemCalculate 预估内存占用大小
     *   false:根据java对象预估内存占用大小，与redis实际占用存在较大差异
     * @return
     */
	public static long getRedisObjectSize(KeyValuePair<?> kv, boolean useCustomAlgo) {
		if (useCustomAlgo) {
			return getRedisMemCalculate(kv);
		}
		return getObjectSize(kv);
	}

	/**
	 * 
	 * https://stackoverflow.com/questions/9368764/calculate-size-of-object-in-java
	 * 
	 * @param o
	 * @return
	 */
	public static long getObjectSize(KeyValuePair<?> kv) {
		long size = 0L;
		switch (kv.getValueRdbType()) {
		case 0: // string
			KeyStringValueString theStringKv = (KeyStringValueString) kv;
			size = theStringKv.getRawValue().length;
			break;
		case 10: // List_ZipList
			KeyStringValueList zipListKv = (KeyStringValueList) kv;
			for (byte[] rawByte : zipListKv.getRawValue()) {
				size = size + rawByte.length;
			}
			break;
		case 1: // List;
		case 14: // List_QuickList
			KeyStringValueList theListKv = (KeyStringValueList) kv;
			for (byte[] rawByte : theListKv.getRawValue()) {
				size = size + rawByte.length;
			}
			break;
		case 2: // Set
			KeyStringValueSet theSetKv = (KeyStringValueSet) kv;
			for (byte[] rawByte : theSetKv.getRawValue()) {
				size = size + rawByte.length;
			}
			break;
		case 11: // Set_IntSet
			KeyStringValueSet intSetKv = (KeyStringValueSet) kv;
			for (byte[] rawByte : intSetKv.getRawValue()) {
				size = size + rawByte.length;
			}
			break;
		case 3: // ZSet
		case 5: // ZSet_2
		case 12: // ZSet_ZipList - LinkedHashSet
			KeyStringValueZSet theZSetKv = (KeyStringValueZSet) kv;
			for (ZSetEntry zSetEntry : theZSetKv.getValue()) {
				size = size + zSetEntry.getRawElement().length;
			}
			break;
		case 4: // Hash
		case 9: // Hash_ZipMap
		case 13: // Hash_ZipList - LinkedHashMap
			KeyStringValueHash theHashKv = (KeyStringValueHash) kv;
			for (Entry<byte[], byte[]> entry : theHashKv.getRawValue().entrySet()) {
				size = size + entry.getKey().length + entry.getValue().length;
			}
			break;
		default:
			break;
		}
		return size;
	}

	/**
	 * 使用 {@link RedisMemCalculate}
	 * 
	 * @param kv
	 * @return
	 */
	public static long getRedisMemCalculate(KeyValuePair<?> kv) {
		long size = 0L;
		switch (kv.getValueRdbType()) {
		case 0: // string
			KeyStringValueString theStringKv = (KeyStringValueString) kv;
			size = RedisMemCalculate.CalculateString(theStringKv);
			break;

		case 10: // List_ZipList
			KeyStringValueList zipListKv = (KeyStringValueList) kv;
			size = RedisMemCalculate.CalculateZipList(zipListKv);
			break;
		case 1: // List;
		case 14: // List_QuickList
			KeyStringValueList theListKv = (KeyStringValueList) kv;
			size = RedisMemCalculate.CalculateLinkedList(theListKv);
			break;
		case 2: // Set
			KeyStringValueSet theSetKv = (KeyStringValueSet) kv;
			size = RedisMemCalculate.CalculateSet(theSetKv);
			break;
		case 11: // Set_IntSet
			KeyStringValueSet intSetKv = (KeyStringValueSet) kv;
			size = RedisMemCalculate.CalculateIntSet(intSetKv);
			break;
		case 3: // ZSet
		case 5: // ZSet_2
		case 12: // ZSet_ZipList - LinkedHashSet
			KeyStringValueZSet theZSetKv = (KeyStringValueZSet) kv;
			size = RedisMemCalculate.CalculateZSet(theZSetKv);
			break;
		case 4: // Hash
		case 9: // Hash_ZipMap
		case 13: // Hash_ZipList - LinkedHashMap
			KeyStringValueHash theHashKv = (KeyStringValueHash) kv;
			size = RedisMemCalculate.CalculateHash(theHashKv);
			break;
		default:
			break;
		}
		return size;
	}

	protected String getDataTypeName(int type) {
		String typeName = null;
		switch (type) {
		case 0:
			typeName = "String";
			break;
		case 1:
		case 10:
		case 14:
			typeName = "List";
			break;
		case 2:
		case 3:
		case 5:
		case 11:
		case 12:
			typeName = "Set";
			break;
		// case 3:
		// typeName = "ZSet";
		// break;
		case 4:
		case 9:
		case 13:
			typeName = "Hash";
			break;
		// case 5:
		// typeName = "ZSet_2";
		// break;
		case 6:
			typeName = "Module";
			break;
		case 7:
			typeName = "Module_2";
			break;
		// case 9:
		// typeName = "Hash_ZipMap";
		// break;
		// case 10:
		// typeName = "List_ZipList";
		// break;
		// case 11:
		// typeName = "Set_IntSet";
		// break;
		// case 12:
		// typeName = "ZSet_ZipList";
		// break;
		// case 13:
		// typeName = "Hash_ZipList";
		// break;
		// case 14:
		// typeName = "List_QuickList";
		// break;
		default:
			break;
		}
		return typeName;
	}
}
