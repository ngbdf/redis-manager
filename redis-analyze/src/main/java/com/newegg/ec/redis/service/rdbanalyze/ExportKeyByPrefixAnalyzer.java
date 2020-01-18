package com.newegg.ec.redis.service.rdbanalyze;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.newegg.ec.redis.constant.AnalyzerConstant;
import com.newegg.ec.redis.entity.RDBAnalyzeInfo;
import com.newegg.ec.redis.service.RDBAnalyzeService;

/**
 * @author：Truman.P.Du
 * @createDate: 2018年10月12日 下午1:40:05
 * @version:1.0
 * @description:按前缀导出相应的key到ES分析器
 */
public class ExportKeyByPrefixAnalyzer extends AbstractAnalyzer {

	private String customPrefix;
	Set<String> batchData = new HashSet<>();

	public ExportKeyByPrefixAnalyzer() {
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

		if (!flag) {
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
		item.put("bytes",rdbAnalyzeInfo.getBytesSize());
		item.put("itemCount", this.calcuItemCount(kv));
		item.put("key", kv.getKey());
		item.put("time", System.currentTimeMillis());
		item.put("analyzeType", AnalyzerConstant.EXPORT_KEY_BY_PREFIX_ANALYZER);
		return item.toJSONString();
	}

}
