package com.group7.jobTrackerApplication.service;

import com.group7.jobTrackerApplication.DTO.CreateJobEntryRequest;
import com.group7.jobTrackerApplication.DTO.GetJobEntryRequest;
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

    public List<GetJobEntryRequest> getAll( User user ) {
        List<JobEntry> entries = jobEntryRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Job entries not found"));

        return entries.stream()
                .map(entry -> new GetJobEntryRequest(
                        entry.getCompanyName(),
                        entry.getJobTitle(),
                        entry.getSalaryText(),
                        entry.getPostingURL()
                )).toList();
    }

    public GetJobEntryRequest getById(Long jobId, User user) {
        JobEntry entry = jobEntryRepository
                .findByJobIdAndUser_UserId(jobId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Job entry not found"));

        return new GetJobEntryRequest(
                entry.getCompanyName(),
                entry.getJobTitle(),
                entry.getSalaryText(),
                entry.getPostingURL()
        );
    }

    public JobEntry create(OAuth2User principal, CreateJobEntryRequest request) {
        User user = userService.getOrCreateFromOAuth(principal);

        JobEntry je = new JobEntry();
        je.setCompanyName(request.companyName());
        je.setJobTitle(request.jobTitle());
        je.setSalaryText(request.salaryText());
        je.setPostingURL(request.postingURL());
        je.setUser(user);

        return jobEntryRepository.save(je);
    }

    public JobEntry replace(Long jobId, UpdateJobEntryRequest request, User user) {
        JobEntry toChange = jobEntryRepository
                        .findByJobIdAndUser_UserId(jobId, user.getUserId())
                        .orElseThrow(() -> new ForbiddenException("Not authorized to update this job entry"));

        toChange.setCompanyName(request.companyName());
        toChange.setJobTitle(request.jobTitle());
        toChange.setPostingURL(request.postingURL());
        toChange.setSalaryText(request.salaryText());

        return jobEntryRepository.save(toChange);
    }

    public JobEntry patch(Long jobId, UpdateJobEntryRequest request, User user) {
        JobEntry toChange = jobEntryRepository.findByJobIdAndUser_UserId(jobId, user.getUserId())
                .orElseThrow(() -> new ForbiddenException("Not authorized to update this job entry"));

        if (request.companyName() != null) toChange.setCompanyName(request.companyName());
        if (request.jobTitle() != null) toChange.setJobTitle(request.jobTitle());
        if (request.salaryText() != null) toChange.setSalaryText(request.salaryText());
        if (request.postingURL() != null) toChange.setPostingURL(request.postingURL());

        return jobEntryRepository.save(toChange);
    }

    public void delete(Long jobId, User user) {
        JobEntry toDelete = jobEntryRepository.findByJobIdAndUser_UserId(jobId, user.getUserId())
                        .orElseThrow(() -> new ForbiddenException("Not authorized to delete this job entry"));

        jobEntryRepository.deleteById(toDelete.getJobId());
    }
}