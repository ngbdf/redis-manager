package com.newegg.ec.redis.config;



import com.newegg.ec.redis.plugin.rct.common.JobFactory;
import com.newegg.ec.redis.schedule.RDBScheduleJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.io.IOException;

/**
 * @author Kyle.K.Zhao
 * @date 1/9/2020 08:18
 */
@Configuration
public class QuartzSchedule {
    @Autowired
    private JobFactory jobFactory;

    @Bean(name = "schedulerFactoryBean")
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(true);
        // 延时启动
        factory.setStartupDelay(20);
        // 自定义Job Factory，用于Spring注入
        factory.setJobFactory(jobFactory);
        return factory;
    }

    @Bean(name = "rdbScheduleJob")
    public JobDetailFactoryBean rdbScheduleJob() {
        JobDetailFactoryBean jobDetail = new JobDetailFactoryBean();
        jobDetail.setJobClass(RDBScheduleJob.class);
        return jobDetail;
    }
}
