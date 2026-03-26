package com.group7.jobTrackerApplication;

import com.group7.jobTrackerApplication.model.Role;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.repository.UserRepository;
import com.group7.jobTrackerApplication.service.CustomOAuth2UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTests {

    @Mock
    private UserRepository userRepository;

    private TestableCustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {
        customOAuth2UserService = new TestableCustomOAuth2UserService(userRepository, "S0nbr4ndonz");
    }

    @Test
    void loadUser_whenNewAdminLogin_createsAdminAndAddsRoleAuthority() {
        OAuth2UserRequest request = org.mockito.Mockito.mock(OAuth2UserRequest.class);
        OAuth2User oauthUser = new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("SCOPE_read:user")),
                Map.of("login", "S0nbr4ndonz", "email", "admin@example.com", "id", 188244044),
                "login"
        );

        customOAuth2UserService.setOAuthUser(oauthUser);
        when(userRepository.findByOauthProviderAndOauthSubject("github", "188244044")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OAuth2User result = customOAuth2UserService.loadUser(request);

        assertEquals("S0nbr4ndonz", result.getAttribute("login"));
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        verify(userRepository).findByOauthProviderAndOauthSubject("github", "188244044");
        verify(userRepository).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUser_whenExistingUserNeedsPromotion_updatesRole() {
        OAuth2UserRequest request = org.mockito.Mockito.mock(OAuth2UserRequest.class);
        OAuth2User oauthUser = new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("SCOPE_read:user")),
                Map.of("login", "S0nbr4ndonz", "email", "admin@example.com", "id", 188244044),
                "login"
        );

        User existing = new User();
        existing.setRole(Role.USER);

        customOAuth2UserService.setOAuthUser(oauthUser);
        when(userRepository.findByOauthProviderAndOauthSubject("github", "188244044")).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        OAuth2User result = customOAuth2UserService.loadUser(request);

        assertEquals(Role.ADMIN, existing.getRole());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        verify(userRepository).findByOauthProviderAndOauthSubject("github", "188244044");
        verify(userRepository).save(existing);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUser_whenLoginMissing_throwsOAuth2AuthenticationException() {
        OAuth2UserRequest request = org.mockito.Mockito.mock(OAuth2UserRequest.class);
        OAuth2User oauthUser = new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("SCOPE_read:user")),
                Map.of("email", "octo@cat.com", "id", 12345),
                "email"
        );

        customOAuth2UserService.setOAuthUser(oauthUser);

        assertThrows(OAuth2AuthenticationException.class, () -> customOAuth2UserService.loadUser(request));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUser_whenGitHubIdMissing_throwsOAuth2AuthenticationException() {
        OAuth2UserRequest request = org.mockito.Mockito.mock(OAuth2UserRequest.class);
        OAuth2User oauthUser = new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("SCOPE_read:user")),
                Map.of("login", "octocat", "email", "octo@cat.com"),
                "login"
        );

        customOAuth2UserService.setOAuthUser(oauthUser);

        assertThrows(OAuth2AuthenticationException.class, () -> customOAuth2UserService.loadUser(request));
        verifyNoMoreInteractions(userRepository);
    }

    private static final class TestableCustomOAuth2UserService extends CustomOAuth2UserService {
        private OAuth2User oauthUser;

        private TestableCustomOAuth2UserService(UserRepository userRepository, String adminGithubLogin) {
            super(userRepository, adminGithubLogin);
        }

        private void setOAuthUser(OAuth2User oauthUser) {
            this.oauthUser = oauthUser;
        }

        @Override
        protected OAuth2User fetchOAuthUser(OAuth2UserRequest request) {
            return oauthUser;
        }
    }
}
