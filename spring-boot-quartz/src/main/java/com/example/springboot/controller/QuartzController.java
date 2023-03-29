package com.example.springboot.controller;

import com.example.springboot.job.SimpleJob;
import com.example.springboot.service.QuartzService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/scheduler")
public class QuartzController {

    @Autowired
    private QuartzService quartzService;

    @PostMapping("/job")
    public ResponseEntity<String> scheduleJob(
            @RequestParam(name = "name") String name,
            @RequestParam(name = "group") String group,
            @RequestParam(name = "start-date-time") LocalDateTime startDateTime,
            @RequestParam(name = "repeat-interval") int repeatInterval,
            @RequestParam(name = "repeat-count") int repeatCount) throws SchedulerException {

        quartzService.scheduleJob(name, group, startDateTime, repeatInterval, repeatCount, SimpleJob.class, null);
        return new ResponseEntity<>("Job created successfully", HttpStatus.CREATED);
    }

    @DeleteMapping("/job")
    public ResponseEntity<String> deleteJob(@RequestParam(name = "name") String name, @RequestParam(name = "group") String group) throws SchedulerException {
        quartzService.deleteJob(name, group);
        return new ResponseEntity<>("Job deleted successfully", HttpStatus.OK);
    }

}
