package com.group7.jobTrackerApplication.DTO;

/**
 * Request payload for updating a job entry.
 */
public record UpdateJobEntryRequest(String companyName, String jobTitle, String salaryText, String postingURL) {
}
