package com.group7.jobTrackerApplication.DTO;


import java.time.LocalDateTime;

public record ApplicationNoteRequest (Long notesId, String jobTitle, String company, String status, String content, LocalDateTime lastEdited) {
}
