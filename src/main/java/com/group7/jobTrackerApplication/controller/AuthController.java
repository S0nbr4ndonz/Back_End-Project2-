package com.group7.jobTrackerApplication.controller;

import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.repository.JobApplicationRepository;
import com.group7.jobTrackerApplication.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

/**
 * Exposes authentication helper endpoints for the currently logged-in user.
 */
@RestController
@Tag(name = "Authentication", description = "Endpoints for inspecting and managing the current authenticated session")
public class AuthController {

    private final UserService userService;
    private final JobApplicationRepository jobApplicationRepository;

    /**
     * Creates a new auth controller with the required collaborators.
     *
     * @param userService service used to resolve3 or create the current user record
     * @param jobApplicationRepository repository used to count current user applications
     */
    /**
     * Creates a new auth controller with the required collaborators.
     *
     * @param userService service used to resolve or create the current user record
     * @param jobApplicationRepository repository used to count current user applications
     */
    public AuthController(UserService userService, JobApplicationRepository jobApplicationRepository) {
        this.userService = userService;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    @GetMapping("/api/me")
    @Operation(summary = "Get current user", description = "Returns the authenticated user's profile details, authorities, and application count.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current user returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public Map<String, Object> me(@AuthenticationPrincipal OAuth2User user) {
        User dbUser = userService.getOrCreateFromOAuth(user);
        long applicationCount = jobApplicationRepository.countByUser_UserId(dbUser.getUserId());
<<<<<<< HEAD
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("username", dbUser.getUsername());
        response.put("name", user.getAttribute("name"));
        response.put("login", user.getAttribute("login"));
        response.put("email", user.getAttribute("email"));
        response.put("oauthProvider", dbUser.getOauthProvider());
        response.put("role", dbUser.getRole().name());
        response.put("applicationCount", applicationCount);
        response.put("authorities", user.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .sorted()
                .collect(Collectors.toList()));
        response.put("attributes", user.getAttributes());
        return response;
=======

        return Map.of(
                "userId", dbUser.getUserId(),
                "username", dbUser.getUsername(),
                "name", user.getAttribute("name"),
                "login", user.getAttribute("login"),
                "email", user.getAttribute("email"),
                "oauthProvider", dbUser.getOauthProvider(),
                "role", dbUser.getRole().name(),
                "applicationCount", applicationCount,
                "authorities", user.getAuthorities().stream()
                        .map(grantedAuthority -> grantedAuthority.getAuthority())
                        .sorted()
                        .collect(Collectors.toList()),
                "attributes", user.getAttributes()
        );
>>>>>>> 64c7059fb9bb29f95889faf5c439a24c8ac207ae
    }

    @DeleteMapping("/api/me")
    @Operation(summary = "Delete current user", description = "Deletes the authenticated user's account and associated data.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Current user deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal OAuth2User user) {
        User dbUser = userService.getOrCreateFromOAuth(user);
        userService.delete(dbUser.getUserId());
        return ResponseEntity.noContent().build();
    }
}
