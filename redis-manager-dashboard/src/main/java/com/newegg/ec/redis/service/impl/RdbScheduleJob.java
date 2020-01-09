package com.newegg.ec.redis.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.entity.RDBAnalyze;
import com.newegg.ec.redis.service.IRdbScheduleJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Kyle.K.Zhao
 * @date 1/9/2020 08:59
 */
@DisallowConcurrentExecution
public class RdbScheduleJob implements IRdbScheduleJob {

    private static final Logger LOG = LoggerFactory.getLogger(RdbScheduleJob.class);

    @Autowired
    RdbAnalyzeService rdbAnalyzeService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        RDBAnalyze rdbAnalyze = (RDBAnalyze) context.getJobDetail().getJobDataMap().get("rdbAnalyzeJob");

        int[] strings = null;
        if (rdbAnalyze.getAnalyzer().contains(",")) {
            String[] str = rdbAnalyze.getAnalyzer().split(",");
            strings = new int[str.length];
            for (int i = 0; i < str.length; i++) {
                strings[i] = Integer.parseInt(str[i]);
            }
        } else {
            strings = new int[1];
            strings[0] = Integer.parseInt(rdbAnalyze.getAnalyzer());
        }
        JSONObject status = rdbAnalyzeService.allocationRDBAnalyzeJob(rdbAnalyze.getId(), strings);
        LOG.info("cron :{}", rdbAnalyze.getSchedule());
        if ((boolean) status.get("status")) {
            LOG.info("cron success,message:{}", status.get("message"));
        } else {
            LOG.warn("cron faild!,message:{}", status.get("message"));
        }

    }
}
