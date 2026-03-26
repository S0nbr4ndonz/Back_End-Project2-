package com.group7.jobTrackerApplication.DTO;

/**
 * Request payload for creating a job entry.
 */
public record CreateJobEntryRequest(
        String companyName,
        String salaryText,
        String postingURL,
        String jobTitle
) {}
