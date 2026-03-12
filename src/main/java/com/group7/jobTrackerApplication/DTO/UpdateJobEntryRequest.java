package com.group7.jobTrackerApplication.DTO;

public record UpdateJobEntryRequest(String company, String jobTitle, String salary, String postingUrl) {
}
