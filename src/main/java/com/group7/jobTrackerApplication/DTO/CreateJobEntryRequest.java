package com.group7.jobTrackerApplication.DTO;

public record CreateJobEntryRequest(
        String companyName,
        String salaryText,
        String postingURL,
        String jobTitle
) {}
