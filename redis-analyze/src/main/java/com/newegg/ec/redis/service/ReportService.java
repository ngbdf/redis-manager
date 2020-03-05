package com.newegg.ec.redis.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.newegg.ec.redis.cache.AppCache;
import com.newegg.ec.redis.constant.AnalyzerConstant;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

/**
 * @author Truman.P.Du
 * @date Aug 1, 2019 3:12:05 PM
 * @version 1.0
 */
@Service
public class ReportService {

	public String fixReportData() {
		Map<String, Set<String>> reportCacheMap = AppCache.reportCacheMap;
		long usedMem = 0l;
		for (Long mem : AppCache.redisUsedMems) {
			usedMem = mem + usedMem;
		}
		if (usedMem == 0) {
			String report = JSONObject.toJSONString(reportCacheMap);
			return report;
		}
		Set<String> dataTypeAnalyzeResult = AppCache.reportCacheMap.get(AnalyzerConstant.DATA_TYPE_ANALYZER + "");
		Set<String> newDataTypeAnalyzeResult = new HashSet<>();
		long estimateMem = 0l;
		for (String str : dataTypeAnalyzeResult) {
			JSONObject jsonObject = JSONObject.parseObject(str);
			estimateMem = estimateMem + jsonObject.getLong("bytes");
		}

		BigDecimal precisionRate = new BigDecimal(usedMem).divide(new BigDecimal(estimateMem),4, BigDecimal.ROUND_HALF_UP);

		for (String str : dataTypeAnalyzeResult) {
			JSONObject jsonObject = JSONObject.parseObject(str);
			long bytes = jsonObject.getLong("bytes");
			long fixBytes = new BigDecimal(bytes).multiply(precisionRate).longValue();
			jsonObject.put("bytes", fixBytes);
			newDataTypeAnalyzeResult.add(jsonObject.toJSONString());
		}
		reportCacheMap.put(AnalyzerConstant.DATA_TYPE_ANALYZER + "", newDataTypeAnalyzeResult);
		
		
		Set<String> prefixAnalyzeResult = AppCache.reportCacheMap.get(AnalyzerConstant.PREFIX_ANALYZER + "");
		Set<String> newPrefixAnalyzeResult = new HashSet<>();
		for (String str : prefixAnalyzeResult) {
			JSONObject jsonObject = JSONObject.parseObject(str);
			long bytes = jsonObject.getLong("bytes");
			long fixBytes = new BigDecimal(bytes).multiply(precisionRate).longValue();
			jsonObject.put("bytes", fixBytes);
			newPrefixAnalyzeResult.add(jsonObject.toJSONString());
		}
		reportCacheMap.put(AnalyzerConstant.PREFIX_ANALYZER + "", newPrefixAnalyzeResult);
		
		Set<String> topAnalyzeResult = AppCache.reportCacheMap.get(AnalyzerConstant.TOP_KEY_ANALYZER + "");
		Set<String> newTopAnalyzeResult = new HashSet<>();
		for (String str : topAnalyzeResult) {
			JSONObject jsonObject = JSONObject.parseObject(str);
			long bytes = jsonObject.getLong("bytes");
			long fixBytes = new BigDecimal(bytes).multiply(precisionRate).longValue();
			jsonObject.put("bytes", fixBytes);
			newTopAnalyzeResult.add(jsonObject.toJSONString());
		}
		reportCacheMap.put(AnalyzerConstant.TOP_KEY_ANALYZER + "", newTopAnalyzeResult);

		String report = JSONObject.toJSONString(reportCacheMap);
		return report;
	}
}
