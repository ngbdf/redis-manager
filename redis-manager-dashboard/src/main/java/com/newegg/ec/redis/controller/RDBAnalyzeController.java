package com.newegg.ec.redis.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.cache.AppCache;
import com.newegg.ec.redis.entity.*;
import com.newegg.ec.redis.service.IClusterService;
import com.newegg.ec.redis.service.impl.*;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/rdb")
public class RDBAnalyzeController {

	private static final Logger LOG = LoggerFactory.getLogger(RDBAnalyzeController.class);
	@Autowired
	RdbAnalyzeService rdbAnalyzeService;
	@Autowired
    RdbAnalyzeResultService rdbAnalyzeResultService;

	@Autowired
	ScheduleTaskService taskService;

	@Autowired
	private IClusterService clusterService;

	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	@ResponseBody
	public Result rdbAnalyzeList(){
		return Result.successResult(rdbAnalyzeService.list());
	}

	@RequestMapping(value = { "", "/" }, method = RequestMethod.DELETE)
	@ResponseBody
	public Result deleteAnalyze(@RequestParam("id") String id){
		if(rdbAnalyzeService.deleteRdbAnalyze(Long.parseLong(id))){
			return Result.successResult("delete success");
		}else {
			return Result.successResult("delete faild");
		}

	}

	// update
	@RequestMapping(value = { "", "/" }, method = RequestMethod.PUT)
	@ResponseBody
	public Result updateRdbAnalyze(@RequestBody RDBAnalyze rdbAnalyze) {

		if (rdbAnalyzeService.updateRdbAnalyze(rdbAnalyze)) {
			try {
				taskService.delTask("rdb" + String.valueOf(rdbAnalyze.getId()));
			} catch (SchedulerException e) {
				LOG.error("schedule job delete faild!message:{}", e.getMessage());
			}

			if (rdbAnalyze.isAutoAnalyze()) {
				try {
					taskService.addTask(rdbAnalyze, RdbScheduleJob.class);
				} catch (SchedulerException e) {
					LOG.error("schedule job add faild!message:{}", e.getMessage());
				}
			}
			return Result.successResult("update success!");
		} else {
			return Result.failResult("update fail!");
		}
	}

	// add
	@RequestMapping(value = { "", "/" }, method = RequestMethod.POST)
	public Result addRdbAnalyze(@RequestBody RDBAnalyze rdbAnalyze) {
		if(!rdbAnalyzeService.exitsRdbAnalyze(rdbAnalyze)){
			if (rdbAnalyzeService.add(rdbAnalyze)) {
				if (rdbAnalyze.isAutoAnalyze()) {
					try {
						taskService.addTask(rdbAnalyze, RdbScheduleJob.class);
					} catch (SchedulerException e) {
						LOG.error("schedule job add faild!message:{}", e.getMessage());
					}
				}
				return Result.successResult("add success! ：" + rdbAnalyze.getId());
			} else {
				return Result.failResult("add fail!");
			}
		}else{
			return Result.failResult();
		}

	}

	/**
	 * 取消定时任务
	 * 
	 * @param id
	 */
	@RequestMapping(value = "/cance/{id}", method = RequestMethod.GET)
	public Result canceRdbAnalyze(@PathVariable("id") Long id) {
		try {
			taskService.delTask("rdb" + String.valueOf(id));
		} catch (SchedulerException e) {
			LOG.error("schedule job delete faild!message:{}", e.getMessage());
		}
		return Result.successResult();
	}

	@RequestMapping(value = { "/clusterId/{clusterId}" }, method = RequestMethod.GET)
	public Result getRDBAnalyzeByParentID(@PathVariable Long clusterId) {
		JSONObject data = new JSONObject();
		RDBAnalyze rdbAnalyze = rdbAnalyzeService.getRDBAnalyzeByPid(clusterId);
		Cluster clusterInfo;
		if (null == rdbAnalyze) {
			rdbAnalyze = new RDBAnalyze();
			rdbAnalyze.setClusterId(clusterId);
		}
		if (null != rdbAnalyze) {
			rdbAnalyze.setClusterId(clusterId);
		}
		data.put("info", rdbAnalyze);
		Long id = rdbAnalyzeService.getRedisIDBasePID(clusterId);
		if(id!=null) {
			data.put("status", rdbAnalyzeService.ifRDBAnalyzeIsRunning(id));
		}		
		return Result.successResult(data);
	}

	@GetMapping("/analyze/status/{clusterId}")
	public Result ifRDBAnalyzeIsRunning(@PathVariable Long clusterId) {
		Long id = rdbAnalyzeService.getRedisIDBasePID(clusterId);
		return Result.successResult(rdbAnalyzeService.ifRDBAnalyzeIsRunning(id));

	}

