package com.group7.jobTrackerApplication.config;

import com.group7.jobTrackerApplication.service.CustomOAuth2UserService;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
public class SecurityConfig {

    // Change if your frontend runs on a different port
    private static final String FRONTEND_ORIGIN = "http://localhost:3000";

    // Where Spring sends the browser after OAuth login completes
    private static final String FRONTEND_SUCCESS_URL = "http://localhost:3000/oauth-success";

    @Bean
    SecurityFilterChain springFilterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService){
        http
                // Dev-friendly. If you add POST/PUT/DELETE later, handle CSRF properly.
                .csrf(AbstractHttpConfigurer::disable)

                // Required so React can call the API AND include cookies
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration cfg = new CorsConfiguration();
                    cfg.setAllowedOrigins(List.of(FRONTEND_ORIGIN));
                    cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                    cfg.setAllowedHeaders(List.of("*"));
                    cfg.setAllowCredentials(true);
                    return cfg;
                }))

                .authorizeHttpRequests(auth -> auth
                        // Preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public endpoints
                        .requestMatchers("/", "/error").permitAll()

                        // OAuth endpoints must be public
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // Protect your API
                        .requestMatchers("/api/**").authenticated()

                        // Everything else (adjust as you like)
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                // OAuth login (GitHub/Google/etc.)
                .oauth2Login(oauth -> oauth
                        .defaultSuccessUrl(FRONTEND_SUCCESS_URL, true)
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                )

                // Disable form login (otherwise APIs may redirect to HTML login)
                .formLogin(AbstractHttpConfigurer::disable)

                // API logout endpoint React can call
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessUrl(FRONTEND_ORIGIN + "/")
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("""
                                             {"error": "NOT AUTHENTICATED", "message" : "Authentication required"}
                                            """
                            );

                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("""
                                    {"error": "FORBIDDEN", "message" : "Insufficient permissions"}
                                    """);
                        })
                );

        return http.build();
    }
}
