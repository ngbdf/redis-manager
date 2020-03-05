package com.newegg.ec.redis.service.rdbanalyze;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.newegg.ec.redis.Analyzer;
import com.newegg.ec.redis.constant.AnalyzerConstant;
import com.newegg.ec.redis.entity.RDBAnalyzeInfo;
import com.newegg.ec.redis.service.RDBAnalyzeService;

import com.alibaba.fastjson.JSONObject;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;

/**
 * @author：Truman.P.Du
 * @createDate: 2018年10月12日 下午1:41:26
 * @version:1.0
 * @description:按过滤器导出相应的key到ES分析器
 */
public class ExportKeyByFilterAnalyzer extends AbstractAnalyzer {
	private String customPrefix;
	private final static Pattern PATTERN = Pattern.compile("\\{\\w+\\}");

	public ExportKeyByFilterAnalyzer() {
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
		KeyValuePair<?> theKV = rdbAnalyzeInfo.getKv();
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
		// 3. :
		if (flag && key.lastIndexOf(":") != -1) {
			prefix = key.substring(0, key.lastIndexOf(":") + 1) + "*";
			flag = false;
		}
		// 4. |
		if (flag && key.lastIndexOf("|") != -1) {
			prefix = key.substring(0, key.lastIndexOf("|") + 1) + "*";
			flag = false;
		}
		// 5. _
		if (flag && key.lastIndexOf("_") != -1) {
			prefix = key.substring(0, key.lastIndexOf("_") + 1) + "*";
			flag = false;
		}
		// 6. -
		if (flag && key.lastIndexOf("-") != -1) {
			prefix = key.substring(0, key.lastIndexOf("-") + 1) + "*";
			flag = false;
		}
		// no matches, 直接使用完整key
		// 对数据进行筛除
		if (flag && !Analyzer.INCLUDE_NoMatchPrefixKey) {
			return;
		}
		if (!flag && keepItOrNot(theKV)) {
			try {
				String message = this.generateResult(prefix, rdbAnalyzeInfo);
				this.async2ES(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private String generateResult(String prefix, RDBAnalyzeInfo<?> rdbAnalyzeInfo) throws Exception {
		JSONObject item = new JSONObject();
		KeyValuePair<?> kv = rdbAnalyzeInfo.getKv();
		item.put("scheduleId", RDBAnalyzeService.getScheduleInfo().getScheduleID());
		item.put("bytes", rdbAnalyzeInfo.getBytesSize());
		item.put("itemCount", this.calcuItemCount(kv));
		item.put("key", kv.getKey());
		item.put("prefix", prefix);
		item.put("time", System.currentTimeMillis());
		item.put("analyzeType", AnalyzerConstant.EXPORT_KEY_BY_FILTER_ANALYZER);
		
		return item.toJSONString();
	}

}
