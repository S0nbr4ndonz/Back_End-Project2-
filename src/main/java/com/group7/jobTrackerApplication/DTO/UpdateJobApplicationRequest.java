package com.group7.jobTrackerApplication.DTO;

import java.time.LocalDate;

public record UpdateJobApplicationRequest(Long jobId, String status, LocalDate dateApplied) {
}
