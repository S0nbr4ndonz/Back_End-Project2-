package com.group7.jobTrackerApplication.controller;

import com.group7.jobTrackerApplication.DTO.GetApplicationNoteSummary;

import com.group7.jobTrackerApplication.DTO.ApplicationNoteRequest;
import com.group7.jobTrackerApplication.service.ApplicationNotesService;
import com.group7.jobTrackerApplication.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/job-applications/notes")
public class ApplicationNotesOverviewController {

    private final ApplicationNotesService applicationNotesService;
    private final UserService userService;

    public ApplicationNotesOverviewController(
            ApplicationNotesService applicationNotesService,
            UserService userService
    ) {
        this.applicationNotesService = applicationNotesService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<GetApplicationNoteSummary>> getAll(@AuthenticationPrincipal OAuth2User principal) {
        return ResponseEntity.ok(
                applicationNotesService.getAllNotes(userService.getOrCreateFromOAuth(principal))
        );
    }
}

    @GetMapping("/notes")
    public ResponseEntity<List<ApplicationNoteRequest>> getAllNotes(
            @AuthenticationPrincipal OAuth2User principal) {

        return ResponseEntity.ok(
                applicationNotesService.getAllNotes(
                        userService.getOrCreateFromOAuth(principal)
                )
        );
    }
}
