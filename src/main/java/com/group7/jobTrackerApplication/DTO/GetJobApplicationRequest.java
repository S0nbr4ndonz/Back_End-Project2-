package com.group7.jobTrackerApplication.DTO;

public record GetJobApplicationRequest(Long applicationId, Long jobId, String jobTitle) {
}
