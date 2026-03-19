package com.group7.jobTrackerApplication.service;

import com.group7.jobTrackerApplication.model.Role;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = super.loadUser(request);

        String login = (String) oauthUser.getAttribute("login");
        if(login == null){
            throw new OAuth2AuthenticationException("Missing GitHub login attribute");
        }

        // Use GitHub ID as the OAuth subject for uniqueness
        Integer githubId = (Integer) oauthUser.getAttribute("id");
        if(githubId == null){
            throw new OAuth2AuthenticationException("Missing GitHub id attribute");
        }
        String oauthSubject = githubId.toString();

        User user = userRepository.findByOauthProviderAndOauthSubject("github", oauthSubject)
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername(login);
                    u.setEmail((String) oauthUser.getAttribute("email"));
                    u.setOauthProvider("github");
                    u.setOauthSubject(oauthSubject);
                    u.setRole(Role.USER);
                    return userRepository.save(u);
                });

        Set<GrantedAuthority> authorities = new HashSet<>(oauthUser.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return new DefaultOAuth2User(authorities, oauthUser.getAttributes(), "login");
    }
}
