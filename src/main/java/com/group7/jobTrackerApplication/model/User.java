package com.group7.jobTrackerApplication.model;

import jakarta.persistence.*;
import com.group7.jobTrackerApplication.model.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing an authenticated application user.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Column(name = "oauth_provider")
    private String oauthProvider;

    @Column(name = "oauth_subject", nullable = false, unique = true)
    private String oauthSubject;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobEntry> jobEntries = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobApplication> jobApplications = new ArrayList<>();

    public User() {}


    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getOauthProvider() { return oauthProvider; }
    public void setOauthProvider(String oauthProvider) { this.oauthProvider = oauthProvider; }

    public String getOauthSubject() { return oauthSubject; }
    public void setOauthSubject(String oauthSubject) { this.oauthSubject = oauthSubject; }
}
