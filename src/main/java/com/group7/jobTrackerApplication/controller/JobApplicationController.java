package com.group7.jobTrackerApplication.controller;

import com.group7.jobTrackerApplication.DTO.CreateJobApplicationRequest;
import com.group7.jobTrackerApplication.DTO.GetJobApplicationRequest;
import com.group7.jobTrackerApplication.DTO.UpdateJobApplicationRequest;
import com.group7.jobTrackerApplication.model.JobApplication;
import com.group7.jobTrackerApplication.service.JobApplicationService;
import com.group7.jobTrackerApplication.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Exposes CRUD endpoints for job application resources belonging to the authenticated user.
 */
@RestController
@RequestMapping("/job-applications")
@Tag(name = "Job Applications", description = "Operations for tracking application status and lifecycle updates")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;
    private final UserService userService;

    /**
     * Creates a new controller with the required application and user services.
     *
     * @param jobApplicationService service responsible for job application logic
     * @param userService service used to resolve the authenticated user
     */
    public JobApplicationController(JobApplicationService jobApplicationService, UserService userService){
        this.jobApplicationService = jobApplicationService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List job applications", description = "Returns all job applications owned by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job applications returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "No job applications found")
    })
    public ResponseEntity<List<GetJobApplicationRequest>> getAll(@AuthenticationPrincipal OAuth2User principal){
        return ResponseEntity.ok(jobApplicationService.getAll(userService.getOrCreateFromOAuth(principal)));
    }

    @GetMapping("/{applicationId}")
    @Operation(summary = "Get a job application", description = "Returns a single job application owned by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job application returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Job application not found")
    })
    public ResponseEntity<GetJobApplicationRequest> getById(@PathVariable Long applicationId, @AuthenticationPrincipal OAuth2User principal){
        return ResponseEntity.ok(jobApplicationService.getById(applicationId, userService.getOrCreateFromOAuth(principal)));
    }

    @PostMapping
    @Operation(summary = "Create a job application", description = "Creates a new job application for one of the authenticated user's job entries.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Job application created"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Referenced job entry not found")
    })
    public ResponseEntity<JobApplication> create(@RequestBody CreateJobApplicationRequest jobApplication, @AuthenticationPrincipal OAuth2User principal){
        JobApplication created = jobApplicationService.create(jobApplication, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{applicationId}")
    @Operation(summary = "Replace a job application", description = "Fully replaces a job application owned by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Job application replaced"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not own the job application")
    })
    public ResponseEntity<JobApplication> replace(@PathVariable Long applicationId, @RequestBody UpdateJobApplicationRequest jobApplication, @AuthenticationPrincipal OAuth2User principal ){
        JobApplication updated = jobApplicationService.replace(applicationId, jobApplication, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{applicationId}")
    @Operation(summary = "Update part of a job application", description = "Partially updates a job application owned by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Job application updated"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not own the job application")
    })
    public ResponseEntity<JobApplication> patch(@PathVariable Long applicationId, @RequestBody UpdateJobApplicationRequest request, @AuthenticationPrincipal OAuth2User principal){
        JobApplication patched = jobApplicationService.patch(applicationId, request, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{applicationId}")
    @Operation(summary = "Delete a job application", description = "Deletes a job application owned by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Job application deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not own the job application")
    })
    public ResponseEntity<JobApplication> delete(@PathVariable Long applicationId, @AuthenticationPrincipal OAuth2User principal){
        jobApplicationService.delete(applicationId, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.noContent().build();
    }
}
