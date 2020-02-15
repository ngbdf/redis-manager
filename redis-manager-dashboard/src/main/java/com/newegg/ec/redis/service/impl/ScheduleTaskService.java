package com.newegg.ec.redis.service.impl;

import com.newegg.ec.redis.entity.RDBAnalyze;
import com.newegg.ec.redis.service.IScheduleTaskService;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Service
public class ScheduleTaskService implements IScheduleTaskService {
    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    private Scheduler getScheduler() {
        return schedulerFactoryBean.getScheduler();
    }

    public void addTask(Object object, Class<? extends Job> jobClass) throws SchedulerException {
        TriggerKey triggerKey = null;
        @SuppressWarnings("unused")
        CronTrigger triggers = null;
        JobDetail jobDetail = null;
        JobKey jobKey = null;
        String cronExpress = "";
        if (object instanceof RDBAnalyze) {
            RDBAnalyze rdbAnalyze = (RDBAnalyze) object;
            triggerKey = TriggerKey.triggerKey("rdb" + String.valueOf(rdbAnalyze.getId()), "Group");
            triggers = (CronTrigger) getScheduler().getTrigger(triggerKey);
            jobKey = JobKey.jobKey("rdb" + String.valueOf(rdbAnalyze.getId()), "Group");
            jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobKey).build();
            jobDetail.getJobDataMap().put("rdbAnalyzeJob", rdbAnalyze);
            cronExpress = rdbAnalyze.getSchedule();

        }
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpress);
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
        if (getScheduler().checkExists(jobKey)) {
            getScheduler().deleteJob(jobKey);
        }
        if (getScheduler().checkExists(triggerKey)) {
            getScheduler().pauseTrigger(triggerKey);
            getScheduler().unscheduleJob(triggerKey);
        }
        getScheduler().scheduleJob(jobDetail, trigger);

    }

    public void delTask(String jobId) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobId, "Group");
        JobKey jobKey = new JobKey(jobId, "Group");
        if (getScheduler().checkExists(jobKey)) {
            getScheduler().pauseJob(jobKey);
            getScheduler().deleteJob(jobKey);
        }
        if (getScheduler().checkExists(triggerKey)) {
            getScheduler().pauseTrigger(triggerKey);
            getScheduler().unscheduleJob(triggerKey);
        }
    }

    public List<String> getRecentTriggerTime(String cron) {
        List<String> list = new ArrayList<String>();
        if (!CronExpression.isValidExpression(cron)) {
            return list;
        }
        CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity("date")
                .withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = cronTrigger.getStartTime();
        for (int i = 0; i < 5; i++) {
            Date time = cronTrigger.getFireTimeAfter(startTime);
            list.add(sdf.format(time));
            startTime = time;
        }
        return list;
    }

    public boolean vaildCronExpress(String cron){
        return CronExpression.isValidExpression(cron);
    }
    public String getJobStatus(String triggerName) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, "Group");
        Trigger.TriggerState status = getScheduler().getTriggerState(triggerKey);
        return status.toString();

    }

}
