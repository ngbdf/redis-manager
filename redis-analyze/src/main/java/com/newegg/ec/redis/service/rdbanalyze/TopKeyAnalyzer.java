package com.newegg.ec.redis.service.rdbanalyze;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.newegg.ec.redis.cache.AppCache;
import com.newegg.ec.redis.constant.AnalyzerConstant;
import com.newegg.ec.redis.entity.RDBAnalyzeInfo;
import com.newegg.ec.redis.service.RDBAnalyzeService;

import com.alibaba.fastjson.JSONObject;

/**
 * @author：Truman.P.Du
 * @createDate: 2018年10月12日 下午1:37:24
 * @version:1.0
 * @description: TopKey 按字节分析器
 */
public class TopKeyAnalyzer extends AbstractAnalyzer {
	private List<TopKey> stringResult = new ArrayList<TopKey>();
	private List<TopKey> listResult = new ArrayList<TopKey>();
	private List<TopKey> setResult = new ArrayList<TopKey>();
	private List<TopKey> mapSetResult = new ArrayList<TopKey>();
	// TopKey数量
	private int topKeyNum = 1000;

	public TopKeyAnalyzer() {
		this.setName("topKey");
	}

	@Override
	public void init(Map<String, String> params) {
		String analyzePort = params.get("port");
		String analyzeTopKeyNum = params.get("topKeyNum");
		if (analyzeTopKeyNum != null) {
			topKeyNum = Integer.parseInt(analyzeTopKeyNum);
		}

		this.setPort(Integer.parseInt(analyzePort));
	}

	@Override
	public void execute(RDBAnalyzeInfo<?> rdbAnalyzeInfo) {

		String type = getDataTypeName(rdbAnalyzeInfo.getKv().getValueRdbType());

		switch (type) {
		case "String":
			TopKey topKey = new TopKey(rdbAnalyzeInfo.getKv().getKey(), type, rdbAnalyzeInfo.getBytesSize(), 1);
			if (stringResult.size() < topKeyNum) {
				stringResult.add(topKey);
				Collections.sort(stringResult);
			} else {
				TopKey minTopKey = stringResult.get(topKeyNum - 1);
				if (topKey.getBytes() > minTopKey.getBytes()) {
					stringResult.remove(topKeyNum - 1);
					stringResult.add(topKey);
					Collections.sort(stringResult);
				}
			}
			break;
		case "List":
			TopKey listTopKey = new TopKey(rdbAnalyzeInfo.getKv().getKey(), type, rdbAnalyzeInfo.getBytesSize(),
					rdbAnalyzeInfo.getKv().getValueAsStringList().size());
			if (listResult.size() < topKeyNum) {
				listResult.add(listTopKey);
				Collections.sort(listResult);
			} else {
				TopKey minTopKey = listResult.get(topKeyNum - 1);
				if (listTopKey.getBytes() > minTopKey.getBytes()) {
					listResult.remove(topKeyNum - 1);
					listResult.add(listTopKey);
					Collections.sort(listResult);
				}
			}
			break;
		case "Set":
			TopKey setTopKey = new TopKey(rdbAnalyzeInfo.getKv().getKey(), type, rdbAnalyzeInfo.getBytesSize(),
					rdbAnalyzeInfo.getKv().getValueAsSet().size());
			if (setResult.size() < topKeyNum) {
				setResult.add(setTopKey);
				Collections.sort(setResult);
			} else {
				TopKey minTopKey = setResult.get(topKeyNum - 1);
				if (setTopKey.getBytes() > minTopKey.getBytes()) {
					setResult.remove(topKeyNum - 1);
					setResult.add(setTopKey);
					Collections.sort(setResult);
				}
			}
			break;
		case "Hash":
			TopKey hashTopKey = new TopKey(rdbAnalyzeInfo.getKv().getKey(), type, rdbAnalyzeInfo.getBytesSize(),
					rdbAnalyzeInfo.getKv().getValueAsHash().size());
			if (mapSetResult.size() < topKeyNum) {
				mapSetResult.add(hashTopKey);
				Collections.sort(mapSetResult);
			} else {
				TopKey minTopKey = mapSetResult.get(topKeyNum - 1);
				if (hashTopKey.getBytes() > minTopKey.getBytes()) {
					mapSetResult.remove(topKeyNum - 1);
					mapSetResult.add(hashTopKey);
					Collections.sort(mapSetResult);
				}
			}
			break;
		}

	}

