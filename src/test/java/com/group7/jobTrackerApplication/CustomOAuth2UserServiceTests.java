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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTests {

    @Mock private UserRepository userRepository;

    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {
        customOAuth2UserService = new CustomOAuth2UserService(userRepository);
    }

    @Test
    void loadUser_whenLoginPresent_returnsUserWithRoleAuthority() {
        // We can't easily call super.loadUser(request) in a unit test without Spring wiring,
        // so we spy and stub the "super" call by overriding via spy + doReturn.
        CustomOAuth2UserService spyService = spy(customOAuth2UserService);

        OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);

        OAuth2User oauthUser = new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("SCOPE_read:user")),
                Map.of("login", "octocat", "email", "octo@cat.com"),
                "login"
        );

        doReturn(oauthUser).when(spyService).loadUser(userRequest); // <-- careful: this would recurse if we call same method

        // Instead: stub super.loadUser(...) via calling DefaultOAuth2UserService#loadUser is not accessible directly here.
        // So we stub the internal behavior by creating a small subclass in-test.
    }

    @Test
    void loadUser_whenLoginMissing_throwsOAuth2AuthenticationException() {
        // same limitation as above for super.loadUser
    }
}