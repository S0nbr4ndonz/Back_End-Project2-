package com.group7.jobTrackerApplication.service;

import com.group7.jobTrackerApplication.DTO.UpdateUserRoleRequest;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.model.Role;
import com.group7.jobTrackerApplication.repository.UserRepository;
import com.group7.jobTrackerApplication.exception.ResourceNotFoundException;
import com.group7.jobTrackerApplication.exception.NotAuthenticatedException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public User update(Long userId, UpdateUserRoleRequest request) {
        User user = getUserById(userId);
        user.setRole(request.role());
        return userRepository.save(user);
    }

    public User delete(Long userId) {
        User user = getUserById(userId);
        userRepository.deleteById(userId);
        return user;
    }

    public User getOrCreateFromOAuth(OAuth2User principal) {
        if (principal == null) {
            throw new NotAuthenticatedException("Authentication required");
        }

        Map<String, Object> attrs = principal.getAttributes();

        if (attrs.get("id") == null) {
            throw new NotAuthenticatedException("Token expired or invalid");
        }

        String provider = "github";
        String subject = attrs.get("id").toString();
        String username = (String) attrs.get("login");
        String email = (String) attrs.get("email");

        return userRepository
                .findByOauthProviderAndOauthSubject(provider, subject)
                .orElseGet(() -> {
                    User u = new User();
                    u.setOauthProvider(provider);
                    u.setOauthSubject(subject);
                    u.setUsername(username);
                    u.setEmail(email);
                    u.setRole(Role.USER);
                    return userRepository.save(u);
                });
    }
}