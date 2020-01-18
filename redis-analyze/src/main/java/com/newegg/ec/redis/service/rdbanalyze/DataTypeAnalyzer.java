package com.newegg.ec.redis.service.rdbanalyze;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.newegg.ec.redis.cache.AppCache;
import com.newegg.ec.redis.constant.AnalyzerConstant;
import com.newegg.ec.redis.entity.RDBAnalyzeInfo;
import com.newegg.ec.redis.service.RDBAnalyzeService;

/**
 * @author：Truman.P.Du
 * @createDate: 2018年10月12日 下午1:34:23
 * @version:1.0
 * @description:按数据类型分析器
 */
public class DataTypeAnalyzer extends AbstractAnalyzer {

	private Map<String, SizeCount> result = null;

	public DataTypeAnalyzer() {
		this.result = new HashMap<String, SizeCount>();
		this.setName("dataType");
	}

	@Override
	public void init(Map<String, String> params) {
		String analyzePort = params.get("port");
		this.setPort(Integer.parseInt(analyzePort));

	}

	@Override
	public void execute(RDBAnalyzeInfo<?> rdbAnalyzeInfo) {

		KeyValuePair<?> theKV = rdbAnalyzeInfo.getKv();
		String dataType = getDataTypeName(theKV.getValueRdbType());
		if (null == dataType) {
			return;
		}
		SizeCount sizeCount = null;
		if (this.result.containsKey(dataType)) {
			sizeCount = this.result.get(dataType);
			sizeCount.setCount(sizeCount.getCount() + 1);
			sizeCount.setSize(sizeCount.getSize() + rdbAnalyzeInfo.getBytesSize());
		} else {
			sizeCount = new SizeCount();
			sizeCount.setCount(1);
			sizeCount.setSize(rdbAnalyzeInfo.getBytesSize());
		}
		this.result.put(dataType, sizeCount);
	}

	@Override
	public void finallyCall() {
		try {
			if (result.size() <= 0) {
				return;
			}
			Set<String> analyzeResult = generateDataTypeResult(this.port);
			if (AppCache.reportCacheMap.containsKey(AnalyzerConstant.DATA_TYPE_ANALYZER + "")) {
				Set<String> oldAnalyzeResult = AppCache.reportCacheMap.get(AnalyzerConstant.DATA_TYPE_ANALYZER + "");
				oldAnalyzeResult.addAll(analyzeResult);
				AppCache.reportCacheMap.put(AnalyzerConstant.DATA_TYPE_ANALYZER + "", oldAnalyzeResult);
			} else {
				AppCache.reportCacheMap.put(AnalyzerConstant.DATA_TYPE_ANALYZER + "", analyzeResult);
			}

			//this.report2ES(analyzeResult);
			result = new HashMap<String, SizeCount>();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Set<String> generateDataTypeResult(int port) throws Exception {
		Set<String> ret = new HashSet<>();
		JSONObject item = null;
		String ip = InetAddress.getLocalHost().getHostAddress();
		for (Entry<String, SizeCount> entry : this.result.entrySet()) {
			item = new JSONObject();
			item.put("scheduleId", RDBAnalyzeService.getScheduleInfo().getScheduleID());
			item.put("ip", ip);
			item.put("port", port);
			item.put("dataType", entry.getKey());
			item.put("bytes", entry.getValue().getSize());
			item.put("itemCount", entry.getValue().getCount());
			item.put("analyzeType", AnalyzerConstant.DATA_TYPE_ANALYZER);
			ret.add(item.toJSONString());
		}
		return ret;
	}

}

class SizeCount {
	private Long size;
	private Integer count;

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "SizeItemCount1 [size=" + size + ", count=" + count + "]";
	}

}
