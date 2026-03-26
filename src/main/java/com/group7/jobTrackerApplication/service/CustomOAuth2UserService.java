package com.group7.jobTrackerApplication.service;

import com.group7.jobTrackerApplication.model.Role;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Customizes GitHub OAuth2 login by provisioning local users and assigning role authorities.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final String adminGithubLogin;

    public CustomOAuth2UserService(
            UserRepository userRepository,
            @Value("${app.admin.github-login:}") String adminGithubLogin
    ){
        this.userRepository = userRepository;
        this.adminGithubLogin = adminGithubLogin;
    }

    /**
     * Loads the OAuth user from GitHub, maps it to a local user record, and adds an application role authority.
     *
     * @param request OAuth2 user request supplied by Spring Security
     * @return OAuth user enriched with local role authority
     * @throws OAuth2AuthenticationException if required GitHub attributes are missing
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = fetchOAuthUser(request);

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
                .map(existingUser -> {
                    Role desiredRole = resolveRole(login);
                    if (existingUser.getRole() != desiredRole) {
                        existingUser.setRole(desiredRole);
                        return userRepository.save(existingUser);
                    }
                    return existingUser;
                })
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername(login);
                    u.setEmail((String) oauthUser.getAttribute("email"));
                    u.setOauthProvider("github");
                    u.setOauthSubject(oauthSubject);
                    u.setRole(resolveRole(login));
                    return userRepository.save(u);
                });

        Set<GrantedAuthority> authorities = new HashSet<>(oauthUser.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return new DefaultOAuth2User(authorities, oauthUser.getAttributes(), "login");
    }

    /**
     * Loads the raw OAuth user from the provider. Extracted to support tests.
     *
     * @param request OAuth2 user request supplied by Spring Security
     * @return raw OAuth user from the provider 
     */
    protected OAuth2User fetchOAuthUser(OAuth2UserRequest request) {
        return super.loadUser(request);
    }

    private Role resolveRole(String githubLogin) {
        if (StringUtils.hasText(adminGithubLogin) && adminGithubLogin.equalsIgnoreCase(githubLogin)) {
            return Role.ADMIN;
        }
        return Role.USER;
    }
}
