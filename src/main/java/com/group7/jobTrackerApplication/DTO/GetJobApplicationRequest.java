package com.group7.jobTrackerApplication.DTO;

import java.time.LocalDate;

public record GetJobApplicationRequest(
        Long applicationId,
        Long jobId,
        String companyName,
        String jobTitle,
        String status,
        LocalDate dateApplied,
        Long notesId
) {
}
