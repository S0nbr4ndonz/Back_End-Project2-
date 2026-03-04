package com.group7.jobTrackerApplication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * [Brief one-sentence description of what this class does.
 *
 * @author Drew "Dr.C" Clinkenbeard
 * @oversion 0.1.0
 * @since
 */

@Configuration
//@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain springFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests( auth -> {
                    auth.requestMatchers("/").permitAll();
                    auth.anyRequest().authenticated();
                })
                .oauth2Login(withDefaults())
                .formLogin(withDefaults())
                .build();
    }
}
