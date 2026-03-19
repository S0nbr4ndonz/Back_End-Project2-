package com.group7.jobTrackerApplication.DTO;

public record UpdateJobEntryRequest(String companyName, String jobTitle, String salaryText, String postingURL) {
}
