package com.newegg.ec.redis.utils;

import java.util.Random;
import java.util.regex.Pattern;

import com.moilioncircle.redis.replicator.rdb.datatype.ExpiredType;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueHash;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueList;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueSet;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueString;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyStringValueZSet;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.moilioncircle.redis.replicator.rdb.datatype.ZSetEntry;

/**
 * @author：Truman.P.Du
 * @createDate: 2018年3月28日 下午3:13:12
 * @version:1.0
 * @description: Redis 数据在内存中存储占用字节计算 假如我们通过解析 rdb，获取到了一个 key 为 hello，value 为
 *               world，类型为 string ，ttl 为 1440 的一条记录，它的内存使用是这样的: 一个 dictEntry
 *               的消耗，Redis db 就是一个大 dict，每对 kv 都是其中的一个 entry ； 一 个 redisObject
 *               的消耗，redisObject 是为了在同一个 dict 内能够存储不同类型的 value，而使用的一个通用的数据结构，全名是
 *               Redis Object； 存储 key 的 sds 消耗，sds 是 Redis 中存储字符串使用的数据结构；
 *               存储过期时间消耗； 存储 value 的sds 消耗； relate to
 *               http://www.infoq.com/cn/articles/analysis-redis
 *               http://www.cnblogs.com/kismetv/p/8654978.html 该代码参考以下实现：
 *               https://github.com/xueqiu/rdr/blob/master/memprofiler.go
 *               https://github.com/gamenet/redis-memory-analyzer/blob/master/rma/redis.py
 */
public class RedisMemCalculate {
	private static final long redisObject = (long)(8 + 8);
	// 一个dictEntry，24字节，jemalloc会分配32字节的内存块
	private static final long dicEntry = (long)(2 * 8 + 8 + 8);
	private static final String patternString = "^[-\\\\+]?[\\\\d]*$";

	private static long skiplistMaxLevel = 32;
	private static long redisSharedInterges = 10000;
	private static long longSize = 8;
	private static long pointerSize = 8;

	/**
	 * 一个SDS结构占据的空间为：free所占长度+len所占长度+ buf数组的长度=4+4+len+1=len+9
	 * 
	 * @param length
	 * @return
	 */
	private static long sds(long length) {
		long mem = 9 + length;
		return mem;
	}

	/**
	 * 
	 * 计算 string byte 大小
	 * 
	 * @param kv
	 *            https://searchdatabase.techtarget.com.cn/wp-content/uploads/res/database/article/2011/2011-11-14-16-56-18.jpg
	 * @return
	 */
	public static long CalculateString(KeyStringValueString kv) {
		long mem = KeyExpiryOverhead(kv);
		mem = dicEntry + SizeofString(kv.getRawKey());
		mem = mem + redisObject + SizeofString(kv.getValueAsString());
		return mem;
	}

	public static long CalculateLinkedList(KeyStringValueList kv) {
		long mem = KeyExpiryOverhead(kv);
		mem = mem + SizeofString(kv.getRawKey());
		mem = mem + redisObject;
		mem = mem + dicEntry;
		long length = kv.getValueAsStringList().size();
		mem = mem + LinkedListEntryOverhead() * length;
		mem = mem + LinkedlistOverhead();
		mem = mem + redisObject * length;
		for (String value : kv.getValueAsStringList()) {
			mem = mem + SizeofString(value);
		}

		return mem;
	}
	
	public static long CalculateZipList(KeyStringValueList kv) {
		long mem = KeyExpiryOverhead(kv);
		mem = mem + dicEntry;
		mem = mem + SizeofString(kv.getRawKey());
		mem = mem + redisObject;
		long length = kv.getValueAsStringList().size();
		mem = mem + ZiplistOverhead(length);
        
		for (String value : kv.getValueAsStringList()) {
			mem = mem + ZiplistAlignedStringOverhead(value);
		}

		return mem;
	}

