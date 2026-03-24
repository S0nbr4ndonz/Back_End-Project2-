package com.group7.jobTrackerApplication.controller;

import com.group7.jobTrackerApplication.DTO.CreateApplicationNoteRequest;
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
    public ResponseEntity<ApplicationNote> getNoteById(@AuthenticationPrincipal OAuth2User principal,  @PathVariable Long noteId, @PathVariable Long applicationId){
        return ResponseEntity.ok(applicationNotesService.getNoteById(noteId, applicationId, userService.getOrCreateFromOAuth(principal) ));
    }

    @PostMapping
    public ResponseEntity<ApplicationNote> create(@AuthenticationPrincipal OAuth2User principal, @PathVariable Long applicationId,@RequestBody CreateApplicationNoteRequest request){
        ApplicationNote created = applicationNotesService.create(request, applicationId, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{noteId}")
    public ResponseEntity<ApplicationNote> patch(@PathVariable Long applicationId, @PathVariable Long noteId, @Valid @RequestBody UpdateApplicationNoteRequest request, @AuthenticationPrincipal OAuth2User principal){
        ApplicationNote updated = applicationNotesService.patch(applicationId, noteId, request , userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<ApplicationNote> delete(@PathVariable Long noteId, @PathVariable Long applicationId,  @AuthenticationPrincipal OAuth2User principal){
        applicationNotesService.delete(noteId, applicationId,  userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.noContent().build();
    }
}