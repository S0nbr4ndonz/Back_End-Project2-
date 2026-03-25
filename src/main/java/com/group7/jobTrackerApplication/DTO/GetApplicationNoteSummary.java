package com.group7.jobTrackerApplication.DTO;

import java.time.LocalDateTime;

public record GetApplicationNoteSummary(
        Long notesId,
        Long applicationId,
        String jobTitle,
        String company,
        String status,
        LocalDateTime lastEdited,
        String content
) {}

