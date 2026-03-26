package com.group7.jobTrackerApplication.controller;

import com.group7.jobTrackerApplication.DTO.CreateApplicationNoteRequest;
import com.group7.jobTrackerApplication.DTO.GetApplicationNoteSummary;
import com.group7.jobTrackerApplication.DTO.UpdateApplicationNoteRequest;
import com.group7.jobTrackerApplication.model.ApplicationNote;
import com.group7.jobTrackerApplication.service.ApplicationNotesService;
import com.group7.jobTrackerApplication.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes note endpoints nested under job applications for the authenticated user.
 */
@RestController
@RequestMapping("/job-applications/{applicationId}/note")
@Tag(name = "Application Notes", description = "Operations for attaching notes to tracked job applications")
public class ApplicationNoteController {

    private final ApplicationNotesService applicationNotesService;
    private final UserService userService;

    /**
     * Creates a new controller with the required note and user services.
     *
     * @param applicationNotesService service responsible for application note logic
     * @param userService service used to resolve the authenticated user
     */
    public ApplicationNoteController(ApplicationNotesService applicationNotesService, UserService userService){
        this.applicationNotesService = applicationNotesService;
        this.userService = userService;
    }

    @GetMapping("/{noteId}")
    @Operation(summary = "Get an application note", description = "Returns a note summary for the authenticated user's application.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Application note not found")
    })
    public ResponseEntity<GetApplicationNoteSummary> getNoteById(@AuthenticationPrincipal OAuth2User principal,  @PathVariable Long noteId, @PathVariable Long applicationId){
        ApplicationNote note = applicationNotesService.getNoteById(noteId, applicationId, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.ok(toNoteSummary(note));
    }

    @PostMapping
    @Operation(summary = "Create an application note", description = "Creates or upserts a note for a job application owned by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Note created"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User is not allowed to create a note for this application")
    })
    public ResponseEntity<GetApplicationNoteSummary> create(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody CreateApplicationNoteRequest request
    ){
        ApplicationNote created = applicationNotesService.create(request.jobApplication().getApplicationId(), request, userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(toNoteSummary(created));
    }

    @PatchMapping("/{noteId}")
    @Operation(summary = "Update an application note", description = "Partially updates a note for a job application owned by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note updated"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User is not allowed to update this note")
    })
    public ResponseEntity<GetApplicationNoteSummary> patch(@PathVariable Long noteId, @Valid @RequestBody UpdateApplicationNoteRequest request, @AuthenticationPrincipal OAuth2User principal){
        ApplicationNote updated = applicationNotesService.patch( request.application().getApplicationId(), noteId, request , userService.getOrCreateFromOAuth(principal));
        return ResponseEntity.ok(toNoteSummary(updated));
    }

    @DeleteMapping("/{noteId}")
    @Operation(summary = "Delete an application note", description = "Deletes a note belonging to the authenticated user's application.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Note deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User is not allowed to delete this note")
    })
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
