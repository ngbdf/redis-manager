package com.newegg.ec.redis.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author：Truman.P.Du
 * @createDate: 2018年10月23日 下午2:16:32
 * @version:1.0
 * @description: 应用缓存
 */
public class AppCache {

	public static Map<String, Set<String>> reportCacheMap = new ConcurrentHashMap<String, Set<String>>();
	public static List<Long> redisUsedMems = new ArrayList<Long>();

}
