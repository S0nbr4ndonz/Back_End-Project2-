package com.group7.jobTrackerApplication.controller;

import com.group7.jobTrackerApplication.DTO.CreateJobEntryRequest;
import com.group7.jobTrackerApplication.DTO.GetJobEntryRequest;
import com.group7.jobTrackerApplication.DTO.UpdateJobEntryRequest;
import com.group7.jobTrackerApplication.model.JobEntry;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.service.JobEntryService;
import com.group7.jobTrackerApplication.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * Exposes CRUD endpoints for job entry resources owned by the authenticated user.
 */
@RestController
@RequestMapping("/job-entries")
@Tag(name = "Job Entries", description = "Operations for creating and managing tracked job listings")
public class JobEntryController {

    private final JobEntryService jobEntryService;
    private final UserService userService;

    /**
     * Creates a new controller with the required job entry and user services.
     *
     * @param jobEntryService service responsible for job entry business logic
     * @param userService service used to resolve the authenticated user
     */
    public JobEntryController(JobEntryService jobEntryService, UserService userService){
        this.jobEntryService = jobEntryService;
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Return raw OAuth attributes", description = "Debug endpoint that exposes the authenticated user's OAuth attributes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OAuth attribute payload returned"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    public Object me(@AuthenticationPrincipal OAuth2User user) {
        return user == null ? "NOT AUTHENTICATED" : user.getAttributes();
    }

    @GetMapping
    @Operation(summary = "List job entries", description = "Returns all job entries owned by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job entries returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "No job entries found")
    })
    public ResponseEntity<List<GetJobEntryRequest>> getAll(@AuthenticationPrincipal OAuth2User principal){
        return ResponseEntity.ok(jobEntryService.getAll(userService.getOrCreateFromOAuth(principal)));
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Get a job entry", description = "Returns a single job entry owned by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Job entry returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Job entry not found")
    })
    public ResponseEntity<GetJobEntryRequest> getById(@PathVariable Long jobId, @AuthenticationPrincipal OAuth2User principal){
        User user = userService.getOrCreateFromOAuth(principal);
        return ResponseEntity.ok(jobEntryService.getById(jobId, user));
    }

    @PostMapping
    @Operation(summary = "Create a job entry", description = "Creates a new job entry for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Job entry created"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<JobEntry> create(@AuthenticationPrincipal OAuth2User principal, @RequestBody CreateJobEntryRequest request){
        JobEntry created = jobEntryService.create(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{jobId}")
    @Operation(summary = "Replace a job entry", description = "Fully replaces a job entry owned by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Job entry replaced"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not own the job entry")
    })
    public ResponseEntity<JobEntry> replace(@PathVariable Long jobId, @RequestBody UpdateJobEntryRequest request, @AuthenticationPrincipal OAuth2User principal ){
        JobEntry updated = jobEntryService.replace(jobId, request, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{jobId}")
    @Operation(summary = "Update part of a job entry", description = "Partially updates fields on a job entry owned by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Job entry updated"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not own the job entry")
    })
    public ResponseEntity<JobEntry> patch(@PathVariable Long jobId, @RequestBody UpdateJobEntryRequest request, @AuthenticationPrincipal OAuth2User principal){
        JobEntry patched = jobEntryService.patch(jobId, request, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{jobId}")
    @Operation(summary = "Delete a job entry", description = "Deletes a job entry owned by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Job entry deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not own the job entry")
    })
    public ResponseEntity<JobEntry> delete(@PathVariable Long jobId, @AuthenticationPrincipal OAuth2User principal){
        jobEntryService.delete(jobId, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.noContent().build();
    }
}