	/**
	 * 根据id下发分析任务到指定的agent 中
	 * 
	 * @param id
	 * @return {message:"",status:true/false }
	 */

//	@RequestMapping(value = "allocation_job/{id}", method = RequestMethod.GET)
//	public Result allocationJob(@PathVariable("id") Long id) {
//		RDBAnalyze rdbAnalyze = rdbAnalyzeService.getRDBAnalyzeById(id);
//		int[] result = null;
//		if (rdbAnalyze.getAnalyzer().contains(",")) {
//			String[] str = rdbAnalyze.getAnalyzer().split(",");
//			result = new int[str.length];
//			for (int i = 0; i < str.length; i++) {
//				result[i] = Integer.parseInt(str[i]);
//
//			}
//		} else {
//			result = new int[1];
//			result[0] = Integer.parseInt(rdbAnalyze.getAnalyzer());
//		}
//		JSONObject responseResult = rdbAnalyzeService.allocationRDBAnalyzeJob(id, result);
//		return Result.successResult(responseResult);
//	}

	@RequestMapping(value = "allocation_job", method = RequestMethod.POST)
	public Result allocationJob(@RequestBody RDBAnalyze rdbAnalyze) {
//	    RDBAnalyzeResult rdbAnalyzeResult = new RDBAnalyzeResult();
//	    rdbAnalyzeResult.setAnalyzeConfig(JSONObject.toJSONString(rdbAnalyze));
//	    rdbAnalyzeResult.setClusterId(Long.parseLong(rdbAnalyze.getCluster().getClusterId().toString()));
//        rdbAnalyzeResultService.add(rdbAnalyzeResult);
		JSONObject responseResult = rdbAnalyzeService.allocationRDBAnalyzeJob(rdbAnalyze);
		return Result.successResult(responseResult);
	}

	/**
	 * 根据ID查询调度进程
	 *
	 */
	@RequestMapping(value = "schedule_detail/{id}", method = RequestMethod.GET)
	public Result scheduleDetail(@PathVariable("id") Long rdbAnalyzeID) {
		RDBAnalyze  rdbAnalyze = rdbAnalyzeService.getRDBAnalyzeByPid(rdbAnalyzeID);
		List<ScheduleDetail> scheduleDetail = AppCache.scheduleDetailMap.get(rdbAnalyze.getId());
		List<ScheduleDetail> result = new ArrayList<ScheduleDetail>();
		if (scheduleDetail != null && scheduleDetail.size() > 0) {
			for (ScheduleDetail scheduleDetails : scheduleDetail) {
				AnalyzeStatus stautStatus = scheduleDetails.getStatus();
				if ("DONE".equalsIgnoreCase(stautStatus.name())) {
					scheduleDetails.setProcess(100);
				} else {
					float ratio = 0;
					if (AppCache.keyCountMap.containsKey(scheduleDetails.getInstance())) {
						float keyCount = AppCache.keyCountMap.get(scheduleDetails.getInstance());
						ratio = scheduleDetails.getProcess() / keyCount;
					}
					scheduleDetails.setProcess((int) (ratio * 100));					
				}

				result.add(scheduleDetails);
			}
		}
		Collections.sort(result, new Comparator<ScheduleDetail>() {
			@Override
			public int compare(ScheduleDetail o1, ScheduleDetail o2) {
				if ("DONE".equals(o1.getStatus().name()) || "DONE".equals(o2.getStatus().name())){
					return o1.getProcess() - o2.getProcess();
				}
				else {
					return o1.getInstance().compareTo(o2.getInstance());
				}
			}
		});
		return Result.successResult(result);
	}

	/**
	 * 取消分析任务
	 * 
	 * @param instance (clusterId)
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
	 */
	@RequestMapping(value = "cance_job/{instance}", method = RequestMethod.GET)
	public Result canceJob(@PathVariable String instance) {
		JSONObject result = rdbAnalyzeService.canceRDBAnalyze(instance);
		return Result.successResult(result);
	}

	/**
	 * get schedule_id list
	 * 
	 * @param clusterId clusterId
	 * @return schedule_id List
	 */
	@GetMapping("/all/schedule_id")
	public Result getAllScheduleId(@RequestParam Long clusterId) {
		try {
			if (null == clusterId) {
				return Result.failResult("clusterId not null!");
			}
//			Map<String, Object> queryMap = new HashMap<>(2);
//			queryMap.put("cluster_id", clusterId);
			List<RDBAnalyzeResult> rdbAnalyzeResultList = rdbAnalyzeResultService.selectAllResultByClusterId(clusterId);
			List<JSONObject> result = new ArrayList<>(500);
			JSONObject obj;
			for (RDBAnalyzeResult rdbAnalyzeResult : rdbAnalyzeResultList) {
				obj = new JSONObject();
				obj.put("value", String.valueOf(rdbAnalyzeResult.getScheduleId()));
				obj.put("label", String.valueOf(rdbAnalyzeResult.getScheduleId()));
				result.add(obj);
			}
			return Result.successResult(result);
		} catch (Exception e) {
			LOG.error("getAllScheduleId failed!", e);
			return Result.failResult("getAllScheduleId failed!");
		}
	}

