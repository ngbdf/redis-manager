package com.newegg.ec.redis.service.rdbanalyze;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.newegg.ec.redis.Analyzer;
import com.newegg.ec.redis.cache.AppCache;
import com.newegg.ec.redis.constant.AnalyzerConstant;
import com.newegg.ec.redis.entity.RDBAnalyzeInfo;
import com.newegg.ec.redis.service.RDBAnalyzeService;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;

/**
 * @author：Truman.P.Du
 * @createDate: 2018年10月12日 下午1:33:17
 * @version:1.0
 * @description: 前缀字节分析器
 */
public class PrefixAnalyzer extends AbstractAnalyzer {

	private Map<String, PrefixSizeCount> result = null;
	private String customPrefix;
	private String[] keyPrefixSeparatorArray = { ":", "|", "-", "_" };
	private boolean isLastIndexOf = true;
	private final static Pattern PATTERN = Pattern.compile("\\{\\w+\\}");

	public PrefixAnalyzer() {
		String keyPrefixIndexLocation = Analyzer.KEY_PREFIX_INDEX_LOCATION;
		if ("last".equalsIgnoreCase(keyPrefixIndexLocation)) {
			this.isLastIndexOf = true;
		} else {
			this.isLastIndexOf = false;
		}
		String keyPrefixSeparators = Analyzer.KEY_PREFIX_SEPARATORS;
		if (StringUtils.isNotBlank(keyPrefixSeparators)) {
			this.keyPrefixSeparatorArray = keyPrefixSeparators.split(",");
		}

		this.result = new HashMap<String, PrefixSizeCount>();
		this.setName("prefix");
	}

	@Override
	public void init(Map<String, String> params) {
		String analyzePort = params.get("port");
		customPrefix = params.get("customPrefix");
		this.setPort(Integer.parseInt(analyzePort));
	}

	@Override
	public void execute(RDBAnalyzeInfo<?> rdbAnalyzeInfo) {

		KeyValuePair<?> kv = rdbAnalyzeInfo.getKv();
		if (this.result == null) {
			this.result = new HashMap<String, PrefixSizeCount>();
		}
		KeyValuePair<?> theKV = kv;
		String key = theKV.getKey();
		String prefix = key;
		boolean flag = true;
		// Prefix 规则
		// 1. 配置指定的
		if (customPrefix != null) {
			String[] prefixes = this.customPrefix.trim().split(",");
			for (int i = 0, length = prefixes.length; i < length; i++) {
				if (flag && prefixes[i].length() > 0 && key.lastIndexOf(prefixes[i]) != -1) {
					prefix = prefixes[i] + "*";
					flag = false;
				}
			}
		}

		// 2. {}
		Matcher matcher = PATTERN.matcher(key);
		if (flag && matcher.find()) {
			prefix = matcher.group(0) + "*";
			flag = false;
		}
		// 自定义分隔符，默认分隔符":", "|", "_", "-"
		if (flag) {
			if (isLastIndexOf) {
				for (int i = 0; i < keyPrefixSeparatorArray.length; i++) {
					if (key.lastIndexOf(keyPrefixSeparatorArray[i]) != -1) {
						prefix = key.substring(0, key.lastIndexOf(keyPrefixSeparatorArray[i])+1) + "*";
						flag = false;
						break;
					}
				}
			} else {
				for (int i = 0; i < keyPrefixSeparatorArray.length; i++) {
					if (key.indexOf(keyPrefixSeparatorArray[i]) != -1) {
						prefix = key.substring(0, key.indexOf(keyPrefixSeparatorArray[i])+1) + "*";
						flag = false;
						break;
					}
				}
			}
		}

		if (!flag) {
			appendResult(prefix, rdbAnalyzeInfo);
		}
	}

	@Override
	public void finallyCall() {
		try {
			if (result.size() <= 0) {
				return;
			}
			Set<String> analyzeResult = generatePrefixResult(this.port);

			if (AppCache.reportCacheMap.containsKey(AnalyzerConstant.PREFIX_ANALYZER + "")) {
				Set<String> oldAnalyzeResult = AppCache.reportCacheMap.get(AnalyzerConstant.PREFIX_ANALYZER + "");
				oldAnalyzeResult.addAll(analyzeResult);
				AppCache.reportCacheMap.put(AnalyzerConstant.PREFIX_ANALYZER + "", oldAnalyzeResult);
			} else {
				AppCache.reportCacheMap.put(AnalyzerConstant.PREFIX_ANALYZER + "", analyzeResult);
			}

			// this.report2ES(analyzeResult);
			result = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Set<String> generatePrefixResult(int port) throws Exception {
		Set<String> ret = new HashSet<>();
		JSONObject item = null;
		String ip = InetAddress.getLocalHost().getHostAddress();
		for (Entry<String, PrefixSizeCount> entry : this.result.entrySet()) {
			item = new JSONObject();
			item.put("scheduleId", RDBAnalyzeService.getScheduleInfo().getScheduleID());
			item.put("ip", ip);
			item.put("port", port);
			item.put("prefix", entry.getKey());
			item.put("bytes", entry.getValue().getBytes());
			item.put("itemCount", entry.getValue().getCount());
			item.put("analyzeType", AnalyzerConstant.PREFIX_ANALYZER);
			ret.add(item.toJSONString());
		}
		return ret;
	}

	private void appendResult(String prefix, RDBAnalyzeInfo<?> rdbAnalyzeInfo) {
		PrefixSizeCount prefixSizeCount = null;
		if (this.result.containsKey(prefix)) {
			prefixSizeCount = this.result.get(prefix);
			prefixSizeCount.setCount(prefixSizeCount.getCount() + 1L);
			prefixSizeCount.setBytes(prefixSizeCount.getBytes() + rdbAnalyzeInfo.getBytesSize());
		} else {
			prefixSizeCount = new PrefixSizeCount();
			prefixSizeCount.setCount(1L);
			prefixSizeCount.setBytes(rdbAnalyzeInfo.getBytesSize());
		}
		this.result.put(prefix, prefixSizeCount);
	}
}

class PrefixSizeCount {
	private Long bytes;
	private Long count;

	public Long getBytes() {
		return bytes;
	}

	public void setBytes(Long bytes) {
		this.bytes = bytes;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "TypeSizeCount [bytes=" + bytes + ", count=" + count + "]";
	}

}
