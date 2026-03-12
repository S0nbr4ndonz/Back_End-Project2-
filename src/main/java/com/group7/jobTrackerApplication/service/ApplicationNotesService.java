package com.group7.jobTrackerApplication.service;

import com.group7.jobTrackerApplication.DTO.CreateApplicationNoteRequest;
import com.group7.jobTrackerApplication.DTO.UpdateApplicationNoteRequest;
import com.group7.jobTrackerApplication.model.ApplicationNote;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.repository.ApplicationNoteRepository;
import org.springframework.stereotype.Service;
import com.group7.jobTrackerApplication.exception.ResourceNotFoundException;
import com.group7.jobTrackerApplication.exception.ForbiddenException;

@Service
public class ApplicationNotesService {

    private final ApplicationNoteRepository applicationNoteRepository;

    public ApplicationNotesService(ApplicationNoteRepository applicationNoteRepository) {
        this.applicationNoteRepository = applicationNoteRepository;
    }

    public ApplicationNote getNoteById(Long noteId, Long applicationId, User user) {
        return applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(noteId, applicationId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Application Note not found"));
    }

    public ApplicationNote create(CreateApplicationNoteRequest request, User user) {

        ApplicationNote ap = new ApplicationNote();
        ap.setContent(request.content());
        ap.setLastEdited(request.lastEdited());
        ap.setApplicationId();

        return applicationNoteRepository.save(ap);
    }

    public ApplicationNote patch(Long notesId, UpdateApplicationNoteRequest request, User user) {

        ApplicationNote toChange = applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(notesId, request.application().getApplicationId(), user.getUserId())
                .orElseThrow(()-> new ForbiddenException("Not authorized to update this note"));

        if(request.lastEdited() != null) toChange.setLastEdited(request.lastEdited());
        if(request.content() != null) toChange.setContent(request.content());

        return applicationNoteRepository.save(toChange);
    }

    public void delete(Long noteId, Long applicationId,  User user) {
        ApplicationNote toDelete = applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(noteId, applicationId, user.getUserId())
                .orElseThrow(() -> new ForbiddenException("Not authorized to delete this note"));

        applicationNoteRepository.deleteById(toDelete.getNotesId());
    }
}