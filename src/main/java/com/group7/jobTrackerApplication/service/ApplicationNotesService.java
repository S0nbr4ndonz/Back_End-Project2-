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

    public List<ApplicationNoteRequest> getAllNotes(User user) {

        List<ApplicationNote> notes =
                applicationNoteRepository.findByApplication_User_UserId(user.getUserId());

        return notes.stream()
                .map(note -> new ApplicationNoteRequest(
                        note.getNotesId(),
                        note.getApplication().getJobEntry().getJobTitle(),
                        note.getApplication().getJobEntry().getCompanyName(),
                        note.getApplication().getStatus().toString(),
                        note.getContent(),
                        note.getLastEdited()
                ))
                .toList();
    }

    public ApplicationNote getNoteById(Long noteId, Long applicationId, User user) {
        return applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(noteId, applicationId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Application Note not found"));
    }

    public List<GetApplicationNoteSummary> getAllNotes(User user) {
        return applicationNoteRepository.findAllByApplication_User_UserId(user.getUserId())
                .stream()
                .sorted(
                        Comparator.comparing(
                                ApplicationNote::getLastEdited,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ).reversed()
                )
                .map(note -> new GetApplicationNoteSummary(
                        note.getNotesId(),
                        note.getApplicationId(),
                        note.getApplication().getJobEntry().getJobTitle(),
                        note.getApplication().getJobEntry().getCompanyName(),
                        note.getApplication().getStatus(),
                        note.getLastEdited(),
                        note.getContent()
                ))
                .toList();
    }



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

    public ApplicationNote patch(Long applicationId, Long notesId, UpdateApplicationNoteRequest request, User user) {

        ApplicationNote toChange = applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(notesId, request.application().getApplicationId(), user.getUserId())
                .orElseThrow(()-> new ForbiddenException("Not authorized to update this note"));

        if(request.content() != null) toChange.setContent(request.content());

        return applicationNoteRepository.save(toChange);
    }

    public void delete(Long noteId, Long applicationId,  User user) {
        ApplicationNote toDelete = applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(noteId, applicationId, user.getUserId())
                .orElseThrow(() -> new ForbiddenException("Not authorized to delete this note"));

        applicationNoteRepository.deleteById(toDelete.getNotesId());
    }
}