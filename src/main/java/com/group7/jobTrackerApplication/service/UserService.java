package com.group7.jobTrackerApplication.service;

import com.group7.jobTrackerApplication.DTO.UpdateUserRoleRequest;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.model.Role;
import com.group7.jobTrackerApplication.repository.UserRepository;
import com.group7.jobTrackerApplication.exception.ResourceNotFoundException;
import com.group7.jobTrackerApplication.exception.NotAuthenticatedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates user lookup, role updates, deletion, and OAuth-backed user provisioning.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final String adminGithubLogin;

    public UserService(
            UserRepository userRepository,
            @Value("${app.admin.github-login:}") String adminGithubLogin
    ) {
        this.userRepository = userRepository;
        this.adminGithubLogin = adminGithubLogin;
    }

    /**
     * Returns all users in the system.
     *
     * @return all persisted users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Returns a user by id.
     *
     * @param userId identifier of the requested user
     * @return the matching user
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    /**
     * Updates the role of an existing user.
     *
     * @param userId identifier of the user to update
     * @param request desired role change
     * @return the updated user
     */
    public User update(Long userId, UpdateUserRoleRequest request) {
        User user = getUserById(userId);
        user.setRole(request.role());
        return userRepository.save(user);
    }

    /**
     * Deletes a user by id.
     *
     * @param userId identifier of the user to delete
     * @return the deleted user record
     */
    public User delete(Long userId) {
        User user = getUserById(userId);
        userRepository.deleteById(userId);
        return user;
    }

    /**
     * Resolves the current OAuth principal to a persisted user, creating one if necessary.
     *
     * @param principal authenticated OAuth principal
     * @return existing or newly created user record
     */
    public User getOrCreateFromOAuth(OAuth2User principal) {
        if (principal == null) {
            throw new NotAuthenticatedException("Authentication required");
        }

        Map<String, Object> attrs = principal.getAttributes();
        String provider = resolveProvider(attrs);
        String subject = resolveSubject(provider, attrs);
        String username = resolveUsername(provider, attrs);
        String email = (String) attrs.get("email");

        return userRepository
                .findByOauthProviderAndOauthSubject(provider, subject)
                .map(existingUser -> {
                    Role desiredRole = resolveRole(provider, username);
                    if (existingUser.getRole() != desiredRole) {
                        existingUser.setRole(desiredRole);
                    }
                    existingUser.setUsername(username);
                    existingUser.setEmail(email);
                    existingUser.setOauthProvider(provider);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    User u = new User();
                    u.setOauthProvider(provider);
                    u.setOauthSubject(subject);
                    u.setUsername(username);
                    u.setEmail(email);
                    u.setRole(resolveRole(provider, username));
                    return userRepository.save(u);
                });
    }

    private String resolveProvider(Map<String, Object> attrs) {
        if (attrs.get("sub") != null) {
            return "google";
        }
        if (attrs.get("id") != null || attrs.get("login") != null) {
            return "github";
        }
        throw new NotAuthenticatedException("Unsupported OAuth provider");
    }

    private String resolveSubject(String provider, Map<String, Object> attrs) {
        Object subject = "google".equals(provider) ? attrs.get("sub") : attrs.get("id");
        if (subject == null) {
            throw new NotAuthenticatedException("Token expired or invalid");
        }
        return subject.toString();
    }

    private String resolveUsername(String provider, Map<String, Object> attrs) {
        if ("google".equals(provider)) {
            String email = (String) attrs.get("email");
            if (StringUtils.hasText(email)) {
                return email;
            }

            String name = (String) attrs.get("name");
            if (StringUtils.hasText(name)) {
                return name;
            }

            throw new NotAuthenticatedException("Google account is missing a usable username");
        }

        String githubLogin = (String) attrs.get("login");
        if (!StringUtils.hasText(githubLogin)) {
            throw new NotAuthenticatedException("GitHub account is missing a login");
        }
        return githubLogin;
    }

    private Role resolveRole(String provider, String username) {
        if ("github".equals(provider)
                && StringUtils.hasText(adminGithubLogin)
                && adminGithubLogin.equalsIgnoreCase(username)) {
            return Role.ADMIN;
        }
        return Role.USER;
    }
}
