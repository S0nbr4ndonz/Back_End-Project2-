package com.group7.jobTrackerApplication.controller;

import com.group7.jobTrackerApplication.DTO.CreateJobEntryRequest;
import com.group7.jobTrackerApplication.DTO.UpdateJobEntryRequest;
import com.group7.jobTrackerApplication.model.JobEntry;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.service.JobEntryService;
import com.group7.jobTrackerApplication.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RestController
@RequestMapping("/job-entries")
public class JobEntryController {

    private final JobEntryService jobEntryService;
    private final UserService userService;


    public JobEntryController(JobEntryService jobEntryService, UserService userService){
        this.jobEntryService = jobEntryService;
        this.userService = userService;
    }

    @GetMapping("/me")
    public Object me(@AuthenticationPrincipal OAuth2User user) {
        return user == null ? "NOT AUTHENTICATED" : user.getAttributes();
    }

    @GetMapping
    public ResponseEntity<List<JobEntry>> getAll(@AuthenticationPrincipal OAuth2User principal){
        return ResponseEntity.ok(jobEntryService.getAll(userService.getOrCreateFromOAuth(principal)));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobEntry> getById(@PathVariable Long jobId, @AuthenticationPrincipal OAuth2User principal){
        User user = userService.getOrCreateFromOAuth(principal);
        return ResponseEntity.ok(jobEntryService.getById(jobId, user));
    }

    @PostMapping
    public ResponseEntity<JobEntry> create(@AuthenticationPrincipal OAuth2User principal, @RequestBody CreateJobEntryRequest request){
        JobEntry created = jobEntryService.create(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<JobEntry> replace(@PathVariable Long jobId, @RequestBody UpdateJobEntryRequest request, @AuthenticationPrincipal OAuth2User principal ){
        JobEntry updated = jobEntryService.replace(jobId, request, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{jobId}")
    public ResponseEntity<JobEntry> patch(@PathVariable Long jobId, @RequestBody UpdateJobEntryRequest request, @AuthenticationPrincipal OAuth2User principal){
        JobEntry patched = jobEntryService.patch(jobId, request, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.ok(patched);
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<JobEntry> delete(@PathVariable Long jobId, @AuthenticationPrincipal OAuth2User principal){
        jobEntryService.delete(jobId, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.noContent().build();
    }
}