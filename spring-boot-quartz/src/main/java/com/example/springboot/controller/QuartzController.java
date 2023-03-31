package com.example.springboot.controller;

import com.example.springboot.dto.QuartzJobRequest;
import com.example.springboot.dto.QuartzJobResponse;
import com.example.springboot.job.QuartzSimpleJob;
import com.example.springboot.service.QuartzService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/scheduler")
public class QuartzController {

    @Autowired
    private QuartzService quartzService;

    @GetMapping("/jobs")
    public ResponseEntity<List<QuartzJobResponse>> getAllJobs() throws SchedulerException {
        List<QuartzJobResponse> jobList = quartzService.scheduleJobList();
        return new ResponseEntity<>(jobList, HttpStatus.OK);
    }

    @PostMapping("/job")
    public ResponseEntity<String> scheduleJob(@RequestBody QuartzJobRequest quartzJobRequest) throws SchedulerException {
        quartzService.scheduleJob(quartzJobRequest, QuartzSimpleJob.class);
        return new ResponseEntity<>("Job created successfully", HttpStatus.CREATED);
    }

    @DeleteMapping("/job")
    public ResponseEntity<String> deleteJob(@RequestParam(name = "name") String name, @RequestParam(name = "group") String group) throws SchedulerException {
        quartzService.deleteJob(name, group);
        return new ResponseEntity<>("Job deleted successfully", HttpStatus.OK);
    }


    @PutMapping("/job/pause")
    public ResponseEntity<String> pauseJob(@RequestParam(name = "name") String name, @RequestParam(name = "group") String group) throws SchedulerException {
        quartzService.pauseJob(name, group);
        return new ResponseEntity<>("Job paused successfully", HttpStatus.OK);
    }

    @PutMapping("/job/resume")
    public ResponseEntity<String> resumeJob(@RequestParam(name = "name") String name, @RequestParam(name = "group") String group) throws SchedulerException {
        quartzService.resumeJob(name, group);
        return new ResponseEntity<>("Job resumed successfully", HttpStatus.OK);
    }

    @PutMapping("/job/stop")
    public ResponseEntity<String> stopJob(@RequestParam(name = "name") String name, @RequestParam(name = "group") String group) throws SchedulerException {
        quartzService.stopJob(name, group);
        return new ResponseEntity<>("Job stopped successfully", HttpStatus.OK);
    }

}