	public static long CalculateHash(KeyStringValueHash kv) {
		long mem = KeyExpiryOverhead(kv);
		mem = mem + SizeofString(kv.getRawKey());
		mem = mem + redisObject;
		mem = mem + dicEntry;
		long length = kv.getValueAsHash().size();
		mem = mem + HashtableOverhead(length);

		for (String key : kv.getValueAsHash().keySet()) {
			String value = kv.getValueAsHash().get(key);
			mem = mem + SizeofString(key);
			mem = mem + SizeofString(value);
			mem = mem + 2 * redisObject;
			mem = mem + HashtableEntryOverhead();
		}

		return mem;
	}

	public static long CalculateSet(KeyStringValueSet kv) {
		long mem = KeyExpiryOverhead(kv);

		mem = mem + SizeofString(kv.getRawKey());
		mem = mem + redisObject;
		mem = mem + dicEntry;
		long length = kv.getValueAsSet().size();
		mem = mem + HashtableOverhead(length);
		mem = mem + redisObject * length;

		for (String value : kv.getValueAsSet()) {
			mem = mem + SizeofString(value);
			mem = mem + 2 * redisObject;
			mem = mem + HashtableEntryOverhead();
		}

		return mem;
	}
	
	public static long CalculateIntSet(KeyStringValueSet kv) {
		long mem = KeyExpiryOverhead(kv);
		mem = mem + dicEntry;
		mem = mem + SizeofString(kv.getRawKey());
		mem = mem + redisObject;
		
		long length = kv.getValueAsSet().size();
		mem = mem + IntsetOverhead(length);

		for (String value : kv.getValueAsSet()) {
			mem = mem + ZiplistAlignedStringOverhead(value);
		}

		return mem;
	}

	public static long CalculateZSet(KeyStringValueZSet kv) {
		long mem = KeyExpiryOverhead(kv);

		mem = mem + SizeofString(kv.getRawKey());
		mem = mem + redisObject;
		mem = mem + dicEntry;
		long length = kv.getValueAsSet().size();
		mem = mem + SkiplistOverhead(length);
		mem = mem + redisObject * length;

		for (ZSetEntry value : kv.getValueAsZSet()) {
			mem = mem + 8;
			mem = mem + SizeofString(value.getElement());
			// TODO 还有个 score
			mem = mem + 2 * redisObject;
			mem = mem + SkiplistEntryOverhead();
		}

		return mem;
	}

	// TopLevelObjOverhead get memory use of a top level object
	// Each top level object is an entry in a dictionary, and so we have to include
	// the overhead of a dictionary entry
	public static long TopLevelObjOverhead() {
		return HashtableEntryOverhead();
	}

	/**
	 * SizeofString get memory use of a string
	 * https://github.com/antirez/redis/blob/unstable/src/sds.h
	 * 
	 * @param bytes
	 * @return
	 */
	public static long SizeofString(byte[] bytes) {
		String value = new String(bytes);

		if (isInteger(value)) {
			try {
				Long num = Long.parseLong(value);
				if (num < redisSharedInterges && num > 0) {
					return 0;
				}
				return 8;
			} catch (NumberFormatException e) {
			}
		}

		return Jemalloc.assign(sds(bytes.length));
	}
	
	
	public static long SizeofString(String value) {
		if (isInteger(value)) {
			try {
				Long num = Long.parseLong(value);
				if (num < redisSharedInterges && num > 0) {
					return 0;
				}
				return 8;
			} catch (NumberFormatException e) {
			}
		}

		return Jemalloc.assign(sds(value.length()));
	}
	
	public static long DictOverhead(long size) {
		 return Jemalloc.assign(56 + 2*pointerSize + nextPower(size) * 3*8);
	}

	public static boolean isInteger(String str) {
		Pattern pattern = Pattern.compile(patternString);
		return pattern.matcher(str).matches();
	}

