package com.example.springboot.service;

import com.example.springboot.dto.QuartzJobRequest;
import com.example.springboot.dto.QuartzJobResponse;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class QuartzService {

    private final ApplicationContext context;

    private final SchedulerFactoryBean schedulerFactoryBean;

    public QuartzService(ApplicationContext context, SchedulerFactoryBean schedulerFactoryBean) {
        this.context = context;
        this.schedulerFactoryBean = schedulerFactoryBean;
    }

    public List<QuartzJobResponse> scheduleJobList() throws SchedulerException {
        List<QuartzJobResponse> jobList = new ArrayList<>();

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        for (String jobGroupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroupName))) {
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                if (triggers == null || triggers.isEmpty()) {
                    continue;
                }

                boolean isRunning = false;

                List<JobExecutionContext> currentJobs = scheduler.getCurrentlyExecutingJobs();
                if (currentJobs != null) {
                    for (JobExecutionContext currentJob : currentJobs) {
                        if (jobKey.getName().equals(currentJob.getJobDetail().getKey().getName())) {
                            isRunning = true;
                            break;
                        }
                    }
                }

                Trigger trigger = triggers.get(0);

                String status;
                if (isRunning) {
                    status = "RUNNING";
                } else {
                    Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                    if (Trigger.TriggerState.NORMAL.equals(triggerState)) {
                        status = "SCHEDULED";
                    } else {
                        status = triggerState.name().toUpperCase();
                    }
                }

                QuartzJobResponse quartzJobResponse = QuartzJobResponse.builder()
                        .name(jobKey.getName())
                        .group(jobKey.getGroup())
                        .status(status)
                        .startAt(asLocalDateTime(trigger.getStartTime()))
                        .endAt(asLocalDateTime(trigger.getEndTime()))
                        .prevFireAt(asLocalDateTime(trigger.getPreviousFireTime()))
                        .nextFireAt(asLocalDateTime(trigger.getNextFireTime()))
                        .build();

                jobList.add(quartzJobResponse);
            }
        }

        return jobList;
    }

    public void scheduleJob(QuartzJobRequest quartzJobRequest, Class<? extends Job> jobClass) throws SchedulerException {
        String cronExpression = quartzJobRequest.getCronExpression();
        if (cronExpression != null && !cronExpression.isEmpty()) {
            scheduleJob(quartzJobRequest.getName(), quartzJobRequest.getGroup(), cronExpression, quartzJobRequest.getJobDataMap(), jobClass);
        } else {
            scheduleJob(quartzJobRequest.getName(), quartzJobRequest.getGroup(), quartzJobRequest.getStartAt(), quartzJobRequest.getIntervalInSeconds(), quartzJobRequest.getJobDataMap(), jobClass);
        }
    }

    private void scheduleJob(String name, String group, LocalDateTime startAt, int intervalInSeconds, JobDataMap jobDataMap, Class<? extends Job> jobClass) throws SchedulerException {
        Trigger trigger = createSimpleTrigger(name, group, startAt, intervalInSeconds);
        JobDetail jobDetail = createJobDetail(name, group, jobClass, jobDataMap);

        schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, trigger);
    }

    private void scheduleJob(String name, String group, String cronExpression, JobDataMap jobDataMap, Class<? extends Job> jobClass) throws SchedulerException {
        Trigger trigger = createCronTrigger(name, group, cronExpression);
        JobDetail jobDetail = createJobDetail(name, group, jobClass, jobDataMap);

        schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, trigger);
    }

    public void deleteJob(String name, String group) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(name, group);
        schedulerFactoryBean.getScheduler().deleteJob(jobKey);
    }

    public void pauseJob(String name, String group) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(name, group);
        schedulerFactoryBean.getScheduler().pauseJob(jobKey);
    }

    public void resumeJob(String name, String group) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(name, group);
        schedulerFactoryBean.getScheduler().resumeJob(jobKey);
    }

    public void stopJob(String name, String group) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(name, group);
        schedulerFactoryBean.getScheduler().interrupt(jobKey);
    }

    private SimpleTrigger createSimpleTrigger(String name, String group, LocalDateTime startAt, int intervalInSeconds) {
        if (intervalInSeconds == 0) {
            return (SimpleTrigger) TriggerBuilder.newTrigger()
                    .withIdentity(name, group)
                    .startAt(asDate(startAt))
                    .build();
        }

        return TriggerBuilder.newTrigger()
                .withIdentity(name, group)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(intervalInSeconds)
                        .repeatForever())
                .startAt(asDate(startAt))
                .build();
    }

    private Trigger createCronTrigger(String name, String group, String cronExpression) {
        return TriggerBuilder.newTrigger()
                .withIdentity(name, group)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
    }

    private JobDetail createJobDetail(String name, String group, Class<? extends Job> jobClass, JobDataMap jobDataMap) {
        if (jobDataMap == null) {
            jobDataMap = new JobDataMap();
            jobDataMap.put(ApplicationContext.class.getSimpleName(), context);
        }

        return JobBuilder.newJob(jobClass)
                .withIdentity(name, group)
                .setJobData(jobDataMap)
                .build();
    }

    private LocalDateTime asLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private Date asDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

}
