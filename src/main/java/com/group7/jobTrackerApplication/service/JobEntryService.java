package com.group7.jobTrackerApplication.service;

import com.group7.jobTrackerApplication.DTO.CreateJobEntryRequest;
import com.group7.jobTrackerApplication.DTO.UpdateJobEntryRequest;
import com.group7.jobTrackerApplication.model.JobEntry;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.repository.JobEntryRepository;
import com.group7.jobTrackerApplication.exception.ResourceNotFoundException;
import com.group7.jobTrackerApplication.exception.ForbiddenException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobEntryService {

    private final JobEntryRepository jobEntryRepository;
    private final UserService userService;

    public JobEntryService(JobEntryRepository jobEntryRepository, UserService userService) {
        this.jobEntryRepository = jobEntryRepository;
        this.userService = userService;
    }

    public List<JobEntry> getAll( User user ) {
        return jobEntryRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Job entries not found"));
    }

    public JobEntry getById(Long jobId, User user) {
        return jobEntryRepository
                .findByJobIdAndUserId(jobId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Job entry not found"));
    }

    public JobEntry create(OAuth2User principal, CreateJobEntryRequest request) {
        User user = userService.getOrCreateFromOAuth(principal);

        JobEntry je = new JobEntry();
        je.setCompanyName(request.CompanyName());
        je.setJobTitle(request.JobTitle());
        je.setSalaryText(request.SalaryText());
        je.setPostingURL(request.PostingUrl());
        je.setUserId(user.getUserId());

        return jobEntryRepository.save(je);
    }

    public JobEntry replace(Long jobId, UpdateJobEntryRequest request, User user) {
        JobEntry toChange = jobEntryRepository
                        .findByJobIdAndUserId(jobId, user.getUserId())
                        .orElseThrow(() -> new ForbiddenException("Not authorized to update this job entry"));

        toChange.setCompanyName(request.company());
        toChange.setJobTitle(request.jobTitle());
        toChange.setPostingURL(request.postingUrl());
        toChange.setSalaryText(request.salary());

        return jobEntryRepository.save(toChange);
    }

    public JobEntry patch(Long jobId, UpdateJobEntryRequest request, User user) {
        JobEntry toChange = jobEntryRepository.findByJobIdAndUserId(jobId, user.getUserId())
                .orElseThrow(() -> new ForbiddenException("Not authorized to update this job entry"));

        if (request.company() != null) toChange.setCompanyName(request.company());
        if (request.jobTitle() != null) toChange.setJobTitle(request.jobTitle());
        if (request.salary() != null) toChange.setSalaryText(request.salary());
        if (request.postingUrl() != null) toChange.setPostingURL(request.postingUrl());

        return jobEntryRepository.save(toChange);
    }

    public void delete(Long jobId, User user) {
        JobEntry toDelete = jobEntryRepository.findByJobIdAndUserId(jobId, user.getUserId())
                        .orElseThrow(() -> new ForbiddenException("Not authorized to delete this job entry"));

        jobEntryRepository.deleteById(toDelete.getJobId());
    }
}