	/**
	 * 过期时间也是存储为一个 dictEntry，时间戳为 int64；
	 * 
	 * @param kv
	 * @return
	 */
	// KeyExpiryOverhead get memory useage of a key expiry
	// Key expiry is stored in a hashtable, so we have to pay for the cost of a
	// hashtable entry
	// The timestamp itself is stored as an int64, which is a 8 bytes
	@SuppressWarnings("rawtypes")
	public static long KeyExpiryOverhead(KeyValuePair kv) {
		// If there is no expiry, there isn't any overhead
		if (kv.getExpiredType() == ExpiredType.NONE) {
			return 0;
		}
		return HashtableEntryOverhead() + 8;
	}

	public static long HashtableOverhead(long size) {
		return 4 + 7 * longSize + 4 * pointerSize + nextPower(size) * pointerSize * 3 / 2;
	}

	// HashtableEntryOverhead get memory use of hashtable entry
	// See https://github.com/antirez/redis/blob/unstable/src/dict.h
	// Each dictEntry has 2 pointers + int64
	public static long HashtableEntryOverhead() {
		return 2 * pointerSize + 8;
	}

	public static long ZiplistOverhead(long size) {
		return Jemalloc.assign(12 + 21 * size);
	}

	public static long ZiplistAlignedStringOverhead(String value) {
		try {
			Long.parseLong(value);
			return 8;
		} catch (NumberFormatException e) {
		}
		return Jemalloc.assign(value.length());
	}

	// LinkedlistOverhead get memory use of a linked list
	// See https://github.com/antirez/redis/blob/unstable/src/adlist.h
	// A list has 5 pointers + an unsigned long
	public static long LinkedlistOverhead() {
		return longSize + 5 * pointerSize;
	}

	// LinkedListEntryOverhead get memory use of a linked list entry
	// See https://github.com/antirez/redis/blob/unstable/src/adlist.h
	// A node has 3 pointers
	public static long LinkedListEntryOverhead() {
		return 3 * pointerSize;
	}

	// SkiplistOverhead get memory use of a skiplist
	public static long SkiplistOverhead(long size) {
		return 2 * pointerSize + HashtableOverhead(size) + (2 * pointerSize + 16);
	}

	// SkiplistEntryOverhead get memory use of a skiplist entry
	public static long SkiplistEntryOverhead() {
		return HashtableEntryOverhead() + 2 * pointerSize + 8 + (pointerSize + 8) * zsetRandLevel();
	}

	public static long nextPower(long size) {
		long power = 1;
		while (power <= size) {
			power = power << 1;
		}
		return power;
	}

	public static long zsetRandLevel() {
		long level = 1;
		int rint = new Random().nextInt(65536);
		int flag = 65535 / 4;
		while (rint < flag) {// skiplistP
			level++;
			rint = new Random().nextInt(65536);
		}
		if (level < skiplistMaxLevel) {
			return level;
		}
		return skiplistMaxLevel;
	}
	
	public static long  IntsetOverhead(long size) {
	    //     typedef struct intset {
	    //     uint32_t encoding;
	    //     uint32_t length;
	    //     int8_t contents[];
	    //      } intset;
	    return (4 + 4) * size;
	}
	public static void main(String[] args) {
		RedisMemCalculate redisMemCalculate = new RedisMemCalculate();
		redisMemCalculate.TestTotalStringCost();

	}

	/**
	 * Redis的字典是使用一个桶bucket，通过对key进行hash得到的索引值index，然后将key-value的数据存在桶的index位置
	 * https://www.2cto.com/database/201312/266901.html
	 * 
	 */
	public void TestTotalStringCost() {
		KeyStringValueString kv = new KeyStringValueString();
		kv.setRawKey("aa10000".getBytes());
		kv.setRawValue("bb10000".getBytes());
		kv.setValue("bb10000");
		// kv.setExpiredType(ExpiredType.SECOND);
		System.out.println(
				RedisMemCalculate.CalculateString(kv) * 90000 + RedisMemCalculate.caculateBucketSzie(90000) * 8);
	}

	public static long caculateBucketSzie(long size) {
		long remainder = size % 2;
		double n = Math.log((double)(size - remainder)) / Math.log(2);
		double newSize = Math.pow(2, Math.ceil(n));
		return (long)newSize;
	}

}
