package com.example.springboot.config;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Slf4j
@Configuration
public class QuartzConfig {

    private final GlobalTriggerListener globalTriggerListener;

    private final GlobalJobListener globalJobListener;

    private final QuartzProperties quartzProperties;

    public QuartzConfig(GlobalTriggerListener globalTriggerListener, GlobalJobListener globalJobListener, QuartzProperties quartzProperties) {
        this.globalTriggerListener = globalTriggerListener;
        this.globalJobListener = globalJobListener;
        this.quartzProperties = quartzProperties;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(ApplicationContext applicationContext) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();

        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        schedulerFactoryBean.setJobFactory(jobFactory);

        schedulerFactoryBean.setApplicationContext(applicationContext);

        Properties properties = new Properties();
        properties.putAll(quartzProperties.getProperties());

        schedulerFactoryBean.setGlobalTriggerListeners(globalTriggerListener);
        schedulerFactoryBean.setGlobalJobListeners(globalJobListener);
        schedulerFactoryBean.setOverwriteExistingJobs(true);
        schedulerFactoryBean.setQuartzProperties(properties);
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);

        return schedulerFactoryBean;
    }

    private static class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {

        private transient AutowireCapableBeanFactory beanFactory;

        @Override
        public void setApplicationContext(final ApplicationContext context) {
            beanFactory = context.getAutowireCapableBeanFactory();
        }

        @Override
        protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
            final Object job = super.createJobInstance(bundle);
            beanFactory.autowireBean(job);

            return job;
        }

    }

    @Component
    public static class GlobalJobListener implements JobListener {

        @Override
        public String getName() {
            return getClass().getSimpleName();
        }

        @Override
        public void jobToBeExecuted(JobExecutionContext context) {
            JobKey jobKey = context.getJobDetail().getKey();
            log.info("jobToBeExecuted: jobKey({})", jobKey);
        }

        @Override
        public void jobExecutionVetoed(JobExecutionContext context) {
            JobKey jobKey = context.getJobDetail().getKey();
            log.info("jobExecutionVetoed: jobKey({})", jobKey);
        }

        @Override
        public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
            JobKey jobKey = context.getJobDetail().getKey();
            log.info("jobWasExecuted: jobKey({})", jobKey);
        }

    }

    @Component
    public static class GlobalTriggerListener implements TriggerListener {

        @Override
        public String getName() {
            return getClass().getSimpleName();
        }

        @Override
        public void triggerFired(Trigger trigger, JobExecutionContext context) {
            JobKey jobKey = trigger.getJobKey();
            log.info("triggerFired: startTime({}), jobKey({})", trigger.getStartTime(), jobKey);
        }

        @Override
        public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
            return false;
        }

        @Override
        public void triggerMisfired(Trigger trigger) {
            JobKey jobKey = trigger.getJobKey();
            log.info("triggerFired: startTime({}), jobKey({})", trigger.getStartTime(), jobKey);
        }

        @Override
        public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {
            JobKey jobKey = trigger.getJobKey();
            log.info("triggerComplete: startTime({}), jobKey({})", trigger.getStartTime(), jobKey);
        }

    }

}
