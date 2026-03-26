package com.group7.jobTrackerApplication.service;

import com.group7.jobTrackerApplication.DTO.ApplicationNoteRequest;
import com.group7.jobTrackerApplication.DTO.CreateApplicationNoteRequest;
import com.group7.jobTrackerApplication.DTO.GetApplicationNoteSummary;
import com.group7.jobTrackerApplication.DTO.UpdateApplicationNoteRequest;
import com.group7.jobTrackerApplication.model.JobApplication;
import com.group7.jobTrackerApplication.model.ApplicationNote;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.repository.JobApplicationRepository;
import com.group7.jobTrackerApplication.repository.ApplicationNoteRepository;
import org.springframework.stereotype.Service;
import com.group7.jobTrackerApplication.exception.ResourceNotFoundException;
import com.group7.jobTrackerApplication.exception.ForbiddenException;

import java.util.Comparator;
import java.util.List;

/**
 * Handles business logic for notes attached to a user's job applications.
 */
@Service
public class ApplicationNotesService {

    private final ApplicationNoteRepository applicationNoteRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public ApplicationNotesService(
            ApplicationNoteRepository applicationNoteRepository,
            JobApplicationRepository jobApplicationRepository
    ) {
        this.applicationNoteRepository = applicationNoteRepository;
        this.jobApplicationRepository = jobApplicationRepository;
    }


    /**
     * Returns a single application note owned by the provided user.
     *
     * @param noteId identifier of the requested note
     * @param applicationId identifier of the parent application
     * @param user owner of the application note
     * @return the matching note
     */
    public ApplicationNote getNoteById(Long noteId, Long applicationId, User user) {
        return applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(noteId, applicationId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Application Note not found"));
    }



    /**
     * Creates a note for an owned application or updates the existing one in a one-to-one relationship.
     *
     * @param applicationId identifier of the parent application
     * @param request request payload for the note
     * @param user owner of the application
     * @return created or updated application note
     */
    public ApplicationNote create(Long applicationId, CreateApplicationNoteRequest request, User user) {
        JobApplication jobApplication = jobApplicationRepository
                .findByApplicationIdAndUser_UserId(applicationId, user.getUserId())
                .orElseThrow(() -> new ForbiddenException("Not authorized to create this note"));

        // 1:1 relationship: if a note already exists, treat "create" as an update (upsert)
        ApplicationNote existing = applicationNoteRepository
                .findByApplication_ApplicationIdAndApplication_User_UserId(applicationId, user.getUserId())
                .orElse(null);

        if (existing != null) {
            existing.setContent(request.content());
            existing.setLastEdited(request.lastEdited());
            return applicationNoteRepository.save(existing);
        }

        ApplicationNote ap = new ApplicationNote();
        ap.setContent(request.content());
        ap.setLastEdited(request.lastEdited());
        ap.setApplication(jobApplication);

        return applicationNoteRepository.save(ap);
    }

    /**
     * Partially updates a note owned by the provided user.
     *
     * @param applicationId identifier of the parent application
     * @param notesId identifier of the note to update
     * @param request partial update payload
     * @param user owner of the note
     * @return updated application note
     */
    public ApplicationNote patch(Long applicationId, Long notesId, UpdateApplicationNoteRequest request, User user) {

        ApplicationNote toChange = applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(notesId, request.application().getApplicationId(), user.getUserId())
                .orElseThrow(()-> new ForbiddenException("Not authorized to update this note"));

        if(request.content() != null) toChange.setContent(request.content());

        return applicationNoteRepository.save(toChange);
    }

    /**
     * Deletes a note owned by the provided user.
     *
     * @param noteId identifier of the note to delete
     * @param applicationId identifier of the parent application
     * @param user owner of the note
     */
    public void delete(Long noteId, Long applicationId,  User user) {
        ApplicationNote toDelete = applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(noteId, applicationId, user.getUserId())
                .orElseThrow(() -> new ForbiddenException("Not authorized to delete this note"));

        applicationNoteRepository.deleteById(toDelete.getNotesId());
    }
}
