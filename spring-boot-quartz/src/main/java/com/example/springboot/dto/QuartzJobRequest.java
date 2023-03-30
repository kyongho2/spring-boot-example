package com.example.springboot.dto;

import lombok.Getter;
import lombok.Setter;
import org.quartz.JobDataMap;

import java.time.LocalDateTime;

@Getter
@Setter
public class QuartzJobRequest {

    private String name;
    private String group;

    private LocalDateTime startAt;
    private int intervalInSeconds;

    private String cronExpression;

    private JobDataMap jobDataMap;

    public LocalDateTime getStartAt() {
        return startAt == null ? LocalDateTime.now() : startAt;
    }

}
