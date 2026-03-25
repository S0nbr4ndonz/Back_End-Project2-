package com.group7.jobTrackerApplication.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "application_notes")
public class ApplicationNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notes_id")
    private Long notesId;

    @Column(name = "content")
    private String content;

    @Column(name = "last_edited")
    private LocalDateTime lastEdited;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private JobApplication application;

    public JobApplication getApplication() {
        return application;
    }

    public Long getNotesId() { return notesId; }
    public void setNotesId(Long notesId) { this.notesId = notesId; }

    public Long getApplicationId() { return application.getApplicationId(); }
    public void setApplicationId() {
        // Legacy helper; prefer setting the full relationship via setApplication(...)
        if (this.application != null) {
            this.application.setApplicationId(application.getApplicationId());
        }
    }

    public void setApplication(JobApplication application) { this.application = application; }
    public JobApplication getApplication() { return application; }


    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getLastEdited() { return lastEdited; }
    public void setLastEdited(LocalDateTime lastEdited) { this.lastEdited = lastEdited; }
}