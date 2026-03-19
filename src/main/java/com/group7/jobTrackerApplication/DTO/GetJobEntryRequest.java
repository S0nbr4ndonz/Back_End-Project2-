package com.group7.jobTrackerApplication.DTO;

import java.time.LocalDate;

public record GetJobEntryRequest(String companyName, String jobTitle, String salaryText, String postingURL) {
}
