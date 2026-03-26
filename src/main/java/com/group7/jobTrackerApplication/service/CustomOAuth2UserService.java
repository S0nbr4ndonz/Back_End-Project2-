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
 * Customizes OAuth2 login by provisioning local users and assigning role authorities.
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
     * Loads the OAuth user from the provider, maps it to a local user record, and adds an application role authority.
     *
     * @param request OAuth2 user request supplied by Spring Security
     * @return OAuth user enriched with local role authority
     * @throws OAuth2AuthenticationException if required provider attributes are missing
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = fetchOAuthUser(request);
        String provider = request.getClientRegistration().getRegistrationId();
        String oauthSubject = resolveSubject(provider, oauthUser);
        String username = resolveUsername(provider, oauthUser);
        String email = (String) oauthUser.getAttribute("email");
        String nameAttributeKey = request.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        if (!StringUtils.hasText(nameAttributeKey)) {
            nameAttributeKey = "github".equals(provider) ? "login" : "sub";
        }

        User user = userRepository.findByOauthProviderAndOauthSubject(provider, oauthSubject)
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
                    u.setUsername(username);
                    u.setEmail(email);
                    u.setOauthProvider(provider);
                    u.setOauthSubject(oauthSubject);
                    u.setRole(resolveRole(provider, username));
                    return userRepository.save(u);
                });

        Set<GrantedAuthority> authorities = new HashSet<>(oauthUser.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return new DefaultOAuth2User(authorities, oauthUser.getAttributes(), nameAttributeKey);
    }

    private String resolveSubject(String provider, OAuth2User oauthUser) {
        Object subject = "google".equals(provider)
                ? oauthUser.getAttribute("sub")
                : oauthUser.getAttribute("id");
        if (subject == null) {
            throw new OAuth2AuthenticationException("Missing " + provider + " subject attribute");
        }
        return subject.toString();
    }

    private String resolveUsername(String provider, OAuth2User oauthUser) {
        if ("google".equals(provider)) {
            String email = (String) oauthUser.getAttribute("email");
            if (StringUtils.hasText(email)) {
                return email;
            }

            String name = (String) oauthUser.getAttribute("name");
            if (StringUtils.hasText(name)) {
                return name;
            }

            throw new OAuth2AuthenticationException("Missing Google username attributes");
        }

        String login = (String) oauthUser.getAttribute("login");
        if (!StringUtils.hasText(login)) {
            throw new OAuth2AuthenticationException("Missing GitHub login attribute");
        }
        return login;
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

    private Role resolveRole(String provider, String username) {
        if ("github".equals(provider)
                && StringUtils.hasText(adminGithubLogin)
                && adminGithubLogin.equalsIgnoreCase(username)) {
            return Role.ADMIN;
        }
        return Role.USER;
    }
}
