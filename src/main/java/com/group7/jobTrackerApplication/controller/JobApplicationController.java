package com.group7.jobTrackerApplication.controller;

import com.group7.jobTrackerApplication.DTO.CreateJobApplicationRequest;
import com.group7.jobTrackerApplication.DTO.GetJobApplicationRequest;
import com.group7.jobTrackerApplication.DTO.UpdateJobApplicationRequest;
import com.group7.jobTrackerApplication.model.JobApplication;
import com.group7.jobTrackerApplication.service.JobApplicationService;
import com.group7.jobTrackerApplication.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/job-applications")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;
    private final UserService userService;

    public JobApplicationController(JobApplicationService jobApplicationService, UserService userService){
        this.jobApplicationService = jobApplicationService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<GetJobApplicationRequest>> getAll(@AuthenticationPrincipal OAuth2User principal){
        return ResponseEntity.ok(jobApplicationService.getAll(userService.getOrCreateFromOAuth(principal)));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<GetJobApplicationRequest> getById(@PathVariable Long applicationId, @AuthenticationPrincipal OAuth2User principal){
        return ResponseEntity.ok(jobApplicationService.getById(applicationId, userService.getOrCreateFromOAuth(principal)));
    }

    @PostMapping
    public ResponseEntity<JobApplication> create(@RequestBody CreateJobApplicationRequest jobApplication, @AuthenticationPrincipal OAuth2User principal){
        JobApplication created = jobApplicationService.create(jobApplication, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{applicationId}")
    public ResponseEntity<JobApplication> replace(@PathVariable Long applicationId, @RequestBody UpdateJobApplicationRequest jobApplication, @AuthenticationPrincipal OAuth2User principal ){
        JobApplication updated = jobApplicationService.replace(applicationId, jobApplication, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{applicationId}")
    public ResponseEntity<JobApplication> patch(@PathVariable Long applicationId, @RequestBody UpdateJobApplicationRequest request, @AuthenticationPrincipal OAuth2User principal){
        JobApplication patched = jobApplicationService.patch(applicationId, request, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{applicationId}")
    public ResponseEntity<JobApplication> delete(@PathVariable Long applicationId, @AuthenticationPrincipal OAuth2User principal){
        jobApplicationService.delete(applicationId, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.noContent().build();
    }
}