	/**
	 * get all key_prefix
	 * 
	 * @param clusterId
	 * @param scheduleId
	 * @return
	 */
	@GetMapping("/all/key_prefix")
	public Result getAllKeyPrefix(@RequestParam Long clusterId, @RequestParam(value = "scheduleId", required = false) Long scheduleId) {
		try {
			if (null == clusterId) {
				return Result.failResult("clusterId should not null!");
			}
			RDBAnalyzeResult rdbAnalyzeResult;
			if(null != scheduleId){
				rdbAnalyzeResult = rdbAnalyzeResultService.selectResultByRIDandSID(clusterId, scheduleId);
			} else {
				rdbAnalyzeResult = rdbAnalyzeResultService.selectLatestResultByRID(clusterId);
			}
			if(null == rdbAnalyzeResult) {
				return Result.successResult(null);
			}
			return Result.successResult(rdbAnalyzeResultService.getAllKeyPrefixByResult(rdbAnalyzeResult.getResult()));
		} catch (Exception e) {
			LOG.error("getAllKey_prefix failed!", e);
			return Result.failResult("getAllKey_prefix failed!");
		}
	}

	/**
	 * get cheat data
	 * 
	 * @param type       value:
	 *                   PrefixKeyByCount,PrefixKeyByMemory,DataTypeAnalyze,TTLAnalyze
	 * @param clusterId
	 * @param scheduleId
	 * @return
	 */
	@GetMapping("/chart/{type}")
	public Result getChartDataByType(@PathVariable("type") String type, @RequestParam Long clusterId,
                                           @RequestParam(value = "scheduleId", required = false) Long scheduleId) {
		try {
			return Result.successResult(rdbAnalyzeResultService.getListStringFromResult(clusterId, scheduleId, type));
		} catch (Exception e) {
			LOG.error("getChartDataByType failed!", e);
			return Result.failResult("getChartDataByType failed!");
		}
	}
	/**
	 * get table data
	 *
	 * @param clusterId
	 * @param scheduleId
	 * @return
	 */
	@GetMapping("/table/prefix")
	public Result getPrefixType(@RequestParam Long clusterId, @RequestParam(value = "scheduleId", required = false) Long scheduleId) {
		try {
			return Result.successResult(rdbAnalyzeResultService.getPrefixType(clusterId, scheduleId));
		}
		catch (Exception e) {
			LOG.error("getPrefixType failed!", e);
			return Result.failResult("getPrefixType failed!");
		}
	}


	/**
	 * getTopKey data
	 * 
	 * @param clusterId
	 * @param scheduleId
	 * @param type       0:string 5:hash 10:list 15:set
	 * @return
	 */
	@GetMapping("/top_key")
	public Result getPrefixKeyByMem(@RequestParam Long clusterId, @RequestParam(value = "scheduleId", required = false) Long scheduleId,
                                          @RequestParam Long type) {
		try {
			return Result.successResult(rdbAnalyzeResultService.getTopKeyFromResultByKey(clusterId, scheduleId, type));
		} catch (Exception e) {
			LOG.error("getPrefixKeyByMem failed!", e);
			return Result.failResult("getPrefixKeyByMem failed!");
		}

	}

	/**
	 * 折线图
	 * 
	 * @param type       PrefixKeyByCount,PrefixKeyByMemory
	 * @param clusterId
	 * @param scheduleId
	 * @return
	 */
	@GetMapping("/line/{type}")
	public Result getPerfixLine(@PathVariable("type") String type, @RequestParam Long clusterId,
                                      @RequestParam(value = "scheduleId", required = false) Long scheduleId) {
		try {
			return Result.successResult(rdbAnalyzeResultService.getLineStringFromResult(clusterId, scheduleId, type));
		} catch (Exception e) {
			LOG.error("getPerfixLine failed!", e);
			return Result.failResult("getPerfixLine failed!");
		}
	}

	/**
	 *
	 * @param type PrefixKeyByCount,PrefixKeyByMemory
	 * @param clusterId clusterId
	 * @return JSONArray
	 */
	@GetMapping("/line/prefix/{type}")
	public Result getPrefixLineByCountOrMem(@PathVariable String type, @RequestParam Long clusterId,
											@RequestParam Long scheduleId,
											@RequestParam(value = "prefixKey", required = false) String prefixKey) {
		try {
			JSONArray result = rdbAnalyzeResultService.getPrefixLineByCountOrMem(clusterId, type, 20, prefixKey);
			return Result.successResult(result);
		}
		catch (Exception e) {
			LOG.error("getPrefixLineByCountOrMem failed!", e);
			return Result.failResult("getPrefixLineByCountOrMem failed!");
		}
	}

	/**
	 * 获取所有的分析列表
	 * @return result
	 */
	@GetMapping("/results")
	public Result getAllAnalyzeResults() {
		try {
			List<RDBAnalyzeResult> results = rdbAnalyzeResultService.selectList();
			List<Cluster> clusters = clusterService.getAllClusterList();
			results = rdbAnalyzeResultService.getAllAnalyzeResult(results, clusters);
			return Result.successResult(results);
		} catch (Exception e) {
			LOG.error("get analyze result failed", e);
			return Result.failResult("get analyze result failed!");
		}
	}


}
