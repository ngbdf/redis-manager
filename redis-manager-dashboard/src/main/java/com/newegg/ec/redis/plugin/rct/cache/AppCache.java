package com.newegg.ec.redis.plugin.rct.cache;

import com.newegg.ec.redis.entity.AnalyzeStatus;
import com.newegg.ec.redis.entity.RDBAnalyze;
import com.newegg.ec.redis.entity.ScheduleDetail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * @author：Truman.P.Du
 * @createDate: 2018年10月19日 下午1:29:56
 * @version:1.0
 * @description: 应用静态缓存
 */
public class AppCache {
	// <rdbAnalyzeID,scheduleID>
	public static Map<Long, Long> scheduleProcess = new HashMap<>();
	// <rdbAnalyzeID,List<ScheduleDetail>>
	public static Map<Long, List<ScheduleDetail>> scheduleDetailMap = new ConcurrentHashMap<>();
	public static Map<String, Float> keyCountMap = new ConcurrentHashMap<>();
	
	public static Map<String, Map<ScheduledFuture<?>,String>> taskList = new ConcurrentHashMap<String, Map<ScheduledFuture<?>,String>>();
	/**
	 * 根据rdbAnalyzeID判断当前集群分析任务是否完成
	 * 
	 * @param rdbAnalyzeID
	 * @return
	 */
	public static boolean isAnalyzeSuccess(Long rdbAnalyzeID) {
		List<ScheduleDetail> scheduleDetails = scheduleDetailMap.get(rdbAnalyzeID);
		if (scheduleDetails != null && scheduleDetails.size() > 0) {
			boolean status = true;
			for (ScheduleDetail scheduleDetail : scheduleDetails) {
				if (!AnalyzeStatus.DONE.equals(scheduleDetail.getStatus())) {
					status = false;
					break;
				}
			}
			return status;
		}
		return true;
	}

	/**
	 * 根据ID判断是否分析完成
	 * 
	 * @param RDBAnalyze
	 *            rdbAnalyze
	 * @return
	 */
	public static boolean isAnalyzeComplete(RDBAnalyze rdbAnalyze) {
		List<ScheduleDetail> scheduleDetails = scheduleDetailMap.get(rdbAnalyze.getId());
		if (scheduleDetails != null && scheduleDetails.size() > 0) {
			boolean status = true;
			for (ScheduleDetail scheduleDetail : scheduleDetails) {
				if (!AnalyzeStatus.DONE.equals(scheduleDetail.getStatus())) {
					status = false;
					break;
				}
			}
			return status;
		}
		return false;
	}

	/**
	 * 根据rdbAnalyzeID判断是否需要继续获取分析器执行状态
	 * 
	 * @param rdbAnalyzeID
	 * @return
	 */
	public static boolean isNeedAnalyzeStastus(Long rdbAnalyzeID) {
		List<ScheduleDetail> scheduleDetails = scheduleDetailMap.get(rdbAnalyzeID);
		if (scheduleDetails != null && scheduleDetails.size() > 0) {
			boolean status = false;
			for (ScheduleDetail scheduleDetail : scheduleDetails) {
				if (!(AnalyzeStatus.DONE.equals(scheduleDetail.getStatus())
						|| AnalyzeStatus.CANCELED.equals(scheduleDetail.getStatus()))) {
					status = true;
					break;
				}
			}
			return status;
		}
		return false;
	}

	public static Long getRdbAnalyzeIDByScheduleID(Long scheduleID) {
		for (Map.Entry<Long, Long> entry : scheduleProcess.entrySet()) {
			if (entry.getValue().equals(scheduleID)) {
				return entry.getKey();
			}
		}
		return null;
	}

}
