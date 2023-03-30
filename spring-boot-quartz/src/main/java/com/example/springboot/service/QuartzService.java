package com.example.springboot.service;

import com.example.springboot.dto.QuartzJobRequest;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
public class QuartzService {

    private final ApplicationContext context;

    private final SchedulerFactoryBean schedulerFactoryBean;

    public QuartzService(ApplicationContext context, SchedulerFactoryBean schedulerFactoryBean) {
        this.context = context;
        this.schedulerFactoryBean = schedulerFactoryBean;
    }

    public void scheduleJob(QuartzJobRequest quartzJobRequest, Class<? extends Job> jobClass) throws SchedulerException {
        String cronExpression = quartzJobRequest.getCronExpression();
        if (cronExpression != null && !cronExpression.isEmpty()) {
            scheduleJob(quartzJobRequest.getName(), quartzJobRequest.getGroup(), cronExpression, quartzJobRequest.getJobDataMap(), jobClass);
        } else {
            scheduleJob(quartzJobRequest.getName(), quartzJobRequest.getGroup(), quartzJobRequest.getStartAt(), quartzJobRequest.getRepeatInterval(), quartzJobRequest.getRepeatCount(), quartzJobRequest.getJobDataMap(), jobClass);
        }
    }

    private void scheduleJob(String name, String group, String cronExpression, JobDataMap jobDataMap, Class<? extends Job> jobClass) throws SchedulerException {
        Trigger trigger = createCronTrigger(name, group, cronExpression);
        JobDetail jobDetail = createJob(name, group, jobClass, jobDataMap, context);

        schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, trigger);
    }

    private void scheduleJob(String name, String group, LocalDateTime startDateTime, int repeatInterval, int repeatCount, JobDataMap jobDataMap, Class<? extends Job> jobClass) throws SchedulerException {
        Trigger trigger = createSimpleTrigger(name, group, startDateTime, repeatInterval, repeatCount);
        JobDetail jobDetail = createJob(name, group, jobClass, jobDataMap, context);

        schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, trigger);
    }

    public void deleteJob(String name, String group) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(name, group);
        schedulerFactoryBean.getScheduler().deleteJob(jobKey);
    }

    private static Trigger createCronTrigger(String name, String group, String cronExpression) {
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setName(name);
        factoryBean.setGroup(group);
        factoryBean.setCronExpression(cronExpression);
        factoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);

        try {
            factoryBean.afterPropertiesSet();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return factoryBean.getObject();
    }

    private static Trigger createSimpleTrigger(String name, String group, LocalDateTime startAt, int repeatInterval, int repeatCount) {
        if (startAt == null) {
            startAt = LocalDateTime.now();
        }

        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setName(name);
        factoryBean.setGroup(group);
        factoryBean.setStartTime(Date.from(startAt.atZone(ZoneId.systemDefault()).toInstant()));
        factoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        factoryBean.setRepeatInterval(repeatInterval);
        factoryBean.setRepeatCount(repeatCount);

        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    private static JobDetail createJob(String name, String group, Class<? extends Job> jobClass, JobDataMap jobDataMap, ApplicationContext context) {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(jobClass);
        factoryBean.setDurability(false);
        factoryBean.setApplicationContext(context);
        factoryBean.setName(name);
        factoryBean.setGroup(group);

        if (jobDataMap != null) {
            factoryBean.setJobDataMap(jobDataMap);
        }

        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

}
