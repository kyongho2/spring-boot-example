package com.example.springboot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class QuartzJobResponse {

    private String name;
    private String group;
    private String status;

    private LocalDateTime startAt;
    private LocalDateTime prevFireAt;
    private LocalDateTime nextFireAt;

}
