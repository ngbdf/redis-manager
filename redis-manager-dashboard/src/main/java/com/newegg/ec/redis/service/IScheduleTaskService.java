package com.newegg.ec.redis.service;

import org.quartz.Job;
import org.quartz.SchedulerException;

import java.util.List;

public interface IScheduleTaskService {
    public void addTask(Object object, Class<? extends Job> jobClass) throws SchedulerException;

    public void delTask(String jobId) throws SchedulerException;

    public List<String> getRecentTriggerTime(String cron);

    public String getJobStatus(String triggerName) throws SchedulerException;

}
