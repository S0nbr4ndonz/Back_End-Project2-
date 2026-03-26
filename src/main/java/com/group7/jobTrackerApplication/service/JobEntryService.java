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

/**
 * Handles business logic for job entry resources owned by a user.
 */
@Service
public class JobEntryService {

    private final JobEntryRepository jobEntryRepository;
    private final UserService userService;

    public JobEntryService(JobEntryRepository jobEntryRepository, UserService userService) {
        this.jobEntryRepository = jobEntryRepository;
        this.userService = userService;
    }

    /**
     * Returns all job entries owned by the provided user.
     *
     * @param user owner of the requested job entries
     * @return summary DTOs for the user's job entries
     */
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

    /**
     * Returns a single job entry owned by the provided user.
     *
     * @param jobId identifier of the requested job entry
     * @param user owner of the requested job entry
     * @return summary DTO for the matching job entry
     */
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

    /**
     * Creates a new job entry for the authenticated principal.
     *
     * @param principal authenticated OAuth principal
     * @param request request payload for the new job entry
     * @return created job entry
     */
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

    /**
     * Fully replaces a job entry owned by the provided user.
     *
     * @param jobId identifier of the job entry to replace
     * @param request full replacement payload
     * @param user owner of the job entry
     * @return updated job entry
     */
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

    /**
     * Partially updates a job entry owned by the provided user.
     *
     * @param jobId identifier of the job entry to update
     * @param request partial update payload
     * @param user owner of the job entry
     * @return updated job entry
     */
    public JobEntry patch(Long jobId, UpdateJobEntryRequest request, User user) {
        JobEntry toChange = jobEntryRepository.findByJobIdAndUser_UserId(jobId, user.getUserId())
                .orElseThrow(() -> new ForbiddenException("Not authorized to update this job entry"));

        if (request.companyName() != null) toChange.setCompanyName(request.companyName());
        if (request.jobTitle() != null) toChange.setJobTitle(request.jobTitle());
        if (request.salaryText() != null) toChange.setSalaryText(request.salaryText());
        if (request.postingURL() != null) toChange.setPostingURL(request.postingURL());

        return jobEntryRepository.save(toChange);
    }

    /**
     * Deletes a job entry owned by the provided user.
     *
     * @param jobId identifier of the job entry to delete
     * @param user owner of the job entry
     */
    public void delete(Long jobId, User user) {
        JobEntry toDelete = jobEntryRepository.findByJobIdAndUser_UserId(jobId, user.getUserId())
                        .orElseThrow(() -> new ForbiddenException("Not authorized to delete this job entry"));

        jobEntryRepository.deleteById(toDelete.getJobId());
    }
}
