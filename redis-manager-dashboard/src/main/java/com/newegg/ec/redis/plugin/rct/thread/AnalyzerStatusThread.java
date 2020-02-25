package com.newegg.ec.redis.plugin.rct.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.newegg.ec.redis.config.RCTConfig;
import com.newegg.ec.redis.entity.*;

import com.newegg.ec.redis.plugin.rct.cache.AppCache;
import com.newegg.ec.redis.plugin.rct.report.EmailSendReport;
import com.newegg.ec.redis.service.impl.RdbAnalyzeResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author：Truman.P.Du
 * @createDate: 2018年10月19日 下午1:48:52
 * @version:1.0
 * @description:
 */
public class AnalyzerStatusThread implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(AnalyzerStatusThread.class);
	private List<ScheduleDetail> scheduleDetails = new ArrayList<>();
	private List<AnalyzeInstance> analyzeInstances = new ArrayList<>();
	private RestTemplate restTemplate;
	private RDBAnalyze rdbAnalyze;
	private RCTConfig.Email emailInfo;
	private RdbAnalyzeResultService rdbAnalyzeResultService;

	public AnalyzerStatusThread(List<AnalyzeInstance> analyzeInstances, RestTemplate restTemplate,
                                RDBAnalyze rdbAnalyze, RCTConfig.Email emailInfo, RdbAnalyzeResultService rdbAnalyzeResultService) {
		this.scheduleDetails = AppCache.scheduleDetailMap.get(rdbAnalyze.getId());
		this.analyzeInstances = analyzeInstances;
		this.restTemplate = restTemplate;
		this.rdbAnalyze = rdbAnalyze;
		this.emailInfo = emailInfo;
		this.rdbAnalyzeResultService = rdbAnalyzeResultService;
	}

	@Override
	public void run() {
		// 获取所有analyzer运行状态
		while (AppCache.isNeedAnalyzeStastus(rdbAnalyze.getId())) {

			for (AnalyzeInstance analyzeInstance : analyzeInstances) {
				for (ScheduleDetail scheduleDetail : scheduleDetails) {
					if (!(AnalyzeStatus.DONE.equals(scheduleDetail.getStatus())
							|| AnalyzeStatus.CANCELED.equals(scheduleDetail.getStatus()))) {
						String instanceStr = scheduleDetail.getInstance();
						try {
							if (analyzeInstance.getHost().equals(instanceStr.split(":")[0])) {
								getAnalyzerStatusRest(analyzeInstance);
								break;
							}

						} catch (Exception e) {
							LOG.error("get analyzer status has error.", e);
						}
					}
				}
			}

		}

		// 当所有analyzer运行完成，获取所有analyzer报表分析结果
		if (AppCache.isAnalyzeComplete(rdbAnalyze)) {

			Map<String, Set<String>> reportData = new HashMap<>();
			Map<String,Long>temp = new HashMap<>();
			for (AnalyzeInstance analyzeInstance : analyzeInstances) {
				try {
					Map<String, Set<String>> instanceReportData = getAnalyzerReportRest(analyzeInstance);
					if (reportData.size() == 0) {
						reportData.putAll(instanceReportData);
					} else {
						for(String key:instanceReportData.keySet()) {
							Set<String> newData = instanceReportData.get(key);
							if(reportData.containsKey(key)) {
								Set<String> oldData = reportData.get(key);
								oldData.addAll(newData);
								reportData.put(key, oldData);
							}else {
								reportData.put(key, newData);
							}
						}
					}
				} catch (Exception e) {
					LOG.error("get analyzer report has error.", e);
				}
			}
			for(String key:temp.keySet()) {
				LOG.info(key+":"+temp.get(key));
			}
			try {
				Map<String, ReportData> latestPrefixData = rdbAnalyzeResultService.getReportDataLatest(rdbAnalyze.getClusterId());
			    try {
                    rdbAnalyzeResultService.reportDataWriteToDb(rdbAnalyze, reportData);
                }catch (Exception e) {
                    LOG.error("reportDataWriteToDb has error.", e);
                }
				if(rdbAnalyze.isReport()) {
					EmailSendReport emailSendReport = new EmailSendReport();
					emailSendReport.sendEmailReport(rdbAnalyze, emailInfo, reportData, latestPrefixData);
				}
			} catch (Exception e) {
				LOG.error("email report has error.", e);
			}finally {
				for (AnalyzeInstance analyzeInstance : analyzeInstances) {
					String url = "http://"+analyzeInstance.getHost()+":"+analyzeInstance.getPort()+"/clear";
					@SuppressWarnings("unused")
                    ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
				}
				
			}
		}

	}

	private void getAnalyzerStatusRest(AnalyzeInstance instance) {
		if (null == instance) {
			LOG.warn("analyzeInstance is null!");
			return;
		}
		// String url = "http://127.0.0.1:8082/status";
		String host = instance.getHost();

		String url = "http://" + host + ":" + instance.getPort() + "/status";
		try {
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
			String str = responseEntity.getBody();
			JSONObject result = JSONObject.parseObject(str);
			if (null == result) {
				LOG.warn("get status URL :" + url + " no response!");
				return;
			} else {
				handleAnalyzerStatusMessage(result);
			}

		} catch (Exception e) {
			LOG.error("getAnalyzerStatusRest failed!", e);
		}
	}

	private Map<String, Set<String>> getAnalyzerReportRest(AnalyzeInstance instance) {
		if (null == instance) {
			LOG.warn("analyzeInstance is null!");
			return null;
		}
		// String url = "http://127.0.0.1:8082/report";
		String host = instance.getHost();

		String url = "http://" + host + ":" + instance.getPort() + "/report";
		try {
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
			String str = responseEntity.getBody();
			if (null == str) {
				LOG.warn("get report URL :" + url + " no response!");
				return null;
			} else {
				return handleAnalyzerReportMessage(str);
			}

		} catch (Exception e) {
			LOG.error("getAnalyzerReportRest failed!", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private void handleAnalyzerStatusMessage(JSONObject message) {
		String analyzeIP = message.getString("ip");
		if (message.get("scheduleInfo") == null || "".equals(message.getString("scheduleInfo").trim())) {
			return;
		}
		JSONObject scheduleInfo = message.getJSONObject("scheduleInfo");
		Long scheduleID = scheduleInfo.getLong("scheduleID");
		Map<String, Map<String, String>> rdbAnalyzeStatus = (Map<String, Map<String, String>>) message
				.get("rdbAnalyzeStatus");
		List<ScheduleDetail> scheduleDetails = new ArrayList<>();

		Map<String, String> newScheduleDtailsInstance = new HashMap<>();

		for (Entry<String, Map<String, String>> entry : rdbAnalyzeStatus.entrySet()) {
			String port = entry.getKey();
			if (entry.getValue() == null) {
				continue;
			}
			Map<String, String> analyzeInfo = entry.getValue();

			AnalyzeStatus status = AnalyzeStatus.fromString(analyzeInfo.get("status"));
			String count = analyzeInfo.get("count");
			String instance = analyzeIP + ":" + port;
			if (count == null || count.equals("")) {
				count = "0";
			}
			ScheduleDetail s = new ScheduleDetail(scheduleID, instance, Integer.parseInt(count), true, status);
			scheduleDetails.add(s);
			newScheduleDtailsInstance.put(instance, instance);
		}
		// 将新旧信息合并
		List<ScheduleDetail> oldScheduleDetails = AppCache.scheduleDetailMap.get(rdbAnalyze.getId());
		List<ScheduleDetail> oldNeedScheduleDetails = new ArrayList<>();

		if (oldScheduleDetails != null && oldScheduleDetails.size() > 0) {
			for (ScheduleDetail detail : oldScheduleDetails) {
				if (newScheduleDtailsInstance.containsKey(detail.getInstance())) {
					continue;
				}
				oldNeedScheduleDetails.add(detail);
			}
			scheduleDetails.addAll(oldNeedScheduleDetails);
		}

		AppCache.scheduleDetailMap.put(rdbAnalyze.getId(), scheduleDetails);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Set<String>> handleAnalyzerReportMessage(String message) {
		Map<String, JSONArray> reportMessage = (Map<String, JSONArray>) JSON.parse(message);
		Map<String, Set<String>> result = new HashMap<>();
		if (reportMessage != null && reportMessage.size() > 0) {
			for (String type : reportMessage.keySet()) {
				JSONArray array = reportMessage.get(type);
				List<String> list = JSONObject.parseArray(array.toJSONString(), String.class);
				Set<String> set = new HashSet<>(list);
				result.put(type, set);
			}
		}
		return result;
	}

}
