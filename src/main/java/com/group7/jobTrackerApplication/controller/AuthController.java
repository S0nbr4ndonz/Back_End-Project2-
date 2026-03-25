package com.group7.jobTrackerApplication.controller;

import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.service.UserService;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/me")
    public Map<String, Object> me(@AuthenticationPrincipal OAuth2User user) {
        User dbUser = userService.getOrCreateFromOAuth(user);

        return Map.of(
                "name", user.getAttribute("name"),
                "login", user.getAttribute("login"),
                "email", user.getAttribute("email"),
                "role", dbUser.getRole().name(),
                "authorities", user.getAuthorities().stream()
                        .map(grantedAuthority -> grantedAuthority.getAuthority())
                        .sorted()
                        .collect(Collectors.toList()),
                "attributes", user.getAttributes()
        );
    }
}
