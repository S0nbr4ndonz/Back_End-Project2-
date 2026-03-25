package com.group7.jobTrackerApplication.controller;

import com.group7.jobTrackerApplication.DTO.CreateApplicationNoteRequest;
import com.group7.jobTrackerApplication.DTO.GetApplicationNoteSummary;
import com.group7.jobTrackerApplication.DTO.UpdateApplicationNoteRequest;
import com.group7.jobTrackerApplication.model.ApplicationNote;
import com.group7.jobTrackerApplication.service.ApplicationNotesService;
import com.group7.jobTrackerApplication.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/job-applications/{applicationId}/note")
public class ApplicationNoteController {

    private final ApplicationNotesService applicationNotesService;
    private final UserService userService;

    public ApplicationNoteController(ApplicationNotesService applicationNotesService, UserService userService){
        this.applicationNotesService = applicationNotesService;
        this.userService = userService;
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<GetApplicationNoteSummary> getNoteById(@AuthenticationPrincipal OAuth2User principal,  @PathVariable Long noteId, @PathVariable Long applicationId){
        ApplicationNote note = applicationNotesService.getNoteById(noteId, applicationId, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.ok(toNoteSummary(note));
    }

    @PostMapping
    public ResponseEntity<GetApplicationNoteSummary> create(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody CreateApplicationNoteRequest request
    ){
        ApplicationNote created = applicationNotesService.create(request.jobApplication().getApplicationId(), request, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(toNoteSummary(created));
    }

    @PatchMapping("/{noteId}")
    public ResponseEntity<GetApplicationNoteSummary> patch(@PathVariable Long noteId, @Valid @RequestBody UpdateApplicationNoteRequest request, @AuthenticationPrincipal OAuth2User principal){
        ApplicationNote updated = applicationNotesService.patch( request.application().getApplicationId(), noteId, request , userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.ok(toNoteSummary(updated));
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<ApplicationNote> delete(@PathVariable Long noteId, @PathVariable Long applicationId,  @AuthenticationPrincipal OAuth2User principal){
        applicationNotesService.delete(noteId, applicationId,  userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.noContent().build();
    }

    private GetApplicationNoteSummary toNoteSummary(ApplicationNote note) {
        return new GetApplicationNoteSummary(
                note.getNotesId(),
                note.getApplicationId(),
                note.getApplication().getJobEntry().getJobTitle(),
                note.getApplication().getJobEntry().getCompanyName(),
                note.getApplication().getStatus(),
                note.getLastEdited(),
                note.getContent()
        );
    }
}