	@Override
	public void finallyCall() {
		try {
			Set<String> result = new HashSet<>();
			if (stringResult.size() > 0) {
				Set<String> analyzeResult = generateData(stringResult, this.port);
				// this.report2Server(analyzeResult);
				result.addAll(analyzeResult);
				//this.report2ES(analyzeResult);
			}

			if (listResult.size() > 0) {
				Set<String> listResultAnalyzer = generateData(listResult, this.port);
				// this.report2Server(listResultAnalyzer);
				result.addAll(listResultAnalyzer);
				//this.report2ES(listResultAnalyzer);
			}
			if (setResult.size() > 0) {
				Set<String> setResultAnalyzer = generateData(setResult, this.port);
				// this.report2Server(setResultAnalyzer);
				result.addAll(setResultAnalyzer);
				//this.report2ES(setResultAnalyzer);
			}
			if (mapSetResult.size() > 0) {
				Set<String> mapSetResultAnalyzer = generateData(mapSetResult, this.port);
				// this.report2Server(mapSetResultAnalyzer);
				result.addAll(mapSetResultAnalyzer);
				//this.report2ES(mapSetResultAnalyzer);
			}
			if (result.size() > 0) {
				if (AppCache.reportCacheMap.containsKey(AnalyzerConstant.TOP_KEY_ANALYZER + "")) {
					Set<String> oldAnalyzeResult = AppCache.reportCacheMap.get(AnalyzerConstant.TOP_KEY_ANALYZER + "");
					oldAnalyzeResult.addAll(result);
					AppCache.reportCacheMap.put(AnalyzerConstant.TOP_KEY_ANALYZER + "", oldAnalyzeResult);
				} else {
					AppCache.reportCacheMap.put(AnalyzerConstant.TOP_KEY_ANALYZER + "", result);
				}
				result = null;
			}

			stringResult = new ArrayList<TopKey>();
			listResult = new ArrayList<TopKey>();
			setResult = new ArrayList<TopKey>();
			mapSetResult = new ArrayList<TopKey>();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Set<String> generateData(List<TopKey> data, int port) throws Exception {
		Set<String> ret = new HashSet<>();
		JSONObject item = null;
		for (TopKey entry : data) {
			item = new JSONObject();
			item.put("scheduleId", RDBAnalyzeService.getScheduleInfo().getScheduleID());
			item.put("ip", InetAddress.getLocalHost().getHostAddress());
			item.put("port", port);
			item.put("analyzeType", AnalyzerConstant.TOP_KEY_ANALYZER);
			item.put("key", entry.getKey());
			item.put("dataType", entry.getType());
			item.put("bytes", entry.getBytes());
			item.put("itemCount", entry.getItemCount());
			ret.add(item.toJSONString());
		}
		return ret;
	}

}

class TopKey implements Comparable<TopKey> {
	private String key;
	private String type;
	private Long bytes;
	private Integer itemCount;

	public TopKey(String key, String type, Long bytes, Integer itemCount) {
		super();
		this.key = key;
		this.type = type;
		this.bytes = bytes;
		this.itemCount = itemCount;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getBytes() {
		return bytes;
	}

	public void setBytes(Long bytes) {
		this.bytes = bytes;
	}

	public Integer getItemCount() {
		return itemCount;
	}

	public void setItemCount(Integer itemCount) {
		this.itemCount = itemCount;
	}

	@Override
	public String toString() {
		return "TypeSizeICount [bytes=" + bytes + ", itemCount=" + itemCount + "]";
	}

	@Override
	public int compareTo(TopKey o) {
		return (int) (o.bytes - this.bytes);
	}

}
