package com.group7.jobTrackerApplication.service;

import com.group7.jobTrackerApplication.DTO.CreateJobApplicationRequest;
import com.group7.jobTrackerApplication.DTO.GetJobApplicationRequest;
import com.group7.jobTrackerApplication.DTO.UpdateJobApplicationRequest;
import com.group7.jobTrackerApplication.model.JobApplication;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.repository.JobApplicationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import com.group7.jobTrackerApplication.exception.ResourceNotFoundException;
import com.group7.jobTrackerApplication.exception.ForbiddenException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@Service
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final UserService userService;

    public JobApplicationService(JobApplicationRepository jobApplicationRepository, UserService userService) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.userService = userService;
    }

    public List<GetJobApplicationRequest> getAll(User user) {
        List<JobApplication> applications = jobApplicationRepository.findALlByUser_UserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(("Job applications not found")));

        return applications.stream()
                .map( app -> new GetJobApplicationRequest(
                        app.getApplicationId(),
                        app.getJobEntry().getJobId(),
                        app.getJobEntry().getJobTitle()
                )).toList();
    }

    public JobApplication getById(Long applicationId, User user) {
        return jobApplicationRepository.findByApplicationIdAndUserId(applicationId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found"));
    }

    public JobApplication create(CreateJobApplicationRequest jobApplication, User user) {

        JobApplication jp = new JobApplication();
        jp.setDateApplied(jobApplication.dateApplied());
        jp.setStatus(jobApplication.status());
        jp.getUser().setUserId(user.getUserId());
        jp.getJobEntry().setJobId(jobApplication.jobId());

        return jobApplicationRepository.save(jp);
    }

    public JobApplication replace(Long applicationId, UpdateJobApplicationRequest jobApplication, User user) {
        JobApplication toChange = jobApplicationRepository.findByApplicationIdAndUserId(applicationId, user.getUserId())
                .orElseThrow(()-> new ForbiddenException("Not authorized to update requested job application."));

        toChange.setDateApplied(jobApplication.dataApplied());
        toChange.setStatus(jobApplication.status());
        toChange.setJobId(jobApplication.jobId());

        return jobApplicationRepository.save(toChange);
    }

    public JobApplication patch(Long applicationId, UpdateJobApplicationRequest request, User user) {
        JobApplication toChange = jobApplicationRepository.findByApplicationIdAndUserId(applicationId, user.getUserId())
                .orElseThrow(()-> new ForbiddenException("Not authorized to update requested job application."));

        if(request.dataApplied() != null) toChange.setDateApplied(request.dataApplied());
        if(request.status() != null) toChange.setStatus(request.status());
        if(request.jobId() != null) toChange.setJobId(request.jobId());

        return jobApplicationRepository.save(toChange);
    }

    public void delete(Long applicationId, User user) {
        JobApplication toDelete = jobApplicationRepository.findByApplicationIdAndUserId(applicationId, user.getUserId())
                        .orElseThrow(() -> new ForbiddenException("Not authorized to delete requested job application."));

        jobApplicationRepository.deleteById(toDelete.getApplicationId());
    }
}