package com.group7.jobTrackerApplication.controller;

import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.repository.JobApplicationRepository;
import com.group7.jobTrackerApplication.service.UserService;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
public class AuthController {

    private final UserService userService;
    private final JobApplicationRepository jobApplicationRepository;

    public AuthController(UserService userService, JobApplicationRepository jobApplicationRepository) {
        this.userService = userService;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    @GetMapping("/api/me")
    public Map<String, Object> me(@AuthenticationPrincipal OAuth2User user) {
        User dbUser = userService.getOrCreateFromOAuth(user);
        long applicationCount = jobApplicationRepository.countByUser_UserId(dbUser.getUserId());

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
    }

    @DeleteMapping("/api/me")
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal OAuth2User user) {
        User dbUser = userService.getOrCreateFromOAuth(user);
        userService.delete(dbUser.getUserId());
        return ResponseEntity.noContent().build();
    }
}
