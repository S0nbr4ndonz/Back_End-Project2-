package com.group7.jobTrackerApplication.DTO;

import com.group7.jobTrackerApplication.model.JobApplication;

import java.time.LocalDateTime;

public record UpdateApplicationNoteRequest(String content, JobApplication application) {
}
