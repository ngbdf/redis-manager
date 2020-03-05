package com.newegg.ec.redis.controller;


import com.newegg.ec.redis.Analyzer;
import com.newegg.ec.redis.cache.AppCache;
import com.newegg.ec.redis.entity.AnalyzeStatus;
import com.newegg.ec.redis.entity.ScheduleInfo;
import com.newegg.ec.redis.service.RDBAnalyzeService;
import com.newegg.ec.redis.service.ReportService;
import com.newegg.ec.redis.worker.AnalyzerWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

/**
 * @author：Truman.P.Du
 * @createDate: 2018年3月28日 上午8:25:32
 * @version:1.0
 * @description:
 */
@RestController
@RequestMapping("/")
public class RDBAnalyzeController {

	@Autowired
	RDBAnalyzeService rdbAnalyzeService;
	@Autowired
	ReportService reportService;

	/**
	 * 初始化配置信息并进行检查
	 * 
	 * @param scheduleInfo
	 * @param request
	 * @return JSON string: </br>
	 *         <code>
	 *            check passed:
	 *            {
	 *                "checked": true
	 *            }
	 *            check not passed:
	 *            {
	 *                "checked": false,
	 *                "message": {
	 *                    "port1": "xxx/dump.rdb NOT EXIST!",
	 *                    ...
	 *                }
	 *            }
	 *         </code>
	 */
	@RequestMapping(value = "/receivedSchedule", method = RequestMethod.POST)
	public String init(@RequestBody ScheduleInfo scheduleInfo) {
		JSONObject ret = new JSONObject();
		if (AnalyzeStatus.RUNNING.equals(RDBAnalyzeService.getStatus())) {
			ret.put("checked", Boolean.FALSE);
			ret.put("message", "This Analyzer is already initialized or running!");
		} else {
			this.rdbAnalyzeService.setScheduleInfo(scheduleInfo);
			ret = this.rdbAnalyzeService.doCheck();
			if (ret.getBoolean("checked")) {
				AppCache.reportCacheMap.clear();
				this.rdbAnalyzeService.execute();
			}
		}
		ret.put("status", RDBAnalyzeService.getStatus());
		return ret.toJSONString();
	}


	/**
	 * 获取当前节点状态
	 * 
	 * @param clusterId
	 * @return JSON string: </br>
	 *         <code>
	 *            {
	 *                "scheduleInfo": {
	 *                    ...
	 *                },
	 *                "rdbAnalyzeStatus": {
	 *                    "port1": "DONE",
	 *                    "port2": "RUNNING",
	 *                    "port1": "NOT_START",
	 *                    ...
	 *                },
	 *                "ip": ""
	 *            }
	 *         </code>
	 */
	@RequestMapping(value = "/status", method = RequestMethod.GET)
	@ResponseBody
	public String analyzeStatus() {
		if (AnalyzeStatus.NOTINIT.equals(RDBAnalyzeService.getStatus())) {
			JSONObject ret = new JSONObject();
			ret.put("scheduleInfo", "");
			ret.put("rdbAnalyzeStatus", "");
			ret.put("message", "This Analyzer is not initialized yet!");
			ret.put("currentCacheSize", AnalyzerWorker.cache.size());
			ret.put("cacheSize", Analyzer.MAX_QUEUE_SIZE);
			return ret.toJSONString();
		}
		return this.rdbAnalyzeService.status().toJSONString();
	}

	/**
	 * 终止取消当前执行的分析任务
	 * 
	 * @return JSON string: </br>
	 *         <code>
	 *         cancel succeed:
	 *            {
	 *                "canceled": true,
	 *                "lastStatus": "[AnalyzeStatus]",
	 *                "currentStatus": "[AnalyzeStatus]"
	 *            }
	 *         cancel failed:
	 *            {
	 *                "canceled": false,
	 *                "lastStatus": "[AnalyzeStatus]",
	 *                "currentStatus": "[AnalyzeStatus]"
	 *            }
	 *         </code>
	 * 
	 *         {@link AnalyzeStatus}
	 */
	@RequestMapping(value = "/cancel", method = RequestMethod.GET)
	@ResponseBody
	public String cancel() {
		JSONObject ret = new JSONObject();
		ret.put("lastStatus", RDBAnalyzeService.getStatus());
		this.rdbAnalyzeService.cancel();
		ret.put("canceled", AnalyzeStatus.CANCELED.equals(RDBAnalyzeService.getStatus()));
		ret.put("currentStatus", RDBAnalyzeService.getStatus());
		AppCache.reportCacheMap.clear();
		return ret.toJSONString();
	}
	
	@RequestMapping(value="/report",method = RequestMethod.GET)
	public String report() {
		String report = reportService.fixReportData();
		return report;
	}

	/**
	 * 动态改变属性值
	 * 
	 * 目前接受更改的属性值有： {@link Analyzer#FILTER_Bytes} {@link Analyzer#FILTER_ItemCount}
	 * 
	 * <code>
	 * {
	 *    "filterBytes": 1024,
	 *    "filterItemCount": 10,
	 *    "useCustomAlgo": boolean,
	 *    "includeNoMatchPrefixKey": boolean,
	 *    "maxEsSenderThreadNum": 2,
	 *    "maxQueueSize": 1000,
	 *    "maxMapSize": 1000
	 * }
	 * </code>
	 * 
	 * @param json
	 * @return
	 */
	@RequestMapping(value = "/properties", method = RequestMethod.POST)
	@ResponseBody
	public String dynamicSetting(@RequestBody JSONObject json) {
		if (json.containsKey("filterBytes")) {
			Analyzer.FILTER_Bytes = json.getLong("filterBytes");
		}
		if (json.containsKey("filterItemCount")) {
			Analyzer.FILTER_ItemCount = json.getInteger("filterItemCount");
		}
		if (json.containsKey("useCustomAlgo")) {
			Analyzer.USE_Custom_Algo = json.getBoolean("useCustomAlgo");
		}
		if (json.containsKey("includeNoMatchPrefixKey")) {
			Analyzer.INCLUDE_NoMatchPrefixKey = json.getBoolean("includeNoMatchPrefixKey");
		}
		if (json.containsKey("maxEsSenderThreadNum")) {
			Analyzer.MAX_EsSenderThreadNum = json.getInteger("maxEsSenderThreadNum");
			AnalyzerWorker.workerNumChanged();
		}
		if (json.containsKey("maxQueueSize")) {
			Analyzer.MAX_QUEUE_SIZE = json.getInteger("maxQueueSize");
		}

		return this.settingObj().toJSONString();
	}

	@RequestMapping(value = "/properties", method = RequestMethod.GET)
	@ResponseBody
	public String dynamicSetting() {
		return this.settingObj().toJSONString();
	}
	
	@RequestMapping(value = "/clear", method = RequestMethod.GET)	
	public void clear() {
		AppCache.reportCacheMap.clear();
	}

	private JSONObject settingObj() {
		JSONObject ret = new JSONObject();
		ret.put("filterBytes", Analyzer.FILTER_Bytes);
		ret.put("filterItemCount", Analyzer.FILTER_ItemCount);
		ret.put("useCustomAlgo", Analyzer.USE_Custom_Algo);
		ret.put("includeNoMatchPrefixKey", Analyzer.INCLUDE_NoMatchPrefixKey);
		ret.put("maxEsSenderThreadNum", Analyzer.MAX_EsSenderThreadNum);
		ret.put("maxQueueSize", Analyzer.MAX_QUEUE_SIZE);
		return ret;
	}
}
