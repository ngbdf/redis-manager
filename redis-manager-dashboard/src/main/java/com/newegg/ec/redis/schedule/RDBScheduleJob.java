package com.newegg.ec.redis.schedule;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.entity.RDBAnalyze;
import com.newegg.ec.redis.service.impl.RdbAnalyzeService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@DisallowConcurrentExecution
public class RDBScheduleJob implements Job {

	private static final Logger LOG = LoggerFactory.getLogger(RDBScheduleJob.class);

	@Autowired
	RdbAnalyzeService rdbAnalyzeService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		RDBAnalyze rdbAnalyze = (RDBAnalyze) context.getJobDetail().getJobDataMap().get("rdbAnalyzeJob");
		JSONObject status = rdbAnalyzeService.allocationRDBAnalyzeJob(rdbAnalyze.getId());
		LOG.info("cron :{}", rdbAnalyze.getSchedule());
		if ((boolean) status.get("status")) {
			LOG.info("cron success,message:{}", status.get("message"));
		} else {
			LOG.warn("cron faild!,message:{}", status.get("message"));
		}

	}


}
