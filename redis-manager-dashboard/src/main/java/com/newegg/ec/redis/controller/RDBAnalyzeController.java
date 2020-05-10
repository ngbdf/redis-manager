package com.newegg.ec.redis.controller;

import com.alibaba.fastjson.JSONObject;

import com.newegg.ec.redis.entity.*;
import com.newegg.ec.redis.plugin.rct.cache.AppCache;
import com.newegg.ec.redis.schedule.RDBScheduleJob;
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
	public Result rdbAnalyzeList(@RequestParam("groupId") String groupId){
		return Result.successResult(rdbAnalyzeService.list(Long.parseLong(groupId)));
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
		try{
			taskService.delTask("rdb" + rdbAnalyze.getId());
			if(rdbAnalyze.isAutoAnalyze()){
				if(taskService.vaildCronExpress(rdbAnalyze.getSchedule())){
					if(rdbAnalyzeService.updateRdbAnalyze(rdbAnalyze)){
						try {
							taskService.addTask(rdbAnalyze, RDBScheduleJob.class);
							return Result.successResult("update success!");
						} catch (SchedulerException e) {
							LOG.error("schedule job update faild!message:{}", e.getMessage());
							return Result.failResult("schedule job update faild!");
						}
					}else {
						return Result.failResult("update data faild!");
					}
				}else{
					return Result.failResult("cron expression has error,please check!");
				}
			}else{
				if(rdbAnalyzeService.updateRdbAnalyze(rdbAnalyze)){
					return Result.successResult("update success!");
				}else{
					return Result.failResult("update data faild!");
				}
			}
		}catch (SchedulerException e){
			LOG.error("schedule job delete faild!message:{}", e.getMessage());
			return Result.failResult("schedule job delete faild!");
		}

	}

	// add
	@RequestMapping(value = { "", "/" }, method = RequestMethod.POST)
	public Result addRdbAnalyze(@RequestBody RDBAnalyze rdbAnalyze) {
	    if(!rdbAnalyzeService.exitsRdbAnalyze(rdbAnalyze)){
	        if(rdbAnalyze.isAutoAnalyze()){
	            if(taskService.vaildCronExpress(rdbAnalyze.getSchedule())){
                    return addAnalyze(rdbAnalyze);
                }else{
                    return Result.failResult("cron expression has error,please check!");
                }
            }else{
               return addAnalyze(rdbAnalyze);
            }
        }else{
            return Result.failResult("add Job faild,job exits!");
        }


	}

	private Result addAnalyze(RDBAnalyze rdbAnalyze){
        if(rdbAnalyzeService.add(rdbAnalyze)){
            if(rdbAnalyze.isAutoAnalyze()){
                try{
                    taskService.addTask(rdbAnalyze, RDBScheduleJob.class);
                 //   return Result.successResult("add success! ：" + rdbAnalyze.getId());
                }catch (SchedulerException e){
                    LOG.error("schedule job add faild!message:{}", e.getMessage());
                    return Result.failResult("schedule job add faild!");
                }
            }
            return Result.successResult("add success!" );
        }else {
            return Result.failResult("add job to database has error,please check!" );
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
			taskService.delTask("rdb" + id);
		} catch (SchedulerException e) {
			LOG.error("schedule job delete faild!message:{}", e.getMessage());
		}
		return Result.successResult("cancel job success!");
	}

	@RequestMapping(value = { "/clusterId/{clusterId}" }, method = RequestMethod.GET)
	public Result getRDBAnalyzeByParentID(@PathVariable Long clusterId) {
		JSONObject data = new JSONObject();
		RDBAnalyze rdbAnalyze = rdbAnalyzeService.getRDBAnalyzeByPid(clusterId);
		if (null == rdbAnalyze) {
			rdbAnalyze = new RDBAnalyze();
		}
		rdbAnalyze.setClusterId(clusterId);
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


	@RequestMapping(value = "allocation_job", method = RequestMethod.POST)
	public Result allocationJob(@RequestBody RDBAnalyze rdbAnalyze) {
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
		List<ScheduleDetail> result = new ArrayList<>();
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
		result.sort((o1, o2) -> {
			if ("DONE".equals(o1.getStatus().name()) || "DONE".equals(o2.getStatus().name())) {
				return o1.getProcess() - o2.getProcess();
			} else {
				return o1.getInstance().compareTo(o2.getInstance());
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
	@RequestMapping(value = "cance_job/{instance}/{scheduleID}", method = RequestMethod.GET)
	public Result canceJob(@PathVariable String instance,@PathVariable String scheduleID) {
		JSONObject result = rdbAnalyzeService.canceRDBAnalyze(instance,scheduleID);
		return Result.successResult(result);
	}

	/**
	 * get schedule_id list
	 * 
	 * @param analyzeResultId analyzeResultId
	 * @return schedule_id List
	 */
	@GetMapping("/all/schedule_id")
	public Result getAllScheduleId(@RequestParam Long analyzeResultId) {
		try {
			if (null == analyzeResultId) {
				return Result.failResult("analyzeResultId not null!");
			}
			List<RDBAnalyzeResult> rdbAnalyzeResultList = rdbAnalyzeResultService.selectAllRecentlyResultById(analyzeResultId);
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
	 * @param analyzeResultId
	 * @return
	 */
	@GetMapping("/all/key_prefix")
	public Result getAllKeyPrefix(@RequestParam Long analyzeResultId) {
		try {
			if (null == analyzeResultId) {
				return Result.failResult("analyzeResultId should not null!");
			}
			return Result.successResult(rdbAnalyzeResultService.getAllKeyPrefixById(analyzeResultId));
		} catch (Exception e) {
			LOG.error("getAllKey_prefix failed!", e);
			return Result.failResult("getAllKey_prefix failed!");
		}
	}


	/**
	 * get cheat data
	 * @param type       value:
	 *                   PrefixKeyByCount,PrefixKeyByMemory,DataTypeAnalyze,TTLAnalyze
	 * @param analyzeResultId
	 * @return
	 */
	@GetMapping("/chart/{type}")
	public Result getChartDataByType(@PathVariable("type") String type, @RequestParam Long analyzeResultId) {
		try {
			return Result.successResult(rdbAnalyzeResultService.getListStringFromResult(analyzeResultId, type));
		} catch (Exception e) {
			LOG.error("getChartDataByType failed!", e);
			return Result.failResult("getChartDataByType failed!");
		}
	}
	/**
	 * get table data
	 *
	 * @param analyzeResultId
	 * @return
	 */
	@GetMapping("/table/prefix")
	public Result getPrefixType(@RequestParam Long analyzeResultId) {
		try {
			return Result.successResult(rdbAnalyzeResultService.getPrefixType(analyzeResultId));
		}
		catch (Exception e) {
			LOG.error("getPrefixType failed!", e);
			return Result.failResult("getPrefixType failed!");
		}
	}


	/**
	 * getTopKey data
	 * 
	 * @param analyzeResultId
	 * @param type       0:string 5:hash 10:list 15:set
	 * @return
	 */
	@GetMapping("/top_key")
	public Result getPrefixKeyByMem(@RequestParam Long analyzeResultId,
                                          @RequestParam Long type) {
		try {
			return Result.successResult(rdbAnalyzeResultService.getTopKeyFromResultByKey(analyzeResultId, type));
		} catch (Exception e) {
			LOG.error("getPrefixKeyByMem failed!", e);
			return Result.failResult("getPrefixKeyByMem failed!");
		}

	}

//	/**
//	 * 折线图
//	 *
//	 * @param type       PrefixKeyByCount,PrefixKeyByMemory
//	 * @param clusterId
//	 * @param scheduleId
//	 * @return
//	 */
//	@GetMapping("/line/{type}")
//	public Result getPerfixLine(@PathVariable("type") String type, @RequestParam Long clusterId,
//                                      @RequestParam(value = "scheduleId", required = false) Long scheduleId) {
//		try {
//			return Result.successResult(rdbAnalyzeResultService.getLineStringFromResult(clusterId, scheduleId, type));
//		} catch (Exception e) {
//			LOG.error("getPerfixLine failed!", e);
//			return Result.failResult("getPerfixLine failed!");
//		}
//	}

	/**
	 *
	 * @param type PrefixKeyByCount,PrefixKeyByMemory
	 * @param analyzeResultId analyzeResultId
	 * @return JSONArray
	 */
	@GetMapping("/line/prefix/{type}")
	public Result getPrefixLineByCountOrMem(@PathVariable String type, @RequestParam Long analyzeResultId,
											@RequestParam(value = "prefixKey", required = false) String prefixKey,
											@RequestParam(value = "top", required = false) Integer top) {
		try {
			if (null == top || top == 0) {
				top = 10;
			}
			JSONObject result = rdbAnalyzeResultService.getPrefixLineByCountOrMem(analyzeResultId, type, top, prefixKey);
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
	public Result getAllAnalyzeResults(@RequestParam String groupId) {
		try {
			Long group_id = Long.parseLong(groupId);
			List<RDBAnalyzeResult> results = rdbAnalyzeResultService.selectList(group_id);
		//	List<Cluster> clusters = clusterService.getAllClusterList();
		//	results = rdbAnalyzeResultService.getAllAnalyzeResult(results, clusters);
			return Result.successResult(results);
		} catch (Exception e) {
			LOG.error("get analyze result failed", e);
			return Result.failResult("get analyze result failed!");
		}
	}


	/**
	 * 测试corn表达式
	 *
	 * @param body
	 * @return
	 */
	@RequestMapping(value = "test_corn", method = RequestMethod.POST)
	@ResponseBody
	public Result testCorn(@RequestBody Map<String, String> body) {
		String schedule = body.get("schedule");
		List<String> list = taskService.getRecentTriggerTime(schedule);
		return Result.successResult(list);
	}



}
