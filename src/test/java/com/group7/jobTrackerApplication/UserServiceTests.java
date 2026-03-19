package com.group7.jobTrackerApplication;

import com.group7.jobTrackerApplication.DTO.UpdateUserRoleRequest;
import com.group7.jobTrackerApplication.exception.NotAuthenticatedException;
import com.group7.jobTrackerApplication.exception.ResourceNotFoundException;
import com.group7.jobTrackerApplication.model.Role;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.repository.UserRepository;
import com.group7.jobTrackerApplication.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    // -------------------------
    // getAllUsers() -> no exception -> 1 test
    // -------------------------
    @Test
    void getAllUsers_returnsAllUsers() {
        List<User> expected = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(expected);

        List<User> result = userService.getAllUsers();

        assertSame(expected, result);
        verify(userRepository).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    // -------------------------
    // getUserById() -> can throw -> 2 tests
    // -------------------------
    @Test
    void getUserById_whenFound_returnsUser() {
        Long userId = 1L;
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserById(userId);

        assertSame(user, result);
        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUserById_whenMissing_throwsResourceNotFoundException() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    // -------------------------
    // update() -> can throw -> 2 tests
    // -------------------------
    @Test
    void update_whenUserExists_updatesRoleAndSaves() {
        Long userId = 5L;

        User existing = new User();
        existing.setRole(Role.USER);

        // Note: UpdateUserRoleRequest is a record; this assumes it has a constructor like new UpdateUserRoleRequest(Role.ADMIN)
        UpdateUserRoleRequest request = new UpdateUserRoleRequest(Role.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.update(userId, request);

        assertEquals(Role.ADMIN, result.getRole());
        verify(userRepository).findById(userId);
        verify(userRepository).save(existing);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void update_whenUserMissing_throwsResourceNotFoundException() {
        Long userId = 404L;
        UpdateUserRoleRequest request = new UpdateUserRoleRequest(Role.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.update(userId, request));

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    // -------------------------
    // delete() -> can throw -> 2 tests
    // -------------------------
    @Test
    void delete_whenUserExists_deletesAndReturnsUser() {
        Long userId = 7L;
        User existing = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));

        User result = userService.delete(userId);

        assertSame(existing, result);
        verify(userRepository).findById(userId);
        verify(userRepository).deleteById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void delete_whenUserMissing_throwsResourceNotFoundException() {
        Long userId = 8L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.delete(userId));

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    // -------------------------
    // getOrCreateFromOAuth() -> can throw -> 2 tests
    // -------------------------
    @Test
    void getOrCreateFromOAuth_whenNotFound_createsUserWithDefaultsAndSaves() {
        OAuth2User principal = mock(OAuth2User.class);

        Map<String, Object> attrs = Map.of(
                "id", 12345L,
                "login", "octocat",
                "email", "octocat@github.com"
        );
        when(principal.getAttributes()).thenReturn(attrs);

        when(userRepository.findByOauthProviderAndOauthSubject("github", "12345"))
                .thenReturn(Optional.empty());

        // Capture the user being saved so we can assert fields set by service
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        User saved = new User();
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userService.getOrCreateFromOAuth(principal);

        assertSame(saved, result);

        verify(userRepository).findByOauthProviderAndOauthSubject("github", "12345");
        verify(userRepository).save(userCaptor.capture());

        User toSave = userCaptor.getValue();
        assertEquals("github", toSave.getOauthProvider());
        assertEquals("12345", toSave.getOauthSubject());
        assertEquals("octocat", toSave.getUsername());
        assertEquals("octocat@github.com", toSave.getEmail());
        assertEquals(Role.USER, toSave.getRole());

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getOrCreateFromOAuth_whenPrincipalNull_throwsNotAuthenticatedException() {
        assertThrows(NotAuthenticatedException.class, () -> userService.getOrCreateFromOAuth(null));
        verifyNoInteractions(userRepository);
    }
}