package com.example.springboot.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Slf4j
public class QuartzSimpleJob extends QuartzJobBean implements InterruptableJob {

    private volatile boolean isJobInterrupted = false;

    private volatile Thread currThread;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        if (!isJobInterrupted) {
            currThread = Thread.currentThread();

            IntStream.range(0, 10).forEach(i -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            });
        }
    }

    @Override
    public void interrupt() {
        isJobInterrupted = true;
        if (currThread != null) {
            log.info("Interrupt: {}", currThread.getName());
            currThread.interrupt();
        }
    }

}
