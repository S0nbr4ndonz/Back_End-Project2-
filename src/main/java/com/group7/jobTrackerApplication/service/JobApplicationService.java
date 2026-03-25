package com.group7.jobTrackerApplication.service;

import com.group7.jobTrackerApplication.DTO.CreateJobApplicationRequest;
import com.group7.jobTrackerApplication.DTO.GetJobApplicationRequest;
import com.group7.jobTrackerApplication.DTO.UpdateJobApplicationRequest;
import com.group7.jobTrackerApplication.model.JobApplication;
import com.group7.jobTrackerApplication.model.JobEntry;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.repository.JobApplicationRepository;
import com.group7.jobTrackerApplication.repository.JobEntryRepository;
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
    private final JobEntryRepository jobEntryRepository;

    public JobApplicationService(JobApplicationRepository jobApplicationRepository, JobEntryRepository jobEntryRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobEntryRepository = jobEntryRepository;
    }

    public List<GetJobApplicationRequest> getAll(User user) {
        List<JobApplication> applications = jobApplicationRepository.findAllByUser_UserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(("Job applications not found")));

        return applications.stream()
                .map( app -> new GetJobApplicationRequest(
                        app.getApplicationId(),
                        app.getJobEntry().getJobId(),
                        app.getJobEntry().getJobTitle(),
                        app.getStatus(),
                        app.getDateApplied(),
                        app.getNote() == null ? null : app.getNote().getNotesId()
                )).toList();
    }

    public GetJobApplicationRequest getById(Long applicationId, User user) {
        JobApplication application = jobApplicationRepository.findByApplicationIdAndUser_UserId(applicationId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found"));

        return new GetJobApplicationRequest(
                application.getApplicationId(),
                application.getJobEntry().getJobId(),
                application.getJobEntry().getJobTitle(),
                application.getStatus(),
                application.getDateApplied(),
                application.getNote() == null ? null : application.getNote().getNotesId()
        ) ;
    }

    public JobApplication create(CreateJobApplicationRequest jobApplication, User user) {
        JobEntry jobEntry = getOwnedJobEntry(jobApplication.jobId(), user);

        JobApplication jp = new JobApplication();
        jp.setDateApplied(jobApplication.dateApplied());
        jp.setStatus(jobApplication.status());
        jp.setUser(user);
        jp.setJobEntry(jobEntry);

        return jobApplicationRepository.save(jp);
    }

    public JobApplication replace(Long applicationId, UpdateJobApplicationRequest jobApplication, User user) {
        JobApplication toChange = jobApplicationRepository.findByApplicationIdAndUser_UserId(applicationId, user.getUserId())
                .orElseThrow(()-> new ForbiddenException("Not authorized to update requested job application."));

        toChange.setDateApplied(jobApplication.dateApplied());
        toChange.setStatus(jobApplication.status());
        toChange.setJobEntry(getOwnedJobEntry(jobApplication.jobId(), user));

        return jobApplicationRepository.save(toChange);
    }

    public JobApplication patch(Long applicationId, UpdateJobApplicationRequest request, User user) {
        JobApplication toChange = jobApplicationRepository.findByApplicationIdAndUser_UserId(applicationId, user.getUserId())
                .orElseThrow(()-> new ForbiddenException("Not authorized to update requested job application."));

        if(request.dateApplied() != null) toChange.setDateApplied(request.dateApplied());
        if(request.status() != null) toChange.setStatus(request.status());
        if(request.jobId() != null) toChange.setJobEntry(getOwnedJobEntry(request.jobId(), user));

        return jobApplicationRepository.save(toChange);
    }

    public void delete(Long applicationId, User user) {
        JobApplication toDelete = jobApplicationRepository.findByApplicationIdAndUser_UserId(applicationId, user.getUserId())
                        .orElseThrow(() -> new ForbiddenException("Not authorized to delete requested job application."));

        jobApplicationRepository.deleteById(toDelete.getApplicationId());
    }

    private JobEntry getOwnedJobEntry(Long jobId, User user) {
        return jobEntryRepository.findByJobIdAndUser_UserId(jobId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Job entry not found"));
    }
}
