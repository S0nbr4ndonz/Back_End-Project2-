package com.group7.jobTrackerApplication.DTO;


import java.time.LocalDateTime;

/**
 * Response payload describing an application note.
 */
public record ApplicationNoteRequest (Long notesId, String jobTitle, String company, String status, String content, LocalDateTime lastEdited) {
}
