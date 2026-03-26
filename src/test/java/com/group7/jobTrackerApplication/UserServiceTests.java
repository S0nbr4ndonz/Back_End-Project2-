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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, "S0nbr4ndonz");
    }

    @Test
    void getAllUsers_returnsAllUsers() {
        List<User> expected = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(expected);

        List<User> result = userService.getAllUsers();

        assertSame(expected, result);
        verify(userRepository).findAll();
        verifyNoMoreInteractions(userRepository);
    }

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

    @Test
    void update_whenUserExists_updatesRoleAndSaves() {
        Long userId = 5L;
        User existing = new User();
        existing.setRole(Role.USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.update(userId, new UpdateUserRoleRequest(Role.ADMIN));

        assertEquals(Role.ADMIN, result.getRole());
        verify(userRepository).findById(userId);
        verify(userRepository).save(existing);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void update_whenUserMissing_throwsResourceNotFoundException() {
        Long userId = 404L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.update(userId, new UpdateUserRoleRequest(Role.ADMIN)));

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

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

    @Test
    void getOrCreateFromOAuth_whenPrincipalNull_throwsNotAuthenticatedException() {
        assertThrows(NotAuthenticatedException.class, () -> userService.getOrCreateFromOAuth(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    void getOrCreateFromOAuth_whenTokenMissingId_throwsNotAuthenticatedException() {
        OAuth2User principal = org.mockito.Mockito.mock(OAuth2User.class);
        when(principal.getAttributes()).thenReturn(Map.of("login", "octocat"));

        assertThrows(NotAuthenticatedException.class, () -> userService.getOrCreateFromOAuth(principal));

        verifyNoInteractions(userRepository);
    }

    @Test
    void getOrCreateFromOAuth_whenUserMissing_createsStandardUser() {
        OAuth2User principal = org.mockito.Mockito.mock(OAuth2User.class);
        when(principal.getAttributes()).thenReturn(Map.of(
                "id", 12345L,
                "login", "octocat",
                "email", "octocat@github.com"
        ));

        when(userRepository.findByOauthProviderAndOauthSubject("github", "12345"))
                .thenReturn(Optional.empty());

        User saved = new User();
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userService.getOrCreateFromOAuth(principal);

        assertSame(saved, result);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
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
    void getOrCreateFromOAuth_whenGitHubLoginMatchesAdmin_promotesToAdmin() {
        OAuth2User principal = org.mockito.Mockito.mock(OAuth2User.class);
        when(principal.getAttributes()).thenReturn(Map.of(
                "id", 188244044L,
                "login", "S0nbr4ndonz",
                "email", "admin@example.com"
        ));

        when(userRepository.findByOauthProviderAndOauthSubject("github", "188244044"))
                .thenReturn(Optional.empty());

        User saved = new User();
        when(userRepository.save(any(User.class))).thenReturn(saved);

        userService.getOrCreateFromOAuth(principal);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(Role.ADMIN, userCaptor.getValue().getRole());
    }

    @Test
    void getOrCreateFromOAuth_whenExistingUserNeedsRoleChange_updatesAndSaves() {
        OAuth2User principal = org.mockito.Mockito.mock(OAuth2User.class);
        when(principal.getAttributes()).thenReturn(Map.of(
                "id", 188244044L,
                "login", "S0nbr4ndonz",
                "email", "admin@example.com"
        ));

        User existing = new User();
        existing.setRole(Role.USER);

        when(userRepository.findByOauthProviderAndOauthSubject("github", "188244044"))
                .thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        User result = userService.getOrCreateFromOAuth(principal);

        assertSame(existing, result);
        assertEquals(Role.ADMIN, existing.getRole());
        verify(userRepository).findByOauthProviderAndOauthSubject("github", "188244044");
        verify(userRepository).save(existing);
        verifyNoMoreInteractions(userRepository);
    }
}
