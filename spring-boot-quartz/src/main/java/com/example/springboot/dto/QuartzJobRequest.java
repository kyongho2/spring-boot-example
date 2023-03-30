package com.example.springboot.dto;

import lombok.*;
import org.quartz.JobDataMap;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class QuartzJobRequest {

    private String name;
    private String group;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startAt;
    private int repeatInterval;
    private int repeatCount;

    private String cronExpression;
    private JobDataMap jobDataMap;

}
